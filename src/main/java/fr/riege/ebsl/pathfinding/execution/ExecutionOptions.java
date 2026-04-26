package fr.riege.ebsl.pathfinding.execution;

public record ExecutionOptions(
    boolean allowReplan,
    boolean allowJumps,
    boolean allowRotation,
    boolean exactGoalCentering,
    double stickySneakDistance,
    boolean sneakLatched,
    double goalCenterX,
    double goalCenterZ,
    double preciseGoalTolerance
) {
    public static final double DEFAULT_TOLERANCE = 0.5;

    static ExecutionOptions defaults() {
        return new ExecutionOptions(true, true, true, false, -1.0, false, 0.5, 0.5, DEFAULT_TOLERANCE);
    }
}
