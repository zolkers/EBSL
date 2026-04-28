package fr.riege.ebsl.pathfinding.execution;


import fr.riege.ebsl.pathfinding.NavigationMode;
import fr.riege.ebsl.pathfinding.Node;
import fr.riege.ebsl.pathfinding.PathfinderConfig;
import fr.riege.ebsl.pathfinding.annotation.NavigationModeHandler;
import fr.riege.ebsl.pathfinding.annotation.PathStatePersistence;
import fr.riege.ebsl.pathfinding.annotation.PathStateTransition;
import fr.riege.ebsl.pathfinding.annotation.PathingStage;
import fr.riege.ebsl.pathfinding.check.PathCheckContext;
import fr.riege.ebsl.pathfinding.check.PathCheckRegistry;
import fr.riege.ebsl.pathfinding.check.PathCheckResult;
import fr.riege.ebsl.pathfinding.check.PathProximitySnapshot;
import fr.riege.ebsl.pathfinding.debug.PathVisualizer;
import fr.riege.ebsl.pathfinding.movement.types.evaluation.MovementValidationResult;
import fr.riege.ebsl.pathfinding.rotation.RotationExecutor;
import fr.riege.ebsl.util.ClientUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Optional;

@PathingStage(PathingStage.Stage.EXECUTION)
@NavigationModeHandler(NavigationMode.WALK)
@PathStatePersistence(
    value = PathStatePersistence.Scope.EXECUTION,
    reason = "Owns the active executable path and its movement/replan state.")
public final class PathExecutor {

    public enum State { IDLE, WALKING, REPLANNING, FINISHED, FAILED }

    static final double STUCK_DIST_THRESHOLD = 0.2;
    static final long   STUCK_TIME_MS        = 400;
    static final double DRIFT_DISTANCE       = 4.5;
    private static final long   REPLAN_COOLDOWN_MS   = 2500;

    static final double JUMP_TRIGGER_DIST      = 0.6;
    static final double STEP_UP_TRIGGER_DIST   = 1.0;
    static final int    JUMP_COOLDOWN_TICKS = 8;
    static final long   STALL_JUMP_PROGRESS_MS = 450;

    private static final double PATH_PROGRESS_EPSILON = 0.08;

    static final double WALK_TARGET_DEADZONE = 0.28;
    static final double WALK_FORWARD_DOT     = 0.18;
    static final double WALK_BACKWARD_DOT    = -0.45;
    static final double WALK_STRAFE_DOT      = 0.32;

    private static final long   COAST_TIMEOUT_MS         = 3000;
    private static final long   SMART_CUTOFF_COOLDOWN_MS = 800;
    private static final int    LOCAL_REPAIR_LOOKAHEAD = 4;
    private static final int    LOCAL_REPAIR_DRIFT_LOOKAHEAD = 7;
    private static final double LOCAL_REPAIR_DRIFT_THRESHOLD = 1.5;

    private static final double GOAL_REACHED_HDIST = 1.2;
    private static final double GOAL_REACHED_VDIST = 2.0;

    private State      state = State.IDLE;
    private boolean    precise;

    private long lastReplanTime       = 0;
    private long coastStartTime       = 0;
    private long lastSmartCutoffTime  = 0;
    private int  jumpCooldown         = 0;
    private int  goalX, goalY, goalZ;
    private double goalCenterX        = 0.5;
    private double goalCenterZ        = 0.5;
    private boolean allowReplan       = true;
    private boolean allowJumps        = true;
    private boolean allowRotation     = true;
    private boolean exactGoalCentering = false;
    private double stickySneakDistance = -1.0;
    private boolean sneakLatched      = false;
    private double preciseGoalTolerance = ExecutionOptions.DEFAULT_TOLERANCE;
    private Runnable onFinished;
    private boolean replanFromPlayerRequested = false;
    private PathRepairRequest repairRequest;
    private String lastRepairReason = "";
    private long lastRepairReasonTime = 0;
    private WalkMovementController movementController;
    @PathStatePersistence(PathStatePersistence.Scope.EXECUTION)
    private final PathTracker pathTracker = new PathTracker();
    private final PathRecoveryController recoveryController = new PathRecoveryController();
    private final PathRotationController rotationController = new PathRotationController();

