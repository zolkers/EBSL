package fr.riege.ebsl.common.pathfinding;

import fr.riege.ebsl.common.math.Vec3d;
import fr.riege.ebsl.common.pathfinding.pathfinder.AStarPathfinder;
import fr.riege.ebsl.common.pathfinding.settings.PathfinderSettings;

import java.util.List;

public final class LongRangePathSession {
    private boolean active;
    private int finalGoalX;
    private int finalGoalY;
    private int finalGoalZ;
    private GoalContract goalContract = GoalContract.XZ;
    private int currentSegmentGoalX;
    private int currentSegmentGoalY;
    private int currentSegmentGoalZ;
    private boolean currentSegmentNeedsContinuation;
    private int currentSegmentId;
    private int calculationSegmentId = -1;
    private boolean segmentCalculationInFlight;
    private AStarPathfinder backgroundPathfinder;
    private PendingSegment preparedSegment;
    private long nextRetryAfterMs;
    private int failedSegmentCalculations;
    private boolean immediateSegmentQueueRequested;

    public void start(int finalGoalX, int finalGoalZ) {
        start(finalGoalX, 0, finalGoalZ, GoalContract.XZ);
    }

    public void startBlockGoal(int finalGoalX, int finalGoalY, int finalGoalZ) {
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

    public void clear() {
        active = false;
        finalGoalY = 0;
        goalContract = GoalContract.XZ;
        currentSegmentGoalX = 0;
        currentSegmentGoalY = 0;
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

    public boolean isActive() {
        return active;
    }

    public boolean shouldKeepNavigationAlive() {
        return active && (currentSegmentNeedsContinuation || segmentCalculationInFlight || preparedSegment != null);
    }

    public int finalGoalX() { return finalGoalX; }
    public int finalGoalY() { return finalGoalY; }
    public int finalGoalZ() { return finalGoalZ; }
    public boolean requiresExactY() { return goalContract == GoalContract.XYZ; }
    public int currentSegmentGoalX() { return currentSegmentGoalX; }
    public int currentSegmentGoalY() { return currentSegmentGoalY; }
    public int currentSegmentGoalZ() { return currentSegmentGoalZ; }

    public SegmentGoal planSegmentGoal(double fromX, double fromZ) {
        double dx = finalGoalX + 0.5 - fromX;
        double dz = finalGoalZ + 0.5 - fromZ;
        double distance = Math.sqrt(dx * dx + dz * dz);
        double maxSegmentDistance = PathfinderSettings.instance().maxSegmentDistance.value();
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

    public void onSegmentStarted(int goalX, int goalZ, boolean needsContinuation) {
        onSegmentStarted(goalX, 0, goalZ, needsContinuation, false);
    }

    public void onSegmentStarted(int goalX, int goalZ, boolean needsContinuation, boolean partial) {
        onSegmentStarted(goalX, 0, goalZ, needsContinuation, partial);
    }

    public void onSegmentStarted(int goalX, int goalY, int goalZ, boolean needsContinuation, boolean partial) {
        currentSegmentGoalX = goalX;
        currentSegmentGoalY = goalY;
        currentSegmentGoalZ = goalZ;
        currentSegmentNeedsContinuation = needsContinuation;
        currentSegmentId++;
        calculationSegmentId = -1;
        preparedSegment = null;
        nextRetryAfterMs = 0;
        failedSegmentCalculations = 0;
        immediateSegmentQueueRequested = partial;
    }

    public void markCompleted() {
        active = false;
        currentSegmentNeedsContinuation = false;
        calculationSegmentId = -1;
        segmentCalculationInFlight = false;
        preparedSegment = null;
        immediateSegmentQueueRequested = false;
        if (backgroundPathfinder != null) {
            backgroundPathfinder.abort();
            backgroundPathfinder = null;
        }
    }

    public SegmentQueueDecision queueDecision(double progressRatio, double remainingDistance, boolean walkExecutionDone, long now) {
        if (!active || !currentSegmentNeedsContinuation || segmentCalculationInFlight || preparedSegment != null || now < nextRetryAfterMs) {
            return SegmentQueueDecision.NONE;
        }
        if (immediateSegmentQueueRequested) {
            immediateSegmentQueueRequested = false;
            return SegmentQueueDecision.NORMAL;
        }
        if (walkExecutionDone) {
            return SegmentQueueDecision.EMERGENCY_FROM_PLAYER;
        }
        if (remainingDistance <= PathfinderSettings.instance().emergencyRemainingDistance.value()) {
            return SegmentQueueDecision.NORMAL;
        }
        if (progressRatio >= PathfinderSettings.instance().earlySegmentRecalcRatio.value()
            || remainingDistance <= PathfinderSettings.instance().prepareRemainingDistance.value()) {
            return SegmentQueueDecision.NORMAL;
        }
        return SegmentQueueDecision.NONE;
    }

    public boolean shouldActivatePreparedSegment(double progressRatio, double remainingDistance, boolean walkExecutionDone) {
        return walkExecutionDone
            || progressRatio >= PathfinderSettings.instance().segmentRecalcRatio.value()
            || remainingDistance <= PathfinderSettings.instance().preparedSwitchRemainingDistance.value();
    }

    public boolean shouldUsePlayerRecoveryStart(double progressRatio, boolean walkExecutionDone) {
        return walkExecutionDone
            || (failedSegmentCalculations >= PathfinderSettings.instance().playerStartAfterFailures.value()
            && progressRatio >= PathfinderSettings.instance().playerStartRecoveryRatio.value());
    }

    public int markSegmentCalculationStarted(AStarPathfinder pathfinder) {
        segmentCalculationInFlight = true;
        calculationSegmentId = currentSegmentId;
        backgroundPathfinder = pathfinder;
        return calculationSegmentId;
    }

    public void setPreparedSegment(PendingSegment preparedSegment) {
        this.preparedSegment = preparedSegment;
        this.segmentCalculationInFlight = false;
        this.backgroundPathfinder = null;
        this.nextRetryAfterMs = 0;
        this.failedSegmentCalculations = 0;
    }

    public void markSegmentCalculationFailed(long now) {
        segmentCalculationInFlight = false;
        calculationSegmentId = -1;
        backgroundPathfinder = null;
        preparedSegment = null;
        nextRetryAfterMs = now + PathfinderSettings.instance().segmentRetryCooldownMs.value();
        failedSegmentCalculations++;
    }

    public void forceNextCalculationFromPlayer() {
        if (backgroundPathfinder != null) {
            backgroundPathfinder.abort();
        }
        segmentCalculationInFlight = false;
        calculationSegmentId = -1;
        backgroundPathfinder = null;
        preparedSegment = null;
        nextRetryAfterMs = 0;
        failedSegmentCalculations = PathfinderSettings.instance().playerStartAfterFailures.value();
        immediateSegmentQueueRequested = true;
    }

    public boolean isCurrentCalculation(int segmentId) {
        return segmentCalculationInFlight && calculationSegmentId == segmentId;
    }

    public boolean hasPreparedSegment() { return preparedSegment != null; }
    public boolean hasSegmentCalculationInFlight() { return segmentCalculationInFlight; }
    public PendingSegment preparedSegment() { return preparedSegment; }

    public boolean isFinalGoalReached(Vec3d playerPos) {
        double dx = (finalGoalX + 0.5) - playerPos.x();
        double dz = (finalGoalZ + 0.5) - playerPos.z();
        if (Math.sqrt(dx * dx + dz * dz) > PathfinderSettings.instance().finalGoalXzTolerance.value()) {
            return false;
        }
        return !requiresExactY() || Math.abs(finalGoalY - playerPos.y()) <= 2.0;
    }

    public boolean isFinalSegmentGoal(int x, int y, int z) {
        return x == finalGoalX && z == finalGoalZ && (!requiresExactY() || y == finalGoalY);
    }

    public record SegmentGoal(int x, int z, boolean segmented) {
    }

    public enum SegmentAttachment {
        MERGE_WITH_CURRENT,
        REPLACE_FROM_PLAYER
    }

    public enum SegmentQueueDecision {
        NONE,
        NORMAL,
        EMERGENCY_FROM_PLAYER
    }

    public enum GoalContract {
        XZ,
        XYZ
    }

    public record PendingSegment(
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
