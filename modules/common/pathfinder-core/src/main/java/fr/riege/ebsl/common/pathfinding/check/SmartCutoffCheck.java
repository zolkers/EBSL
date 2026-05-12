package fr.riege.ebsl.common.pathfinding.check;

import fr.riege.ebsl.common.pathfinding.annotation.PathCheckRole;
import fr.riege.ebsl.common.pathfinding.settings.PathfinderSettings;

@PathCheckRole("smart_cutoff")
final class SmartCutoffCheck implements PathCheck {
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
        if (proximity.horizontalDistance() > PathfinderSettings.instance().smartCutoffMaxHorizontalDistance.value()
            || proximity.verticalDistance() > PathfinderSettings.instance().smartCutoffMaxVerticalDistance.value()) {
            return PathCheckResult.none();
        }
        if (proximity.progress() < context.pursuitSegment() + PathfinderSettings.instance().smartCutoffMinProgressSkip.value()) {
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
