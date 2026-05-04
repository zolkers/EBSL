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
    private boolean allowParkour;
    private boolean allowSneak;
    private boolean allowJump;
    private boolean allowFall;
    private boolean allowClimb;
    private boolean allowSwim;
    private boolean allowStepUp;
    private boolean allowWalkDiagonal;
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
        allowParkour = true;
        allowSneak = true;
        allowJump = true;
        allowFall = true;
        allowClimb = true;
        allowSwim = true;
        allowStepUp = true;
        allowWalkDiagonal = true;
        exactGoalCentering = false;
    }

    void configure(Runnable onFinished, Runnable onFailed, boolean allowReplan, double preciseGoalTolerance) {
        configure(onFinished, onFailed, allowReplan, true, true, true, preciseGoalTolerance);
    }

    void configure(Runnable onFinished, Runnable onFailed, boolean allowReplan, boolean allowParkour,
                   boolean allowRotation, boolean allowSneak, double preciseGoalTolerance) {
        this.onFinished = onFinished;
        this.onFailed = onFailed;
        this.allowReplan = allowReplan;
        this.allowParkour = allowParkour;
        this.allowRotation = allowRotation;
        this.allowSneak = allowSneak;
        this.preciseGoalTolerance = preciseGoalTolerance;
        exactGoalCentering = preciseGoalTolerance != DEFAULT_PRECISE_GOAL_TOLERANCE;
        stickySneakDistance = exactGoalCentering && allowSneak ? 5.0 : -1.0;
        if (!allowSneak) {
            sneakLatched = false;
        }
    }

    ExecutionOptions snapshot() {
        return new ExecutionOptions(
            allowReplan, allowJumps, allowRotation, allowSneak, exactGoalCentering,
            stickySneakDistance, allowSneak && sneakLatched, goalCenterX, goalCenterZ, preciseGoalTolerance);
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

    boolean allowParkour() {
        return allowParkour;
    }

    void setAllowParkour(boolean allowParkour) {
        this.allowParkour = allowParkour;
    }

    boolean allowJump() {
        return allowJump;
    }

    void setAllowJump(boolean allowJump) {
        this.allowJump = allowJump;
    }

    boolean allowFall() {
        return allowFall;
    }

    void setAllowFall(boolean allowFall) {
        this.allowFall = allowFall;
    }

    boolean allowClimb() {
        return allowClimb;
    }

    void setAllowClimb(boolean allowClimb) {
        this.allowClimb = allowClimb;
    }

    boolean allowSwim() {
        return allowSwim;
    }

    void setAllowSwim(boolean allowSwim) {
        this.allowSwim = allowSwim;
    }

    boolean allowStepUp() {
        return allowStepUp;
    }

    void setAllowStepUp(boolean allowStepUp) {
        this.allowStepUp = allowStepUp;
    }

    boolean allowWalkDiagonal() {
        return allowWalkDiagonal;
    }

    void setAllowWalkDiagonal(boolean allowWalkDiagonal) {
        this.allowWalkDiagonal = allowWalkDiagonal;
    }

    void setAllowSneak(boolean allowSneak) {
        this.allowSneak = allowSneak;
        if (!allowSneak) {
            sneakLatched = false;
            stickySneakDistance = -1.0;
        }
    }

    void setExactGoalCentering(boolean exactGoalCentering) {
        this.exactGoalCentering = exactGoalCentering;
    }
}
