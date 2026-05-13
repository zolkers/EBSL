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
package fr.riege.ebsl.common.pathfinding.quality;

import fr.riege.ebsl.common.pathfinding.movement.MovementTerrain;
import fr.riege.ebsl.common.pathfinding.wrapper.PathPosition;

import java.util.List;

final class TerrainOpportunityQualityMetric implements PathQualityMetric {
    @Override
    public String id() {
        return "terrain_opportunity";
    }

    @Override
    public PathQualityContribution evaluate(PathQualityContext context) {
        MovementTerrain checker = context.checker();
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
