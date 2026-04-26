package fr.riege.ebsl.pathfinding;

import fr.riege.ebsl.pathfinding.execution.ExecutionOptions;

final class WalkExecutionOptions {
    private static final double DEFAULT_PRECISE_GOAL_TOLERANCE = ExecutionOptions.DEFAULT_TOLERANCE;

    private Runnable onFinished;
    private Runnable onFailed;
    private boolean allowReplan;
    private double preciseGoalTolerance;
    private double stickySneakDistance;
    private double goalCenterX;
    private double goalCenterZ;
    private boolean sneakLatched;
    private boolean allowJumps;
    private boolean allowRotation;
    private boolean exactGoalCentering;

    WalkExecutionOptions() {
        reset();
    }

    void reset() {
        onFinished = null;
        onFailed = null;
        allowReplan = true;
        preciseGoalTolerance = DEFAULT_PRECISE_GOAL_TOLERANCE;
        stickySneakDistance = -1.0;
        goalCenterX = 0.5;
        goalCenterZ = 0.5;
        sneakLatched = false;
        allowJumps = true;
        allowRotation = true;
        exactGoalCentering = false;
    }

    void configure(Runnable onFinished, Runnable onFailed, boolean allowReplan, double preciseGoalTolerance) {
        this.onFinished = onFinished;
        this.onFailed = onFailed;
        this.allowReplan = allowReplan;
        this.preciseGoalTolerance = preciseGoalTolerance;
        exactGoalCentering = preciseGoalTolerance != DEFAULT_PRECISE_GOAL_TOLERANCE;
        stickySneakDistance = exactGoalCentering ? 5.0 : -1.0;
    }

    ExecutionOptions snapshot() {
        return new ExecutionOptions(
            allowReplan, allowJumps, allowRotation, exactGoalCentering,
            stickySneakDistance, sneakLatched, goalCenterX, goalCenterZ, preciseGoalTolerance);
    }

    boolean isPreciseExecution() {
        return exactGoalCentering || preciseGoalTolerance != DEFAULT_PRECISE_GOAL_TOLERANCE;
    }

    Runnable onFinished() {
        return onFinished;
    }

    Runnable onFailed() {
        return onFailed;
    }

    void setGoalCenterOffsets(double goalCenterX, double goalCenterZ) {
        this.goalCenterX = goalCenterX;
        this.goalCenterZ = goalCenterZ;
    }

    void setAllowJumps(boolean allowJumps) {
        this.allowJumps = allowJumps;
    }

    void setAllowRotation(boolean allowRotation) {
        this.allowRotation = allowRotation;
    }

    void setAllowReplan(boolean allowReplan) {
        this.allowReplan = allowReplan;
    }

    void setExactGoalCentering(boolean exactGoalCentering) {
        this.exactGoalCentering = exactGoalCentering;
    }
}
