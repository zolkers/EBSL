package fr.riege.ebsl.pathfinding.check;

import fr.riege.ebsl.pathfinding.PathfinderConfig;
import fr.riege.ebsl.pathfinding.annotation.PathCheckRole;

@PathCheckRole("huge_deviation_replan")
final class HugeDeviationCheck implements PathCheck {
    @Override
    public PathCheckResult evaluate(PathCheckContext context) {
        PathProximitySnapshot proximity = context.proximity();
        if (proximity.horizontalDistance() >= PathfinderConfig.HUGE_DEVIATION_HORIZONTAL_DISTANCE.get()) {
            return PathCheckResult.forceReplan(String.format(
                "huge horizontal deviation h=%.2f y=%.2f segment=%d",
                proximity.horizontalDistance(),
                proximity.verticalDistance(),
                proximity.nearestSegmentIndex()));
        }
        return PathCheckResult.none();
    }
}
