package fr.riege.ebsl.pathfinding.check;

final class HugeDeviationCheck implements PathCheck {
    private static final double HUGE_HORIZONTAL_DISTANCE = 12.0;

    @Override
    public PathCheckResult evaluate(PathCheckContext context) {
        PathProximitySnapshot proximity = context.proximity();
        if (proximity.horizontalDistance() >= HUGE_HORIZONTAL_DISTANCE) {
            return PathCheckResult.forceReplan(String.format(
                "huge horizontal deviation h=%.2f y=%.2f segment=%d",
                proximity.horizontalDistance(),
                proximity.verticalDistance(),
                proximity.nearestSegmentIndex()));
        }
        return PathCheckResult.none();
    }
}
