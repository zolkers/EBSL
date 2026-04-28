package fr.riege.ebsl.pathfinding.check;

import fr.riege.ebsl.pathfinding.annotation.PathCheckRole;

@PathCheckRole("sustained_off_path_replan")
final class SustainedOffPathCheck implements PathCheck {
    private static final long MAX_OFF_PATH_MS = 5000L;
    // Vertical recovery gets more time because higher jump height can keep Y off-path longer.
    private static final long MAX_VERTICAL_OFF_PATH_MS = 7000L;
    private static final double OFF_PATH_HORIZONTAL_DISTANCE = 3.0;
    private static final double OFF_PATH_VERTICAL_DISTANCE = 3.0;

    @Override
    public PathCheckResult evaluate(PathCheckContext context) {
        PathProximitySnapshot proximity = context.proximity();
        boolean verticalDeviation = proximity.verticalDistance() >= OFF_PATH_VERTICAL_DISTANCE;
        if (verticalDeviation && context.severeOffPathDurationMs() >= MAX_VERTICAL_OFF_PATH_MS) {
            return PathCheckResult.forceReplan(String.format(
                "vertical off-path timeout dist3d=%.2f y=%.2f duration=%dms segment=%d",
                proximity.distance3d(),
                proximity.verticalDistance(),
                context.severeOffPathDurationMs(),
                proximity.nearestSegmentIndex()));
        }

        boolean horizontalDeviation = proximity.horizontalDistance() >= OFF_PATH_HORIZONTAL_DISTANCE;
        if (horizontalDeviation && context.severeOffPathDurationMs() >= MAX_OFF_PATH_MS) {
            return PathCheckResult.forceReplan(String.format(
                "horizontal off-path timeout dist3d=%.2f h=%.2f duration=%dms segment=%d",
                proximity.distance3d(),
                proximity.horizontalDistance(),
                context.severeOffPathDurationMs(),
                proximity.nearestSegmentIndex()));
        }
        return PathCheckResult.none();
    }
}
