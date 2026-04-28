package fr.riege.ebsl.pathfinding.check;

import fr.riege.ebsl.pathfinding.PathfinderConfig;
import fr.riege.ebsl.pathfinding.annotation.PathCheckRole;

@PathCheckRole("sustained_off_path_replan")
final class SustainedOffPathCheck implements PathCheck {
    @Override
    public PathCheckResult evaluate(PathCheckContext context) {
        PathProximitySnapshot proximity = context.proximity();
        boolean verticalDeviation = proximity.verticalDistance() >= PathfinderConfig.OFF_PATH_VERTICAL_DISTANCE.get();
        if (verticalDeviation
            && context.severeOffPathDurationMs() >= PathfinderConfig.SUSTAINED_VERTICAL_OFF_PATH_MAX_MS.get()) {
            return PathCheckResult.forceReplan(String.format(
                "vertical off-path timeout dist3d=%.2f y=%.2f duration=%dms segment=%d",
                proximity.distance3d(),
                proximity.verticalDistance(),
                context.severeOffPathDurationMs(),
                proximity.nearestSegmentIndex()));
        }

        boolean horizontalDeviation = proximity.horizontalDistance() >= PathfinderConfig.OFF_PATH_HORIZONTAL_DISTANCE.get();
        if (horizontalDeviation && context.severeOffPathDurationMs() >= PathfinderConfig.SUSTAINED_OFF_PATH_MAX_MS.get()) {
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
