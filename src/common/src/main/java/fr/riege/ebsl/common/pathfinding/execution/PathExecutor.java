package fr.riege.ebsl.common.pathfinding.execution;

import fr.riege.ebsl.common.layer.IPhysicsLayer;
import fr.riege.ebsl.common.layer.IPlayerLayer;
import fr.riege.ebsl.common.layer.IWorldLayer;
import fr.riege.ebsl.common.math.Vec3d;
import fr.riege.ebsl.common.pathfinding.Node;
import fr.riege.ebsl.common.pathfinding.check.PathCheckContext;
import fr.riege.ebsl.common.pathfinding.check.PathCheckRegistry;
import fr.riege.ebsl.common.pathfinding.check.PathCheckResult;
import fr.riege.ebsl.common.pathfinding.check.PathProximitySnapshot;
import fr.riege.ebsl.common.pathfinding.movement.WalkabilityChecker;
import fr.riege.ebsl.common.pathfinding.rotation.RotationExecutor;
import fr.riege.ebsl.common.pathfinding.settings.PathfinderSettings;

import java.util.List;

public final class PathExecutor {
    public enum State { IDLE, WALKING, REPLANNING, FINISHED, FAILED }

    private final IWorldLayer world;
    private final IPlayerLayer player;
    private final IPhysicsLayer physics;
    private final WalkabilityChecker checker;
    private final PathTracker pathTracker = new PathTracker();
    private final PathRecoveryController recoveryController = new PathRecoveryController();
    private final RotationExecutor rotationExecutor;
    private final PathRotationController rotationController;
    private final WalkMovementController movementController;

    private State state = State.IDLE;
    private boolean precise;
    private long lastReplanTime;
    private long coastStartTime;
    private long lastSmartCutoffTime;
    private int jumpCooldown;
    private int goalX, goalY, goalZ;
    private double goalCenterX = 0.5;
    private double goalCenterZ = 0.5;
    private boolean allowReplan = true;
    private boolean allowJumps = true;
    private boolean allowRotation = true;
    private boolean allowSneak = true;
    private boolean exactGoalCentering;
    private double stickySneakDistance = -1.0;
    private boolean sneakLatched;
    private double preciseGoalTolerance = ExecutionOptions.DEFAULT_TOLERANCE;
    private Runnable onFinished;
    private boolean finishAtPathEnd = true;
    private boolean replanFromPlayerRequested;
    private PathRepairRequest repairRequest;
    private String lastRepairReason = "";
    private long lastRepairReasonTime;
    private Node.MoveType lastKnownMoveType = Node.MoveType.WALK;

    public PathExecutor(IWorldLayer world, IPlayerLayer player, IPhysicsLayer physics) {
        this.world = world;
        this.player = player;
        this.physics = physics;
        this.checker = new WalkabilityChecker(world);
        this.rotationExecutor = new RotationExecutor(player, physics);
        this.rotationController = new PathRotationController(world, player, rotationExecutor);
        this.movementController = new WalkMovementController(world, player, physics, checker);
    }

    public void start(List<Node> path, int goalX, int goalY, int goalZ, boolean precise) {
        start(new ExecutionPlan(path, goalX, goalY, goalZ, precise, null));
    }