    public void start(List<Node> path, int goalX, int goalY, int goalZ, boolean precise) {
        start(path, goalX, goalY, goalZ, precise, null);
    }

    public void start(List<Node> path, int goalX, int goalY, int goalZ, boolean precise,
                      Runnable onFinished) {
        start(new ExecutionPlan(path, goalX, goalY, goalZ, precise, onFinished));
    }

    @PathStateTransition(PathStateTransition.Action.BEGIN)
    public void start(ExecutionPlan plan) {
        this.goalX      = plan.goalX();
        this.goalY      = plan.goalY();
        this.goalZ      = plan.goalZ();
        this.precise    = plan.precise();
        this.onFinished = plan.onFinished();
        ExecutionOptions opts = plan.options();
        this.allowReplan          = opts.allowReplan();
        this.allowJumps           = opts.allowJumps();
        this.allowRotation        = opts.allowRotation();
        this.exactGoalCentering   = opts.exactGoalCentering();
        this.stickySneakDistance  = opts.stickySneakDistance();
        this.preciseGoalTolerance = Math.max(0.01, opts.preciseGoalTolerance());
        this.goalCenterX          = opts.goalCenterX();
        this.goalCenterZ          = opts.goalCenterZ();
        this.sneakLatched         = opts.sneakLatched();
        resetTransientState();
        pathTracker.start(plan.path());
        rotationController.rebuild(plan.path());
        movementController = new WalkMovementController(plan.path());
        state = State.WALKING;
    }

    private void resetTransientState() {
        jumpCooldown              = 0;
        coastStartTime            = 0;
        lastSmartCutoffTime       = 0;
        replanFromPlayerRequested = false;
        repairRequest             = null;
        lastRepairReason          = "";
        lastRepairReasonTime      = 0;
        sneakLatched              = false;
        recoveryController.reset();
    }

    /** Externally latch/unlatch sneak (e.g. from PathfindingManager). */
    public void setSneakLatched(boolean sneakLatched) {
        this.sneakLatched = sneakLatched;
    }

    public State getState() { return state; }
    public Node.MoveType getCurrentMoveType() {
        if (state != State.WALKING) return null;
        Node waypoint = pathTracker.getMovementWaypoint();
        return waypoint != null ? waypoint.moveType : null;
    }
    public int getWaypointIndex() { return pathTracker.getPursuitSegment(); }
    public int getCamTargetIdx()  { return rotationController.getCamTargetIdx(); }
    public List<Vec3> getCameraPath() { return rotationController.getCameraPath(); }
    public List<Node> getPathSnapshot() { return pathTracker.getPathSnapshot(); }
    public int getCameraIndex() { return rotationController.getCameraIndex(); }
    void setJumpCooldown(int jumpCooldown) { this.jumpCooldown = jumpCooldown; }
    public double getProgressRatio(Vec3 playerPos) {
        return pathTracker.getProgressRatio(playerPos);
    }

    public double getRemainingDistance(Vec3 playerPos) {
        return pathTracker.getRemainingDistance(playerPos);
    }

    public boolean isSneakLatched() {
        return sneakLatched;
    }

    public boolean consumeReplanFromPlayerRequest() {
        boolean requested = replanFromPlayerRequested;
        replanFromPlayerRequested = false;
        return requested;
    }

    public PathRepairRequest consumeRepairRequest() {
        PathRepairRequest request = repairRequest;
        repairRequest = null;
        return request;
    }

    public void rememberRecentRepair(String reason) {
        lastRepairReason = reason;
        lastRepairReasonTime = System.currentTimeMillis();
    }

    @PathStateTransition(PathStateTransition.Action.MERGE)
    public void continueWith(List<Node> continuationPath, int goalX, int goalY, int goalZ) {
        if (continuationPath == null || continuationPath.isEmpty()) {
            return;
        }
        this.goalX = goalX;
        this.goalY = goalY;
        this.goalZ = goalZ;
        pathTracker.continueWith(continuationPath);
        rebuildControllers();
    }

