package fr.riege.ebsl.pathfinding;

import fr.riege.ebsl.pathfinding.execution.PathExecutor;

final class WalkExecutionOptions {
    private static final double DEFAULT_PRECISE_GOAL_TOLERANCE = 0.5;

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
        stickySneakDistance = 5.0;
        exactGoalCentering = preciseGoalTolerance != DEFAULT_PRECISE_GOAL_TOLERANCE;
    }

    void applyTo(PathExecutor executor) {
        executor.setAllowRotation(allowRotation);
        executor.setAllowReplan(allowReplan);
        executor.setAllowJumps(allowJumps);
        executor.setExactGoalCentering(exactGoalCentering);
        executor.setStickySneakDistance(stickySneakDistance);
        executor.setSneakLatched(sneakLatched);
        executor.setGoalCenterOffsets(goalCenterX, goalCenterZ);
        executor.setPreciseGoalTolerance(preciseGoalTolerance);
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

    boolean isSneakLatched() {
        return sneakLatched;
    }

    void setSneakLatched(boolean sneakLatched) {
        this.sneakLatched = sneakLatched;
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
