package fr.riege.ebsl.common.pathfinding.quality;

import fr.riege.ebsl.common.pathfinding.movement.WalkabilityChecker;
import fr.riege.ebsl.common.pathfinding.wrapper.PathPosition;

import java.util.List;

final class TerrainOpportunityQualityMetric implements PathQualityMetric {
    @Override
    public String id() {
        return "terrain_opportunity";
    }

    @Override
    public PathQualityContribution evaluate(PathQualityContext context) {
        WalkabilityChecker checker = context.checker();
        List<PathPosition> positions = context.positions();
        if (checker == null || positions.isEmpty()) {
            return new PathQualityContribution(id(), 0.75, 0.7, "not sampled");
        }
        double total = 0.0;
        int sampled = 0;
        for (PathPosition position : positions) {
            total += TerrainOpportunityScorer.scorePosition(checker, position);
            sampled++;
        }
        double score = sampled == 0 ? 0.0 : total / sampled;
        return new PathQualityContribution(id(), score, 1.1, sampled + " samples");
    }
}
