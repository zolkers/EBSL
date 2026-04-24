package fr.riege.ebsl.pathfinding;

import fr.riege.ebsl.pathfinding.pathfinder.AStarPathfinder;
import net.minecraft.world.phys.Vec3;

import java.util.List;

final class LongRangePathSession {
    private static final double SEGMENT_RECALC_RATIO = 0.70;
    private static final double EARLY_SEGMENT_RECALC_RATIO = 0.55;
    private static final double PREPARE_REMAINING_DISTANCE = 45.0;
    private static final double EMERGENCY_REMAINING_DISTANCE = 14.0;
    private static final double FINAL_GOAL_XZ_TOLERANCE = 1.75;
    private static final double MAX_SEGMENT_DISTANCE = 150.0;
    private static final long SEGMENT_RETRY_COOLDOWN_MS = 1500;
    private static final int PLAYER_START_AFTER_FAILURES = 2;
    private static final double PLAYER_START_RECOVERY_RATIO = 0.90;

    private boolean active;
    private int finalGoalX;
    private int finalGoalZ;
    private int currentSegmentGoalX;
    private int currentSegmentGoalZ;
    private boolean currentSegmentNeedsContinuation;
    private boolean currentSegmentPartial;
    private int currentSegmentId;
    private int calculationSegmentId = -1;
    private boolean segmentCalculationInFlight;
    private AStarPathfinder backgroundPathfinder;
    private PendingSegment preparedSegment;
    private long nextRetryAfterMs;
    private int failedSegmentCalculations;

    void start(int finalGoalX, int finalGoalZ) {
        clear();
        active = true;
        this.finalGoalX = finalGoalX;
        this.finalGoalZ = finalGoalZ;
    }

    void clear() {
        active = false;
        currentSegmentGoalX = 0;
        currentSegmentGoalZ = 0;
        currentSegmentNeedsContinuation = false;
        currentSegmentPartial = false;
        currentSegmentId = 0;
        calculationSegmentId = -1;
        segmentCalculationInFlight = false;
        preparedSegment = null;
        nextRetryAfterMs = 0;
        failedSegmentCalculations = 0;
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
        if (distance <= MAX_SEGMENT_DISTANCE) {
            return new SegmentGoal(finalGoalX, finalGoalZ, false);
        }

        double scale = MAX_SEGMENT_DISTANCE / Math.max(distance, 1.0e-6);
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

    void onSegmentStarted(int goalX, int goalZ, boolean needsContinuation, boolean partial) {
        currentSegmentGoalX = goalX;
        currentSegmentGoalZ = goalZ;
        currentSegmentNeedsContinuation = needsContinuation;
        currentSegmentPartial = partial;
        currentSegmentId++;
        calculationSegmentId = -1;
        preparedSegment = null;
        nextRetryAfterMs = 0;
        failedSegmentCalculations = 0;
    }

    SegmentQueueDecision queueDecision(double progressRatio, double remainingDistance, boolean walkExecutionDone, long now) {
        if (!active
            || !currentSegmentNeedsContinuation
            || segmentCalculationInFlight
            || preparedSegment != null
            || now < nextRetryAfterMs) {
            return SegmentQueueDecision.NONE;
        }
        if (walkExecutionDone) {
            return SegmentQueueDecision.EMERGENCY_FROM_PLAYER;
        }
        if (remainingDistance <= EMERGENCY_REMAINING_DISTANCE) {
            return SegmentQueueDecision.NORMAL;
        }
        if (progressRatio >= EARLY_SEGMENT_RECALC_RATIO || remainingDistance <= PREPARE_REMAINING_DISTANCE) {
            return SegmentQueueDecision.NORMAL;
        }
        return SegmentQueueDecision.NONE;
    }

    double recalcThresholdRatio() {
        return SEGMENT_RECALC_RATIO;
    }

    boolean shouldUsePlayerRecoveryStart(double progressRatio, boolean walkExecutionDone) {
        return walkExecutionDone
            || currentSegmentPartial
            || (failedSegmentCalculations >= PLAYER_START_AFTER_FAILURES
            && progressRatio >= PLAYER_START_RECOVERY_RATIO);
    }

    int markSegmentCalculationStarted(AStarPathfinder pathfinder) {
        segmentCalculationInFlight = true;
        calculationSegmentId = currentSegmentId;
        backgroundPathfinder = pathfinder;
        return calculationSegmentId;
    }

    void setPreparedSegment(PendingSegment preparedSegment) {
        this.preparedSegment = preparedSegment;
        this.segmentCalculationInFlight = false;
        this.backgroundPathfinder = null;
        this.nextRetryAfterMs = 0;
        this.failedSegmentCalculations = 0;
    }

    void markSegmentCalculationFailed(long now) {
        segmentCalculationInFlight = false;
        calculationSegmentId = -1;
        backgroundPathfinder = null;
        preparedSegment = null;
        nextRetryAfterMs = now + SEGMENT_RETRY_COOLDOWN_MS;
        failedSegmentCalculations++;
    }

    void forceNextCalculationFromPlayer() {
        if (backgroundPathfinder != null) {
            backgroundPathfinder.abort();
        }
        segmentCalculationInFlight = false;
        calculationSegmentId = -1;
        backgroundPathfinder = null;
        preparedSegment = null;
        nextRetryAfterMs = 0;
        failedSegmentCalculations = PLAYER_START_AFTER_FAILURES;
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
        return Math.sqrt(dx * dx + dz * dz) <= FINAL_GOAL_XZ_TOLERANCE;
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

    record PendingSegment(
        List<Node> path,
        int goalX,
        int goalY,
        int goalZ,
        boolean needsContinuation,
        boolean partial,
        long exploredCount,
        SegmentAttachment attachment
    ) {
    }
}
