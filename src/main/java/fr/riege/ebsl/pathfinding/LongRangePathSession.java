package fr.riege.ebsl.pathfinding;

import fr.riege.ebsl.pathfinding.annotation.PathStatePersistence;
import fr.riege.ebsl.pathfinding.annotation.PathStateTransition;
import fr.riege.ebsl.pathfinding.annotation.PathingStage;
import fr.riege.ebsl.pathfinding.pathfinder.AStarPathfinder;
import net.minecraft.world.phys.Vec3;

import java.util.List;

@PathingStage(PathingStage.Stage.STATE_PERSISTENCE)
@PathStatePersistence(
    value = PathStatePersistence.Scope.LONG_RANGE_SESSION,
    reason = "Persists final XZ goal and prepared segment state across partial path replans.")
final class LongRangePathSession {
    @PathStatePersistence(PathStatePersistence.Scope.LONG_RANGE_SESSION)
    private boolean active;
    @PathStatePersistence(PathStatePersistence.Scope.LONG_RANGE_SESSION)
    private int finalGoalX;
    @PathStatePersistence(PathStatePersistence.Scope.LONG_RANGE_SESSION)
    private int finalGoalY;
    @PathStatePersistence(PathStatePersistence.Scope.LONG_RANGE_SESSION)
    private int finalGoalZ;
    @PathStatePersistence(PathStatePersistence.Scope.LONG_RANGE_SESSION)
    private GoalContract goalContract = GoalContract.XZ;
    @PathStatePersistence(PathStatePersistence.Scope.EXECUTION)
    private int currentSegmentGoalX;
    @PathStatePersistence(PathStatePersistence.Scope.EXECUTION)
    private int currentSegmentGoalZ;
    @PathStatePersistence(PathStatePersistence.Scope.EXECUTION)
    private boolean currentSegmentNeedsContinuation;
    private int currentSegmentId;
    private int calculationSegmentId = -1;
    private boolean segmentCalculationInFlight;
    private AStarPathfinder backgroundPathfinder;
    private PendingSegment preparedSegment;
    private long nextRetryAfterMs;
    private int failedSegmentCalculations;
    private boolean immediateSegmentQueueRequested;

    @PathStateTransition(PathStateTransition.Action.BEGIN)
    void start(int finalGoalX, int finalGoalZ) {
        start(finalGoalX, 0, finalGoalZ, GoalContract.XZ);
    }

    @PathStateTransition(PathStateTransition.Action.BEGIN)
    void startBlockGoal(int finalGoalX, int finalGoalY, int finalGoalZ) {
        start(finalGoalX, finalGoalY, finalGoalZ, GoalContract.XYZ);
    }

    private void start(int finalGoalX, int finalGoalY, int finalGoalZ, GoalContract goalContract) {
        clear();
        active = true;
        this.finalGoalX = finalGoalX;
        this.finalGoalY = finalGoalY;
        this.finalGoalZ = finalGoalZ;
        this.goalContract = goalContract;
    }

    @PathStateTransition(PathStateTransition.Action.CLEAR)
    void clear() {
        active = false;
        finalGoalY = 0;
        goalContract = GoalContract.XZ;
        currentSegmentGoalX = 0;
        currentSegmentGoalZ = 0;
        currentSegmentNeedsContinuation = false;
        currentSegmentId = 0;
        calculationSegmentId = -1;
        segmentCalculationInFlight = false;
        preparedSegment = null;
        nextRetryAfterMs = 0;
        failedSegmentCalculations = 0;
        immediateSegmentQueueRequested = false;
        if (backgroundPathfinder != null) {
            backgroundPathfinder.abort();
            backgroundPathfinder = null;
        }
    }

    boolean isActive() {
        return active;
    }

    boolean shouldKeepNavigationAlive() {
        return active
            && (currentSegmentNeedsContinuation || segmentCalculationInFlight || preparedSegment != null);
    }

    int finalGoalX() {
        return finalGoalX;
    }

    int finalGoalZ() {
        return finalGoalZ;
    }

    int finalGoalY() {
        return finalGoalY;
    }

    boolean requiresExactY() {
        return goalContract == GoalContract.XYZ;
    }

    int currentSegmentGoalX() {
        return currentSegmentGoalX;
    }

    int currentSegmentGoalZ() {
        return currentSegmentGoalZ;
    }

    int currentSegmentId() {
        return currentSegmentId;
    }

    SegmentGoal planSegmentGoal(double fromX, double fromZ) {
        double dx = finalGoalX + 0.5 - fromX;
        double dz = finalGoalZ + 0.5 - fromZ;
        double distance = Math.sqrt(dx * dx + dz * dz);
        double maxSegmentDistance = PathfinderConfig.MAX_SEGMENT_DISTANCE.get();
        if (distance <= maxSegmentDistance) {
            return new SegmentGoal(finalGoalX, finalGoalZ, false);
        }

        double scale = maxSegmentDistance / Math.max(distance, 1.0e-6);
        int segmentX = (int) Math.floor(fromX + dx * scale);
        int segmentZ = (int) Math.floor(fromZ + dz * scale);
        if (segmentX == finalGoalX && segmentZ == finalGoalZ) {
            return new SegmentGoal(finalGoalX, finalGoalZ, false);
        }
        return new SegmentGoal(segmentX, segmentZ, true);
    }

    void onSegmentStarted(int goalX, int goalZ, boolean needsContinuation) {
        onSegmentStarted(goalX, goalZ, needsContinuation, false);
    }

