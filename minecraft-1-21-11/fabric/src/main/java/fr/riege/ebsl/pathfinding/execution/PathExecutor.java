package fr.riege.ebsl.pathfinding.execution;


import fr.riege.ebsl.pathfinding.NavigationMode;
import fr.riege.ebsl.pathfinding.Node;
import fr.riege.ebsl.pathfinding.annotation.NavigationModeHandler;
import fr.riege.ebsl.pathfinding.settings.PathfinderSettings;
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
    private boolean allowSneak        = true;
    private boolean exactGoalCentering = false;
    private double stickySneakDistance = -1.0;
    private boolean sneakLatched      = false;
    private double preciseGoalTolerance = ExecutionOptions.DEFAULT_TOLERANCE;
    private Runnable onFinished;
    private boolean finishAtPathEnd = true;
    private boolean replanFromPlayerRequested = false;
    private PathRepairRequest repairRequest;
    private String lastRepairReason = "";
    private long lastRepairReasonTime = 0;
    private Node.MoveType lastKnownMoveType = Node.MoveType.WALK;
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
        this.allowSneak           = opts.allowSneak();
        this.exactGoalCentering   = opts.exactGoalCentering();
        this.stickySneakDistance  = allowSneak ? opts.stickySneakDistance() : -1.0;
        this.preciseGoalTolerance = Math.max(0.01, opts.preciseGoalTolerance());
        this.goalCenterX          = opts.goalCenterX();
        this.goalCenterZ          = opts.goalCenterZ();
        this.sneakLatched         = allowSneak && opts.sneakLatched();
        this.finishAtPathEnd      = plan.finishAtPathEnd();
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
        lastKnownMoveType         = Node.MoveType.WALK;
        sneakLatched              = false;
        recoveryController.reset();
    }

    /** Externally latch/unlatch sneak (e.g. from PathfindingManager). */
    public void setSneakLatched(boolean sneakLatched) {
        this.sneakLatched = sneakLatched;
    }

    public State getState() { return state; }
    public Node.MoveType getCurrentMoveType() {
        if (state == State.REPLANNING) return lastKnownMoveType;
        if (state != State.WALKING) return null;
        Node waypoint = pathTracker.getMovementWaypoint();
        if (waypoint != null && waypoint.moveType != null) {
            lastKnownMoveType = waypoint.moveType;
        }
        return lastKnownMoveType;
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

        if (mc.player.onGround() || mc.player.isInWater()) {
            jumpCooldown = Math.max(0, jumpCooldown - 1);
        }

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

        double distMoved = pathTracker.noteMovementProgress(playerPos, PathfinderSettings.instance().stuckDistThreshold.value());

        long now = System.currentTimeMillis();
        pathTracker.advancePursuit(playerPos, now);

        pathTracker.computeAndTrackPathProgress(playerPos, PathfinderSettings.instance().pathProgressEpsilon.value(), now);

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

        if (PathfinderSettings.instance().showDebug.value() && now % 2000 < 50 && pathTracker.getPursuitSegment() < path.size()) {
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
            now - lastReplanTime > PathfinderSettings.instance().replanCooldownMs.value(),
            jumpCooldown,
            recoveryMoveType(path, pathTracker.getPursuitSegment()));
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
        if (recovery.action() == PathRecoveryController.Action.ALIGN_TO_PATH) {
            Vec3 correction = pathTracker.computeCorrectionToPath(playerPos, proximity);
            InputApplier.applyCornerAlignment(mc, correction.x, correction.z);
            return;
        }
        boolean recoveryJump = recovery.action() == PathRecoveryController.Action.RECOVERY_JUMP;
        if (recoveryJump) {
            jumpCooldown = PathfinderSettings.instance().jumpCooldownTicks.value();
        }

        if (allowRotation) {
            rotationController.updateRotation(mc, playerPos, path, pathTracker.getPursuitSegment(), message -> {
            });
        }

        double finalDx = (goalX + goalCenterX) - playerPos.x;
        double finalDy = goalY         - playerPos.y;
        double finalDz = (goalZ + goalCenterZ) - playerPos.z;
        double distToFinal = Math.sqrt(finalDx * finalDx + finalDy * finalDy + finalDz * finalDz);

        if (allowSneak && !sneakLatched && stickySneakDistance > 0 && distToFinal <= stickySneakDistance) {
            sneakLatched = true;
        }

        Node movementWp = getMovementWaypoint();
        movementController.apply(this, mc, playerPos, movementWp, distToFinal,
                allowJumps && !recoveryJump, allowSneak && sneakLatched, pathTracker.getPursuitSegment(), jumpCooldown, pathTracker.getLastProgressTime());

        if (allowSneak && sneakLatched) {
            mc.options.keyShift.setDown(true);
        } else {
            mc.options.keyShift.setDown(false);
        }
    }

    public void stop(Minecraft mc) {
        state = State.IDLE;
        RotationExecutor.stopRotating();
        releaseAll(mc);
    }

    public void releaseAll(Minecraft mc) {
        InputApplier.releaseAll(mc, allowSneak && sneakLatched);
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
                || now - lastSmartCutoffTime < PathfinderSettings.instance().smartCutoffCooldownMs.value()
                || !pathTracker.applySmartCutoff(pathCheckResult.cutoffSegmentIndex())) {
            return PathCheckHandling.continueWith(path, proximity);
        }
        lastSmartCutoffTime = now;

        List<Node> updatedPath = pathTracker.getPath();
        rotationController.rebuild(updatedPath);
        movementController.setPath(updatedPath);
        PathVisualizer.setPath(updatedPath);
        PathVisualizer.setCameraPath(rotationController.getCameraPath());
        PathVisualizer.updateExecution(rotationController.getCamTargetIdx());
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
        if (PathfinderSettings.instance().showDebug.value()) {
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
        if (PathfinderSettings.instance().showDebug.value()) {
            ClientUtils.sendDebugMessage(mc, "Path repair: " + reason);
        }
        state = State.REPLANNING;
    }

    private int chooseLocalRepairSegment(List<Node> path, PathProximitySnapshot proximity) {
        if (path == null || path.size() < 2) {
            return 0;
        }
        int base = Math.max(pathTracker.getPursuitSegment(), proximity.nearestSegmentIndex());
        int lookahead = proximity.horizontalDistance() > PathfinderSettings.instance().localRepairDriftThreshold.value()
            ? PathfinderSettings.instance().localRepairDriftLookahead.value()
            : PathfinderSettings.instance().localRepairLookahead.value();
        int target = base + lookahead;
        int maxJoinSegment = Math.max(0, path.size() - 2);
        return Math.max(0, Math.min(target, maxJoinSegment));
    }

    private boolean isParkourRecoveryWindow(List<Node> path, int pursuitSegment) {
        if (path == null || path.isEmpty()) {
            return false;
        }
        int start = Math.max(0, pursuitSegment);
        int end = Math.min(path.size() - 1, start + 2);
        for (int i = start; i <= end; i++) {
            if (path.get(i).moveType == Node.MoveType.PARKOUR) {
                return true;
            }
        }
        return false;
    }

    private Node.MoveType recoveryMoveType(List<Node> path, int pursuitSegment) {
        if (isParkourRecoveryWindow(path, pursuitSegment)) {
            return Node.MoveType.PARKOUR;
        }
        if (path == null || path.isEmpty()) {
            return Node.MoveType.WALK;
        }
        int index = Math.max(0, Math.min(pursuitSegment, path.size() - 1));
        return path.get(index).moveType;
    }

    private boolean isAtGoal(Vec3 playerPos) {
        double dx = (goalX + goalCenterX) - playerPos.x;
        double dz = (goalZ + goalCenterZ) - playerPos.z;
        return Math.sqrt(dx * dx + dz * dz) <= PathfinderSettings.instance().goalReachedHDist.value()
            && Math.abs(goalY - playerPos.y) <= PathfinderSettings.instance().goalReachedVDist.value();
    }

    private void tickGoalCoasting(Minecraft mc, Vec3 playerPos) {
        if (!finishAtPathEnd && !isAtGoal(playerPos)) {
            triggerReplan(mc, false, true, "path ended before final goal");
            return;
        }
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
                || System.currentTimeMillis() - coastStartTime > PathfinderSettings.instance().coastTimeoutMs.value()) {
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
