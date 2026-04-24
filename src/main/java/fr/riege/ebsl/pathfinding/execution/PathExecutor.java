package fr.riege.ebsl.pathfinding.execution;


import fr.riege.ebsl.pathfinding.Node;
import fr.riege.ebsl.pathfinding.PathfinderConfig;
import fr.riege.ebsl.pathfinding.check.PathCheckContext;
import fr.riege.ebsl.pathfinding.check.PathCheckRegistry;
import fr.riege.ebsl.pathfinding.check.PathCheckResult;
import fr.riege.ebsl.pathfinding.check.PathProximitySnapshot;
import fr.riege.ebsl.pathfinding.debug.PathVisualizer;
import fr.riege.ebsl.pathfinding.movement.types.MovementValidationResult;
import fr.riege.ebsl.pathfinding.rotation.RotationExecutor;
import fr.riege.ebsl.util.ClientUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Optional;

/**
 * State machine that walks a player along a pre-computed path using keyboard injection.
 * Includes jump handling and automatic stall detection.
 */
public final class PathExecutor {

    public enum State { IDLE, WALKING, REPLANNING, FINISHED, FAILED }

    // Stall detection thresholds
    static final double STUCK_DIST_THRESHOLD = 0.2;
    static final long   STUCK_TIME_MS        = 2000;
    static final double DRIFT_DISTANCE       = 4.5;
    private static final long   REPLAN_COOLDOWN_MS   = 5000;

    // Jump triggers
    static final double JUMP_TRIGGER_DIST      = 0.6;
    static final double STEP_UP_TRIGGER_DIST   = 1.0;
    static final int    JUMP_COOLDOWN_TICKS = 8;
    static final long   STALL_JUMP_PROGRESS_MS = 450;

    private static final double PATH_PROGRESS_EPSILON = 0.08;

    static final double WALK_TARGET_DEADZONE = 0.28;
    static final double WALK_FORWARD_DOT     = 0.18;
    static final double WALK_BACKWARD_DOT    = -0.45;
    static final double WALK_STRAFE_DOT      = 0.32;

    private static final long   COAST_TIMEOUT_MS    = 3000;

    /** Direct goal proximity check - if player is this close horizontally, finish. */
    private static final double GOAL_REACHED_HDIST = 1.2;
    private static final double GOAL_REACHED_VDIST = 2.0;

    private State      state = State.IDLE;
    private boolean    precise;

    private long lastReplanTime  = 0;
    private long coastStartTime  = 0;
    private int  jumpCooldown    = 0;
    private int  goalX, goalY, goalZ;
    private double goalCenterX   = 0.5;
    private double goalCenterZ   = 0.5;
    private boolean allowReplan  = true;
    private boolean allowJumps   = true;
    private boolean allowRotation = true;
    private boolean exactGoalCentering = false;
    private double stickySneakDistance = -1.0;
    private boolean sneakLatched = false;
    private double preciseGoalTolerance = 0.5;
    private Runnable onFinished;
    private boolean replanFromPlayerRequested = false;
    private PathRepairRequest repairRequest;
    private String lastRepairReason = "";
    private long lastRepairReasonTime = 0;
    private WalkMovementController movementController;
    private final PathTracker pathTracker = new PathTracker();
    private final PathRecoveryController recoveryController = new PathRecoveryController();
    private final PathRotationController rotationController = new PathRotationController();

    // --- Public API ----------------------------------------------------------

    public void start(List<Node> path, int goalX, int goalY, int goalZ, boolean precise) {
        start(path, goalX, goalY, goalZ, precise, null);
    }

    public void start(List<Node> path, int goalX, int goalY, int goalZ, boolean precise,
                      Runnable onFinished) {
        this.goalX      = goalX;
        this.goalY      = goalY;
        this.goalZ      = goalZ;
        this.precise    = precise;
        this.onFinished = onFinished;
        resetConfig();
        resetTransientState();
        pathTracker.start(path);
        rotationController.rebuild(path);
        movementController = new WalkMovementController(path);
        state = State.WALKING;
    }

    private void resetConfig() {
        allowReplan          = true;
        allowJumps           = true;
        allowRotation        = true;
        exactGoalCentering   = false;
        stickySneakDistance  = -1.0;
        preciseGoalTolerance = 0.5;
        goalCenterX          = 0.5;
        goalCenterZ          = 0.5;
    }

    private void resetTransientState() {
        jumpCooldown              = 0;
        coastStartTime            = 0;
        replanFromPlayerRequested = false;
        repairRequest             = null;
        lastRepairReason          = "";
        lastRepairReasonTime      = 0;
        sneakLatched              = false;
        recoveryController.reset();
    }

    public void setAllowReplan(boolean allowReplan) {
        this.allowReplan = allowReplan;
    }

    public void setPreciseGoalTolerance(double preciseGoalTolerance) {
        this.preciseGoalTolerance = Math.max(0.01, preciseGoalTolerance);
    }

    public void setAllowJumps(boolean allowJumps) {
        this.allowJumps = allowJumps;
    }

    public void setAllowRotation(boolean allowRotation) {
        this.allowRotation = allowRotation;
    }