    @PathStateTransition(PathStateTransition.Action.REPLACE)
    void onSegmentStarted(int goalX, int goalZ, boolean needsContinuation, boolean partial) {
        currentSegmentGoalX = goalX;
        currentSegmentGoalZ = goalZ;
        currentSegmentNeedsContinuation = needsContinuation;
        currentSegmentId++;
        calculationSegmentId = -1;
        preparedSegment = null;
        nextRetryAfterMs = 0;
        failedSegmentCalculations = 0;
        immediateSegmentQueueRequested = partial;
    }

    @PathStateTransition(PathStateTransition.Action.PRESERVE)
    SegmentQueueDecision queueDecision(double progressRatio, double remainingDistance, boolean walkExecutionDone, long now) {
        if (!active
            || !currentSegmentNeedsContinuation
            || segmentCalculationInFlight
            || preparedSegment != null
            || now < nextRetryAfterMs) {
            return SegmentQueueDecision.NONE;
        }
        if (immediateSegmentQueueRequested) {
            immediateSegmentQueueRequested = false;
            return SegmentQueueDecision.NORMAL;
        }
        if (walkExecutionDone) {
            return SegmentQueueDecision.EMERGENCY_FROM_PLAYER;
        }
        if (remainingDistance <= PathfinderConfig.EMERGENCY_REMAINING_DISTANCE.get()) {
            return SegmentQueueDecision.NORMAL;
        }
        if (progressRatio >= PathfinderConfig.EARLY_SEGMENT_RECALC_RATIO.get()
            || remainingDistance <= PathfinderConfig.PREPARE_REMAINING_DISTANCE.get()) {
            return SegmentQueueDecision.NORMAL;
        }
        return SegmentQueueDecision.NONE;
    }

    double recalcThresholdRatio() {
        return PathfinderConfig.SEGMENT_RECALC_RATIO.get();
    }

    boolean shouldActivatePreparedSegment(double progressRatio, double remainingDistance, boolean walkExecutionDone) {
        return walkExecutionDone
            || progressRatio >= PathfinderConfig.SEGMENT_RECALC_RATIO.get()
            || remainingDistance <= PathfinderConfig.PREPARED_SWITCH_REMAINING_DISTANCE.get();
    }

    boolean shouldUsePlayerRecoveryStart(double progressRatio, boolean walkExecutionDone) {
        return walkExecutionDone
            || (failedSegmentCalculations >= PathfinderConfig.PLAYER_START_AFTER_FAILURES.get()
            && progressRatio >= PathfinderConfig.PLAYER_START_RECOVERY_RATIO.get());
    }

    @PathStateTransition(PathStateTransition.Action.REPLACE)
    int markSegmentCalculationStarted(AStarPathfinder pathfinder) {
        segmentCalculationInFlight = true;
        calculationSegmentId = currentSegmentId;
        backgroundPathfinder = pathfinder;
        return calculationSegmentId;
    }

    @PathStateTransition(PathStateTransition.Action.PRESERVE)
    void setPreparedSegment(PendingSegment preparedSegment) {
        this.preparedSegment = preparedSegment;
        this.segmentCalculationInFlight = false;
        this.backgroundPathfinder = null;
        this.nextRetryAfterMs = 0;
        this.failedSegmentCalculations = 0;
    }

    @PathStateTransition(PathStateTransition.Action.RESET)
    void markSegmentCalculationFailed(long now) {
        segmentCalculationInFlight = false;
        calculationSegmentId = -1;
        backgroundPathfinder = null;
        preparedSegment = null;
        nextRetryAfterMs = now + PathfinderConfig.SEGMENT_RETRY_COOLDOWN_MS.get();
        failedSegmentCalculations++;
    }

    @PathStateTransition(PathStateTransition.Action.REPLACE)
    void forceNextCalculationFromPlayer() {
        if (backgroundPathfinder != null) {
            backgroundPathfinder.abort();
        }
        segmentCalculationInFlight = false;
        calculationSegmentId = -1;
        backgroundPathfinder = null;
        preparedSegment = null;
        nextRetryAfterMs = 0;
        failedSegmentCalculations = PathfinderConfig.PLAYER_START_AFTER_FAILURES.get();
        immediateSegmentQueueRequested = true;
    }

    boolean isCurrentCalculation(int segmentId) {
        return segmentCalculationInFlight && calculationSegmentId == segmentId;
    }

    boolean hasPreparedSegment() {
        return preparedSegment != null;
    }

    boolean hasSegmentCalculationInFlight() {
        return segmentCalculationInFlight;
    }

    PendingSegment preparedSegment() {
        return preparedSegment;
    }

    boolean isFinalGoalReached(Vec3 playerPos) {
        double dx = (finalGoalX + 0.5) - playerPos.x;
        double dz = (finalGoalZ + 0.5) - playerPos.z;
        if (Math.sqrt(dx * dx + dz * dz) > PathfinderConfig.FINAL_GOAL_XZ_TOLERANCE.get()) {
            return false;
        }
        return !requiresExactY() || Math.abs(finalGoalY - playerPos.y) <= 2.0;
    }

    boolean isFinalSegmentGoal(int x, int y, int z) {
        return x == finalGoalX
            && z == finalGoalZ
            && (!requiresExactY() || y == finalGoalY);
    }

    record SegmentGoal(int x, int z, boolean segmented) {
    }

    enum SegmentAttachment {
        MERGE_WITH_CURRENT,
        REPLACE_FROM_PLAYER
    }

    enum SegmentQueueDecision {
        NONE,
        NORMAL,
        EMERGENCY_FROM_PLAYER
    }

    enum GoalContract {
        XZ,
        XYZ
    }

    record PendingSegment(
        List<Node> path,
        int goalX,
        int goalY,
        int goalZ,
        boolean needsContinuation,
        boolean partial,
        long exploredCount,
        SegmentAttachment attachment,
        boolean rollingHorizon
    ) {
    }
}
