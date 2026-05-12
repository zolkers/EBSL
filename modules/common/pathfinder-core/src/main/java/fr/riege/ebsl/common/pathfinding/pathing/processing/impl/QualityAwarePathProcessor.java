package fr.riege.ebsl.common.pathfinding.pathing.processing.impl;

import fr.riege.ebsl.common.pathfinding.Node;
import fr.riege.ebsl.common.pathfinding.movement.WalkabilityChecker;
import fr.riege.ebsl.common.pathfinding.pathing.configuration.PathfinderConfiguration;
import fr.riege.ebsl.common.pathfinding.pathing.processing.Cost;
import fr.riege.ebsl.common.pathfinding.pathing.processing.NodeProcessor;
import fr.riege.ebsl.common.pathfinding.pathing.processing.context.EvaluationContext;
import fr.riege.ebsl.common.pathfinding.provider.LayerNavigationPointProvider;
import fr.riege.ebsl.common.pathfinding.quality.MovementRiskScorer;
import fr.riege.ebsl.common.pathfinding.quality.PathMoveClassifier;
import fr.riege.ebsl.common.pathfinding.quality.TerrainOpportunityScorer;
import fr.riege.ebsl.common.pathfinding.wrapper.PathPosition;

public final class QualityAwarePathProcessor implements NodeProcessor {
    @Override
    public Cost calculateCostContribution(EvaluationContext context) {
        PathPosition previous = context.getPreviousPathPosition();
        PathPosition current = context.getCurrentPathPosition();
        if (previous == null || current == null) {
            return Cost.ZERO;
        }

        PathfinderConfiguration configuration = context.getPathfinderConfiguration();
        double riskWeight = Math.max(0.0, configuration.qualityRiskCostWeight);
        double terrainWeight = Math.max(0.0, configuration.qualityTerrainCostWeight);
        if (riskWeight <= 0.0 && terrainWeight <= 0.0) {
            return Cost.ZERO;
        }

        WalkabilityChecker checker = context.getNavigationPointProvider() instanceof LayerNavigationPointProvider provider
            ? provider.checker()
            : null;
        Node.MoveType moveType = PathMoveClassifier.classify(
            previous,
            current,
            context.getNavigationPointProvider(),
            context.getEnvironmentContext(),
            checker
        );

        double cost = MovementRiskScorer.risk(moveType) * riskWeight;
        if (checker != null && terrainWeight > 0.0) {
            cost += (1.0 - TerrainOpportunityScorer.scorePosition(checker, current)) * terrainWeight;
        }
        return Cost.of(Math.max(0.0, cost));
    }
}
