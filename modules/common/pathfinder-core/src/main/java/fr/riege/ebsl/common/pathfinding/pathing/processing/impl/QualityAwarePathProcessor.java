/*
 * This file is part of EBSL.
 *
 * EBSL is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * EBSL is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package fr.riege.ebsl.common.pathfinding.pathing.processing.impl;

import fr.riege.ebsl.common.pathfinding.Node;
import fr.riege.ebsl.common.pathfinding.movement.MovementClassificationContext;
import fr.riege.ebsl.common.pathfinding.movement.MovementTerrain;
import fr.riege.ebsl.common.pathfinding.pathing.configuration.PathfinderConfiguration;
import fr.riege.ebsl.common.pathfinding.pathing.processing.Cost;
import fr.riege.ebsl.common.pathfinding.pathing.processing.NodeProcessor;
import fr.riege.ebsl.common.pathfinding.pathing.processing.context.EvaluationContext;
import fr.riege.ebsl.common.pathfinding.provider.WorldNavigationPointProvider;
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

        MovementTerrain checker = context.getNavigationPointProvider() instanceof WorldNavigationPointProvider provider
            ? provider.checker()
            : null;
        Node.MoveType moveType = context.getCurrentMoveType() == null
            ? configuration.movementClassifier.classify(new MovementClassificationContext(
                previous,
                current,
                context.getNavigationPointProvider(),
                context.getEnvironmentContext(),
                checker))
            : context.getCurrentMoveType();

        double cost = configuration.movementCostModel.planningPenalty(moveType) * riskWeight;
        if (checker != null && terrainWeight > 0.0) {
            cost += (1.0 - TerrainOpportunityScorer.scorePosition(checker, current)) * terrainWeight;
        }
        return Cost.of(Math.max(0.0, cost));
    }
}
