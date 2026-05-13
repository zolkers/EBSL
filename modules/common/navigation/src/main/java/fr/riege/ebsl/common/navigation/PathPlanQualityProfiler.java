/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 *
 * Copyright (C) 2026 Victor Riegert and EBSL contributors
 *
 * This file is part of EBSL.
 *
 * EBSL is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * EBSL is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public
 * License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with EBSL. If not, see <https://www.gnu.org/licenses/>.
 */

package fr.riege.ebsl.common.navigation;

import fr.riege.ebsl.common.pathfinding.Node;
import fr.riege.ebsl.common.pathfinding.movement.MovementTerrain;
import fr.riege.ebsl.common.pathfinding.quality.MovementRiskScorer;
import fr.riege.ebsl.common.pathfinding.quality.TerrainOpportunityScorer;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

final class PathPlanQualityProfiler {
    private static final double MOVEMENT_WEIGHT = 0.72;
    private static final double TERRAIN_WEIGHT = 0.28;

    private PathPlanQualityProfiler() {
    }

    static PathPlanQualityProfile profile(PathPlan plan, MovementTerrain checker) {
        if (plan == null || !plan.usable()) {
            return new PathPlanQualityProfile(0.0, 1.0, 1.0, -1);
        }
        List<Node> nodes = plan.navigationNodes().isEmpty() ? plan.rawNodes() : plan.navigationNodes();
        if (nodes.size() <= 1) {
            return new PathPlanQualityProfile(plan.quality().score(), 0.0, 0.0, -1);
        }
        double worstWeakness = 0.0;
        int weakestIndex = -1;
        ArrayList<Double> weaknesses = new ArrayList<>();
        for (int i = 1; i < nodes.size(); i++) {
            double weakness = weakness(nodes.get(i), checker);
            weaknesses.add(weakness);
            if (weakness > worstWeakness) {
                worstWeakness = weakness;
                weakestIndex = i;
            }
        }
        weaknesses.sort(Comparator.reverseOrder());
        int sampleCount = Math.min(weaknesses.size(), Math.max(1, Math.ceilDiv(weaknesses.size(), 5)));
        double weakTotal = 0.0;
        for (int i = 0; i < sampleCount; i++) {
            weakTotal += weaknesses.get(i);
        }
        return new PathPlanQualityProfile(
            plan.quality().score(),
            worstWeakness,
            weakTotal / sampleCount,
            weakestIndex);
    }

    static double weakness(Node node, MovementTerrain checker) {
        if (node == null) {
            return 1.0;
        }
        double movementWeakness = MovementRiskScorer.risk(node.moveType());
        double terrainWeakness = checker == null ? movementWeakness : 1.0 - TerrainOpportunityScorer.scorePosition(checker, node.position);
        return Math.clamp(movementWeakness * MOVEMENT_WEIGHT + terrainWeakness * TERRAIN_WEIGHT, 0.0, 1.0);
    }
}