    public void setExactGoalCentering(boolean exactGoalCentering) {
        this.exactGoalCentering = exactGoalCentering;
    }

    public void setStickySneakDistance(double stickySneakDistance) {
        this.stickySneakDistance = stickySneakDistance;
    }

    public void setSneakLatched(boolean sneakLatched) {
        this.sneakLatched = sneakLatched;
    }

    public void setGoalCenterOffsets(double goalCenterX, double goalCenterZ) {
        this.goalCenterX = goalCenterX;
        this.goalCenterZ = goalCenterZ;
    }

    public boolean isSneakLatched() {
        return sneakLatched;
    }

    public State getState() { return state; }
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
        if (ClientUtils.isInventoryScreenOpen(mc)) {
            releaseAll(mc);
            return;
        }

        if (mc.player.getAbilities().flying) {
            releaseAll(mc);
            mc.options.keyShift.setDown(true);
            return;
        }

        jumpCooldown = Math.max(0, jumpCooldown - 1);

        Vec3 playerPos = mc.player.position();

        List<Node> path = pathTracker.getPath();
        if (path == null || path.isEmpty()) {
            finish(mc);
            return;
        }

        // -- Direct goal proximity check -------------------------------------
        // Catches cases where waypoint advancement gets stuck but player is at the goal.
        if (isAtGoal(playerPos)) {
            finish(mc);
            return;
        }

        // -- Movement progress tracking -------------------------------------
        double distMoved = pathTracker.noteMovementProgress(playerPos, STUCK_DIST_THRESHOLD);

        long now = System.currentTimeMillis();
        pathTracker.advancePursuit(playerPos, now);

        pathTracker.computeAndTrackPathProgress(playerPos, PATH_PROGRESS_EPSILON, now);

        PathProximitySnapshot proximity = pathTracker.analyzePathProximity(playerPos);
        pathTracker.updateSevereOffPathState(proximity, now);

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
            return;
        }
        if (pathCheckResult.isRepairToSegment()) {
            triggerRepair(mc, pathCheckResult.cutoffSegmentIndex(), pathCheckResult.reason());
            return;
        }
        if (pathCheckResult.isCutoff()) {
            if (pathTracker.applySmartCutoff(pathCheckResult.cutoffSegmentIndex())) {
                path = pathTracker.getPath();
                rotationController.rebuild(path);
                movementController = new WalkMovementController(path);
                PathVisualizer.setPath(path, 0);
                PathVisualizer.setCameraPath(rotationController.getCameraPath());
                PathVisualizer.updateExecution(pathTracker.getPursuitSegment(), rotationController.getCamTargetIdx());
                proximity = pathTracker.analyzePathProximity(playerPos);
            }
        }

        MovementValidationResult movementValidation = movementController.validateCurrentSegment(
            mc, playerPos, pathTracker.getPursuitSegment());
        if (!movementValidation.valid()) {
            triggerRepair(
                mc,
                Math.min(path.size() - 2, pathTracker.getPursuitSegment() + 1),
                movementValidation.reason());
            return;
        }

        // Periodic debug
        if (PathfinderConfig.SHOW_DEBUG.get() && now % 2000 < 50 && pathTracker.getPursuitSegment() < path.size()) {
            Node wp   = path.get(pathTracker.getPursuitSegment());
            double dx = wp.position.centeredX() - playerPos.x;
            double dz = wp.position.centeredZ() - playerPos.z;
            double hDist = Math.sqrt(dx * dx + dz * dz);
            ClientUtils.sendDebugMessage(mc, String.format(
                    "path seg=%d/%d hDist=%.2f",
                    pathTracker.getPursuitSegment(), path.size(), hDist));
        }

        // -- Goal reached / coasting ----------------------------------------
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
            this,
            mc,
            playerPos,
            progress,
            allowReplan,
            now - lastReplanTime > REPLAN_COOLDOWN_MS,
            jumpCooldown);
        if (recovery.action() == PathRecoveryController.Action.REPLAN_FROM_PLAYER) {
            triggerReplan(mc, false, true, recovery.reason());
            return;
        }
        if (recovery.action() == PathRecoveryController.Action.TICK_HANDLED) {
            return;
        }
        boolean recoveryJump = recovery.action() == PathRecoveryController.Action.RECOVERY_JUMP;
        if (recoveryJump) {
            jumpCooldown = JUMP_COOLDOWN_TICKS;
        }

        // -- Rotation: look at best visible waypoint ahead -------------
        if (allowRotation) {
            rotationController.updateRotation(mc, playerPos, path, pathTracker.getPursuitSegment(), message -> {
            });
        }

        // -- Distance to final goal -----------------------------------------
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

    // --- Internal helpers ----------------------------------------------------

    private void rebuildControllers() {
        List<Node> path = pathTracker.getPath();
        rotationController.rebuild(path);
        this.movementController = new WalkMovementController(path);
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
        Optional<PathRepairRequest> request = pathTracker.createRepairRequest(segmentIndex, reason);
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

    private boolean isAtGoal(Vec3 playerPos) {
        double dx = (goalX + 0.5) - playerPos.x;
        double dz = (goalZ + 0.5) - playerPos.z;
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

}
