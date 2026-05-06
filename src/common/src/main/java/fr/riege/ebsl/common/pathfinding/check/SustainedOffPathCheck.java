package fr.riege.ebsl.common.pathfinding.check;

import fr.riege.ebsl.common.pathfinding.annotation.PathCheckRole;
import fr.riege.ebsl.common.pathfinding.settings.PathfinderSettings;

@PathCheckRole("sustained_off_path_replan")
final class SustainedOffPathCheck implements PathCheck {
    @Override
    public PathCheckResult evaluate(PathCheckContext context) {
        PathProximitySnapshot proximity = context.proximity();
        boolean verticalDeviation = proximity.verticalDistance() >= PathfinderSettings.instance().offPathVerticalDistance.value();
        if (verticalDeviation
            && context.severeOffPathDurationMs() >= PathfinderSettings.instance().sustainedVerticalOffPathMaxMs.value()) {
            return PathCheckResult.repairToSegment(proximity.nearestSegmentIndex(), String.format(
                "vertical off-path repair dist3d=%.2f y=%.2f duration=%dms segment=%d",
                proximity.distance3d(),
                proximity.verticalDistance(),
                context.severeOffPathDurationMs(),
                proximity.nearestSegmentIndex()));
        }

        boolean horizontalDeviation = proximity.horizontalDistance() >= PathfinderSettings.instance().offPathHorizontalDistance.value();
        if (horizontalDeviation && context.severeOffPathDurationMs() >= PathfinderSettings.instance().sustainedOffPathMaxMs.value()) {
            return PathCheckResult.repairToSegment(proximity.nearestSegmentIndex(), String.format(
                "horizontal off-path repair dist3d=%.2f h=%.2f duration=%dms segment=%d",
                proximity.distance3d(),
                proximity.horizontalDistance(),
                context.severeOffPathDurationMs(),
                proximity.nearestSegmentIndex()));
        }
        return PathCheckResult.none();
    }
}