    @PathStateTransition(PathStateTransition.Action.MERGE)
    public void trimAndContinueWith(double trimRatio, List<Node> newPath, int goalX, int goalY, int goalZ) {
        if (newPath == null || newPath.isEmpty()) return;
        this.goalX = goalX;
        this.goalY = goalY;
        this.goalZ = goalZ;
        pathTracker.trimAndContinueWith(trimRatio, newPath);
        rebuildControllers();
    }

    public Node getNodeAtRatio(double ratio) {
        return pathTracker.getNodeAtRatio(ratio);
    }

    public void tick(Minecraft mc) {
        if (mc.player == null) return;
        if (state != State.WALKING && state != State.REPLANNING) return;
        if (pauseForBlockingClientState(mc)) {
            return;
        }

        jumpCooldown = Math.max(0, jumpCooldown - 1);

        Vec3 playerPos = mc.player.position();

        List<Node> path = pathTracker.getPath();
        if (path == null || path.isEmpty()) {
            finish(mc);
            return;
        }

        if (isAtGoal(playerPos)) {
            finish(mc);
            return;
        }

        double distMoved = pathTracker.noteMovementProgress(playerPos, STUCK_DIST_THRESHOLD);

        long now = System.currentTimeMillis();
        pathTracker.advancePursuit(playerPos, now);

        pathTracker.computeAndTrackPathProgress(playerPos, PATH_PROGRESS_EPSILON, now);

        PathProximitySnapshot proximity = pathTracker.analyzePathProximity(playerPos);
        pathTracker.updateSevereOffPathState(proximity, now);

        PathCheckHandling pathCheckHandling = handlePathChecks(mc, playerPos, path, proximity, now);
        if (pathCheckHandling.handled()) {
            return;
        }
        path = pathCheckHandling.path();
        proximity = pathCheckHandling.proximity();

        MovementValidationResult movementValidation = movementController.validateCurrentSegment(
            mc, playerPos, pathTracker.getPursuitSegment());
        if (!movementValidation.valid()) {
            triggerRepair(
                mc,
                Math.min(path.size() - 2, pathTracker.getPursuitSegment() + 1),
                movementValidation.reason());
            return;
        }

        if (PathfinderConfig.SHOW_DEBUG.get() && now % 2000 < 50 && pathTracker.getPursuitSegment() < path.size()) {
            Node wp   = path.get(pathTracker.getPursuitSegment());
            double dx = wp.position.centeredX() - playerPos.x;
            double dz = wp.position.centeredZ() - playerPos.z;
            double hDist = Math.sqrt(dx * dx + dz * dz);
            ClientUtils.sendDebugMessage(mc, String.format(
                    "path seg=%d/%d hDist=%.2f",
                    pathTracker.getPursuitSegment(), path.size(), hDist));
        }

        if (pathTracker.getPursuitSegment() >= path.size() - 1) {
            tickGoalCoasting(mc, playerPos);
            return;
        }

        PathProgressSnapshot progress = new PathProgressSnapshot(
            distMoved,
            now - pathTracker.getLastProgressTime(),
            now - pathTracker.getLastPathProgressTime(),
            proximity);
        PathRecoveryController.RecoveryDecision recovery = recoveryController.update(
            mc,
            playerPos,
            progress,
            allowReplan,
            now - lastReplanTime > REPLAN_COOLDOWN_MS,
            jumpCooldown);
        if (recovery.noteProgress()) {
            noteRecoveryMovement(playerPos);
        }
        if (recovery.action() == PathRecoveryController.Action.REPLAN_FROM_PLAYER) {
            triggerReplan(mc, false, true, recovery.reason());
            return;
        }
        if (recovery.action() == PathRecoveryController.Action.REPAIR_TO_SEGMENT) {
            triggerRepair(mc, chooseLocalRepairSegment(path, proximity), recovery.reason());
            return;
        }
        if (recovery.action() == PathRecoveryController.Action.TICK_HANDLED) {
            return;
        }
        boolean recoveryJump = recovery.action() == PathRecoveryController.Action.RECOVERY_JUMP;
        if (recoveryJump) {
            jumpCooldown = JUMP_COOLDOWN_TICKS;
        }

        if (allowRotation) {
            rotationController.updateRotation(mc, playerPos, path, pathTracker.getPursuitSegment(), message -> {
            });
        }

        double finalDx = (goalX + goalCenterX) - playerPos.x;
        double finalDy = goalY         - playerPos.y;
        double finalDz = (goalZ + goalCenterZ) - playerPos.z;
        double distToFinal = Math.sqrt(finalDx * finalDx + finalDy * finalDy + finalDz * finalDz);

        if (!sneakLatched && stickySneakDistance > 0 && distToFinal <= stickySneakDistance) {
            sneakLatched = true;
        }

        Node movementWp = getMovementWaypoint();
        movementController.apply(this, mc, playerPos, movementWp, distToFinal,
                allowJumps && !recoveryJump, sneakLatched, pathTracker.getPursuitSegment(), jumpCooldown, pathTracker.getLastProgressTime());

        if (sneakLatched) {
            mc.options.keyShift.setDown(true);
        }
    }

