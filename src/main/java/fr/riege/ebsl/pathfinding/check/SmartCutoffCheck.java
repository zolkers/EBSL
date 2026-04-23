package fr.riege.ebsl.pathfinding.check;

final class SmartCutoffCheck implements PathCheck {
    private static final double MAX_CUTOFF_HORIZONTAL_DISTANCE = 1.35;
    private static final double MAX_CUTOFF_VERTICAL_DISTANCE = 1.75;
    private static final double MIN_PROGRESS_SKIP = 1.25;

    @Override
    public PathCheckResult evaluate(PathCheckContext context) {
        PathProximitySnapshot proximity = context.proximity();
        int candidateSegment = proximity.nearestSegmentIndex();
        if (candidateSegment < context.pursuitSegment() + 2) {
            return PathCheckResult.none();
        }
        if (candidateSegment >= context.path().size() - 1) {
            return PathCheckResult.none();
        }
        if (proximity.horizontalDistance() > MAX_CUTOFF_HORIZONTAL_DISTANCE
            || proximity.verticalDistance() > MAX_CUTOFF_VERTICAL_DISTANCE) {
            return PathCheckResult.none();
        }
        if (proximity.progress() < context.pursuitSegment() + MIN_PROGRESS_SKIP) {
            return PathCheckResult.none();
        }
        return PathCheckResult.cutoff(candidateSegment, String.format(
            "smart cutoff segment=%d progress=%.2f h=%.2f y=%.2f",
            candidateSegment,
            proximity.progress(),
            proximity.horizontalDistance(),
            proximity.verticalDistance()));
    }
}
