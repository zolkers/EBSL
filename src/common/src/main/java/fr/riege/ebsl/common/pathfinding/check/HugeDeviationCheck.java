package fr.riege.ebsl.common.pathfinding.check;

import fr.riege.ebsl.common.pathfinding.annotation.PathCheckRole;
import fr.riege.ebsl.common.pathfinding.settings.PathfinderSettings;

@PathCheckRole("huge_deviation_replan")
final class HugeDeviationCheck implements PathCheck {
    @Override
    public PathCheckResult evaluate(PathCheckContext context) {
        PathProximitySnapshot proximity = context.proximity();
        if (proximity.horizontalDistance() >= PathfinderSettings.instance().hugeDeviationHorizontalDistance.value()) {
            return PathCheckResult.repairToSegment(proximity.nearestSegmentIndex(), String.format(
                "huge horizontal deviation repair h=%.2f y=%.2f segment=%d",
                proximity.horizontalDistance(),
                proximity.verticalDistance(),
                proximity.nearestSegmentIndex()));
        }
        return PathCheckResult.none();
    }
}