    public void stop(Minecraft mc) {
        state = State.IDLE;
        RotationExecutor.stopRotating();
        releaseAll(mc);
    }

    public void releaseAll(Minecraft mc) {
        InputApplier.releaseAll(mc, sneakLatched);
    }

    void noteRecoveryMovement(Vec3 playerPos) {
        pathTracker.noteMovementProgress(playerPos, 0.0);
    }

    private boolean pauseForBlockingClientState(Minecraft mc) {
        if (ClientUtils.isInventoryScreenOpen(mc)) {
            releaseAll(mc);
            return true;
        }

        if (mc.player.getAbilities().flying) {
            releaseAll(mc);
            mc.options.keyShift.setDown(true);
            return true;
        }

        return false;
    }

    private PathCheckHandling handlePathChecks(Minecraft mc, Vec3 playerPos,
                                               List<Node> path,
                                               PathProximitySnapshot proximity,
                                               long now) {
        PathCheckResult pathCheckResult = PathCheckRegistry.evaluate(new PathCheckContext(
            playerPos,
            path,
            pathTracker.getPursuitSegment(),
            goalX,
            goalY,
            goalZ,
            pathTracker.getSevereOffPathDuration(now),
            proximity
        ));

        if (pathCheckResult.isForceReplan()) {
            triggerReplan(mc, true, true, pathCheckResult.reason());
            return PathCheckHandling.handled(path, proximity);
        }
        if (pathCheckResult.isRepairToSegment()) {
            triggerRepair(mc, pathCheckResult.cutoffSegmentIndex(), pathCheckResult.reason());
            return PathCheckHandling.handled(path, proximity);
        }
        if (!pathCheckResult.isCutoff()
                || now - lastSmartCutoffTime < SMART_CUTOFF_COOLDOWN_MS
                || !pathTracker.applySmartCutoff(pathCheckResult.cutoffSegmentIndex())) {
            return PathCheckHandling.continueWith(path, proximity);
        }
        lastSmartCutoffTime = now;

        List<Node> updatedPath = pathTracker.getPath();
        rotationController.rebuild(updatedPath);
        movementController.setPath(updatedPath);
        PathVisualizer.setPath(updatedPath, 0);
        PathVisualizer.setCameraPath(rotationController.getCameraPath());
        PathVisualizer.updateExecution(pathTracker.getPursuitSegment(), rotationController.getCamTargetIdx());
        return PathCheckHandling.continueWith(updatedPath, pathTracker.analyzePathProximity(playerPos));
    }

    private void rebuildControllers() {
        List<Node> path = pathTracker.getPath();
        rotationController.rebuild(path);
        movementController.setPath(path);
    }

    private Node getMovementWaypoint() {
        return pathTracker.getMovementWaypoint();
    }