    public void start(ExecutionPlan plan) {
        this.goalX = plan.goalX();
        this.goalY = plan.goalY();
        this.goalZ = plan.goalZ();
        this.precise = plan.precise();
        this.onFinished = plan.onFinished();
        ExecutionOptions opts = plan.options();
        this.allowReplan = opts.allowReplan();
        this.allowJumps = opts.allowJumps();
        this.allowRotation = opts.allowRotation();
        this.allowSneak = opts.allowSneak();
        this.exactGoalCentering = opts.exactGoalCentering();
        this.stickySneakDistance = allowSneak ? opts.stickySneakDistance() : -1.0;
        this.preciseGoalTolerance = Math.max(0.01, opts.preciseGoalTolerance());
        this.goalCenterX = opts.goalCenterX();
        this.goalCenterZ = opts.goalCenterZ();
        this.sneakLatched = allowSneak && opts.sneakLatched();
        this.finishAtPathEnd = plan.finishAtPathEnd();
        resetTransientState();
        pathTracker.start(plan.path());
        movementController.setPath(plan.path());
        rotationController.rebuild(plan.path());
        state = State.WALKING;
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
    public int getCamTargetIdx() { return rotationController.getCamTargetIdx(); }
    public List<Node> getPathSnapshot() { return pathTracker.getPathSnapshot(); }
    public boolean isSneakLatched() { return sneakLatched; }
    public void setSneakLatched(boolean sneakLatched) { this.sneakLatched = sneakLatched; }
    public double getProgressRatio(Vec3d playerPos) { return pathTracker.getProgressRatio(playerPos); }
    public double getRemainingDistance(Vec3d playerPos) { return pathTracker.getRemainingDistance(playerPos); }
    public Node getNodeAtRatio(double ratio) { return pathTracker.getNodeAtRatio(ratio); }

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

    void setJumpCooldown(int jumpCooldown) {
        this.jumpCooldown = Math.max(0, jumpCooldown);
    }

    public void continueWith(List<Node> continuationPath, int goalX, int goalY, int goalZ) {
        if (continuationPath == null || continuationPath.isEmpty()) return;
        this.goalX = goalX;
        this.goalY = goalY;
        this.goalZ = goalZ;
        pathTracker.continueWith(continuationPath);
        movementController.setPath(pathTracker.getPath());
        rotationController.rebuild(pathTracker.getPath());
    }

    public void trimAndContinueWith(double trimRatio, List<Node> newPath, int goalX, int goalY, int goalZ) {
        if (newPath == null || newPath.isEmpty()) return;
        this.goalX = goalX;
        this.goalY = goalY;
        this.goalZ = goalZ;
        pathTracker.trimAndContinueWith(trimRatio, newPath);
        movementController.setPath(pathTracker.getPath());
        rotationController.rebuild(pathTracker.getPath());
    }

    public void tick() {
        if (state != State.WALKING && state != State.REPLANNING) return;
        if (!player.isAlive()) {
            stop();
            return;
        }
        if (pauseForBlockingClientState()) return;

        if (player.onGround() || player.isInWater()) {
            jumpCooldown = Math.max(0, jumpCooldown - 1);
        }

        Vec3d playerPos = player.position();
        List<Node> path = pathTracker.getPath();
        if (path == null || path.isEmpty()) {
            finish();
            return;
        }
        if (isAtGoal(playerPos)) {
            finish();
            return;
        }

        double distMoved = pathTracker.noteMovementProgress(playerPos, PathfinderSettings.instance().stuckDistThreshold.value());
        long now = System.currentTimeMillis();
        pathTracker.advancePursuit(playerPos, now);
        pathTracker.computeAndTrackPathProgress(playerPos, PathfinderSettings.instance().pathProgressEpsilon.value(), now);

        PathProximitySnapshot proximity = pathTracker.analyzePathProximity(playerPos);
        pathTracker.updateSevereOffPathState(proximity, now);
        if (handlePathChecks(playerPos, path, proximity, now)) return;
        path = pathTracker.getPath();
        proximity = pathTracker.analyzePathProximity(playerPos);

        if (pathTracker.getPursuitSegment() >= path.size() - 1) {
            tickGoalCoasting(playerPos);
            return;
        }

        PathProgressSnapshot progress = new PathProgressSnapshot(
            distMoved,
            now - pathTracker.getLastProgressTime(),
            now - pathTracker.getLastPathProgressTime(),
            proximity);
        PathRecoveryController.RecoveryDecision recovery = recoveryController.update(
            player,
            physics,
            playerPos,
            progress,
            allowReplan,
            now - lastReplanTime > PathfinderSettings.instance().replanCooldownMs.value(),
            jumpCooldown,
            recoveryMoveType(path, pathTracker.getPursuitSegment()));
        if (recovery.noteProgress()) pathTracker.noteMovementProgress(playerPos, 0.0);
        if (recovery.action() == PathRecoveryController.Action.REPLAN_FROM_PLAYER) {
            triggerReplan(false, true, recovery.reason());
            return;
        }
        if (recovery.action() == PathRecoveryController.Action.REPAIR_TO_SEGMENT) {
            triggerRepair(chooseLocalRepairSegment(proximity), recovery.reason());
            return;
        }
        if (recovery.action() == PathRecoveryController.Action.TICK_HANDLED) return;
        if (recovery.action() == PathRecoveryController.Action.ALIGN_TO_PATH) {
            Vec3d correction = pathTracker.computeCorrectionToPath(playerPos, proximity);
            InputApplier.applyCornerAlignment(player, physics, correction.x(), correction.z());
            return;
        }
        boolean recoveryJump = recovery.action() == PathRecoveryController.Action.RECOVERY_JUMP;
        if (recoveryJump) {
            jumpCooldown = PathfinderSettings.instance().jumpCooldownTicks.value();
        }

        Node movementWp = pathTracker.getMovementWaypoint();
        if (allowRotation) {
            rotationController.updateRotation(playerPos, path, pathTracker.getPursuitSegment(), null);
            rotationController.tickExecutor();
        }
        double finalDx = (goalX + goalCenterX) - playerPos.x();
        double finalDy = goalY - playerPos.y();
        double finalDz = (goalZ + goalCenterZ) - playerPos.z();
        double distToFinal = Math.sqrt(finalDx * finalDx + finalDy * finalDy + finalDz * finalDz);

        if (allowSneak && !sneakLatched && stickySneakDistance > 0 && distToFinal <= stickySneakDistance) {
            sneakLatched = true;
        }
        if (movementWp != null) {
            movementController.apply(this, playerPos, movementWp, distToFinal,
                allowJumps && !recoveryJump, allowSneak && sneakLatched,
                pathTracker.getPursuitSegment(), jumpCooldown, pathTracker.getLastProgressTime());
        }
        physics.setSneak(allowSneak && sneakLatched);
    }

    public void stop() {
        state = State.IDLE;
        rotationController.stop();
        releaseAll();
    }

    public void releaseAll() {
        InputApplier.releaseAll(physics, allowSneak && sneakLatched);
    }

    private void resetTransientState() {
        jumpCooldown = 0;
        coastStartTime = 0;
        lastSmartCutoffTime = 0;
        replanFromPlayerRequested = false;
        repairRequest = null;
        lastRepairReason = "";
        lastRepairReasonTime = 0;
        lastKnownMoveType = Node.MoveType.WALK;
        recoveryController.reset();
        rotationController.reset();
    }

    private boolean pauseForBlockingClientState() {
        if (player.isFlying()) {
            releaseAll();
            physics.setSneak(true);
            return true;
        }
        return false;
    }

    private boolean handlePathChecks(Vec3d playerPos, List<Node> path, PathProximitySnapshot proximity, long now) {
        PathCheckResult pathCheckResult = PathCheckRegistry.evaluate(new PathCheckContext(
            playerPos,
            path,
            pathTracker.getPursuitSegment(),
            goalX,
            goalY,
            goalZ,
            pathTracker.getSevereOffPathDuration(now),
            proximity));

        if (pathCheckResult.isForceReplan()) {
            triggerReplan(true, true, pathCheckResult.reason());
            return true;
        }
        if (pathCheckResult.isRepairToSegment()) {
            triggerRepair(pathCheckResult.cutoffSegmentIndex(), pathCheckResult.reason());
            return true;
        }
        if (!pathCheckResult.isCutoff()
            || now - lastSmartCutoffTime < PathfinderSettings.instance().smartCutoffCooldownMs.value()
            || !pathTracker.applySmartCutoff(pathCheckResult.cutoffSegmentIndex())) {
            return false;
        }
        lastSmartCutoffTime = now;
        return false;
    }

    private void tickGoalCoasting(Vec3d playerPos) {
        double dx = (goalX + goalCenterX) - playerPos.x();
        double dy = goalY - playerPos.y();
        double dz = (goalZ + goalCenterZ) - playerPos.z();
        double hDist = Math.sqrt(dx * dx + dz * dz);
        double vDist = Math.abs(dy);
        double tolerance = precise ? preciseGoalTolerance : PathfinderSettings.instance().goalReachedHDist.value();
        if (hDist <= tolerance && vDist <= PathfinderSettings.instance().goalReachedVDist.value()) {
            finish();
            return;
        }
        if (!finishAtPathEnd) {
            finish();
            return;
        }
        if (coastStartTime == 0) coastStartTime = System.currentTimeMillis();
        if (System.currentTimeMillis() - coastStartTime > PathfinderSettings.instance().coastTimeoutMs.value()) {
            finish();
            return;
        }
        InputApplier.applyGoalCentering(player, physics, dx, dz);
    }

    private boolean isAtGoal(Vec3d playerPos) {
        double dx = (goalX + goalCenterX) - playerPos.x();
        double dy = goalY - playerPos.y();
        double dz = (goalZ + goalCenterZ) - playerPos.z();
        double hDist = Math.sqrt(dx * dx + dz * dz);
        double tolerance = precise ? preciseGoalTolerance : PathfinderSettings.instance().goalReachedHDist.value();
        return hDist <= tolerance && Math.abs(dy) <= PathfinderSettings.instance().goalReachedVDist.value();
    }

    private Node.MoveType recoveryMoveType(List<Node> path, int pursuitSegment) {
        int idx = Math.max(0, Math.min(path.size() - 1, pursuitSegment + 1));
        Node.MoveType type = path.get(idx).moveType;
        return type == null ? Node.MoveType.WALK : type;
    }

    private void triggerReplan(boolean force, boolean fromPlayer, String reason) {
        releaseAll();
        lastReplanTime = System.currentTimeMillis();
        pathTracker.markReplanTriggered(lastReplanTime);
        replanFromPlayerRequested = fromPlayer;
        state = State.REPLANNING;
    }

    private void triggerRepair(int segmentIndex, String reason) {
        long now = System.currentTimeMillis();
        if (reason.equals(lastRepairReason) && now - lastRepairReasonTime < 750) {
            triggerReplan(true, true, reason);
            return;
        }
        repairRequest = pathTracker.createRepairRequest(segmentIndex, reason, goalX, goalY, goalZ).orElse(null);
        if (repairRequest == null) {
            triggerReplan(true, true, reason);
            return;
        }
        releaseAll();
        state = State.REPLANNING;
    }

    private int chooseLocalRepairSegment(PathProximitySnapshot proximity) {
        int lookahead = proximity.horizontalDistance() >= PathfinderSettings.instance().localRepairDriftThreshold.value()
            ? PathfinderSettings.instance().localRepairDriftLookahead.value()
            : PathfinderSettings.instance().localRepairLookahead.value();
        return Math.min(pathTracker.getPath().size() - 2, proximity.nearestSegmentIndex() + lookahead);
    }

    private void finish() {
        state = State.FINISHED;
        rotationController.stop();
        releaseAll();
        Runnable finished = onFinished;
        onFinished = null;
        if (finished != null) finished.run();
    }

}