    private void triggerReplan(Minecraft mc, boolean force, boolean fromPlayer, String reason) {
        releaseAll(mc);
        lastReplanTime = System.currentTimeMillis();
        pathTracker.markReplanTriggered(lastReplanTime);
        replanFromPlayerRequested = fromPlayer;
        if (PathfinderConfig.SHOW_DEBUG.get()) {
            ClientUtils.sendDebugMessage(mc, "Path replan: " + reason);
        }
        state = State.REPLANNING;
    }

    private void triggerRepair(Minecraft mc, int segmentIndex, String reason) {
        long now = System.currentTimeMillis();
        if (reason.equals(lastRepairReason) && now - lastRepairReasonTime < 1500) {
            triggerReplan(mc, false, true, "repair loop suppressed: " + reason);
            return;
        }
        Optional<PathRepairRequest> request = pathTracker.createRepairRequest(segmentIndex, reason, goalX, goalY, goalZ);
        if (request.isEmpty()) {
            triggerReplan(mc, false, true, reason);
            return;
        }
        releaseAll(mc);
        lastRepairReason = reason;
        lastRepairReasonTime = now;
        lastReplanTime = now;
        pathTracker.markReplanTriggered(lastReplanTime);
        repairRequest = request.get();
        if (PathfinderConfig.SHOW_DEBUG.get()) {
            ClientUtils.sendDebugMessage(mc, "Path repair: " + reason);
        }
        state = State.REPLANNING;
    }

    private int chooseLocalRepairSegment(List<Node> path, PathProximitySnapshot proximity) {
        if (path == null || path.size() < 2) {
            return 0;
        }
        int base = Math.max(pathTracker.getPursuitSegment(), proximity.nearestSegmentIndex());
        int lookahead = proximity.horizontalDistance() > LOCAL_REPAIR_DRIFT_THRESHOLD
            ? LOCAL_REPAIR_DRIFT_LOOKAHEAD
            : LOCAL_REPAIR_LOOKAHEAD;
        int target = base + lookahead;
        int maxJoinSegment = Math.max(0, path.size() - 2);
        return Math.max(0, Math.min(target, maxJoinSegment));
    }

    private boolean isAtGoal(Vec3 playerPos) {
        double dx = (goalX + goalCenterX) - playerPos.x;
        double dz = (goalZ + goalCenterZ) - playerPos.z;
        return Math.sqrt(dx * dx + dz * dz) <= GOAL_REACHED_HDIST
            && Math.abs(goalY - playerPos.y) <= GOAL_REACHED_VDIST;
    }

    private void tickGoalCoasting(Minecraft mc, Vec3 playerPos) {
        if (!precise) {
            finish(mc);
            return;
        }
        if (coastStartTime == 0) coastStartTime = System.currentTimeMillis();
        double dx = (goalX + goalCenterX) - playerPos.x;
        double dz = (goalZ + goalCenterZ) - playerPos.z;
        double hDist = Math.sqrt(dx * dx + dz * dz);
        if (exactGoalCentering && hDist > preciseGoalTolerance) {
            applyGoalCenteringMovement(mc, dx, dz);
        } else {
            releaseAll(mc);
        }
        if (hDist <= preciseGoalTolerance
                || System.currentTimeMillis() - coastStartTime > COAST_TIMEOUT_MS) {
            finish(mc);
        }
    }

    private void applyGoalCenteringMovement(Minecraft mc, double dx, double dz) {
        if (mc.player == null) {
            return;
        }

        InputApplier.applyGoalCentering(mc, dx, dz);
    }

    private void finish(Minecraft mc) {
        RotationExecutor.stopRotating();
        releaseAll(mc);
        state = State.FINISHED;
        if (onFinished != null) {
            onFinished.run();
        }
    }

    private record PathCheckHandling(boolean handled, List<Node> path, PathProximitySnapshot proximity) {
        static PathCheckHandling handled(List<Node> path, PathProximitySnapshot proximity) {
            return new PathCheckHandling(true, path, proximity);
        }

        static PathCheckHandling continueWith(List<Node> path, PathProximitySnapshot proximity) {
            return new PathCheckHandling(false, path, proximity);
        }
    }

}
