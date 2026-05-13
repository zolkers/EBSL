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

package fr.riege.ebsl.common.pathfinding.quality;

import fr.riege.ebsl.common.pathfinding.Node;

import java.util.List;

final class MovementRiskQualityMetric implements PathQualityMetric {
    @Override
    public String id() {
        return "movement_risk";
    }

    @Override
    public PathQualityContribution evaluate(PathQualityContext context) {
        List<Node> nodes = context.rawNodes().isEmpty() ? context.navigationNodes() : context.rawNodes();
        if (nodes.size() <= 1) {
            boolean hasPositionPath = context.positions().size() > 1;
            return new PathQualityContribution(id(), hasPositionPath || !nodes.isEmpty() ? 1.0 : 0.0, 1.4, "no typed moves");
        }
        double risk = 0.0;
        double maxRisk = 0.0;
        for (int i = 1; i < nodes.size(); i++) {
            double moveRisk = MovementRiskScorer.risk(nodes.get(i).moveType());
            risk += moveRisk;
            maxRisk = Math.max(maxRisk, moveRisk);
        }
        double averageRisk = risk / (nodes.size() - 1);
        double humanRisk = averageRisk * 0.55 + maxRisk * 0.45;
        double score = Math.clamp(1.0 - humanRisk, 0.0, 1.0);
        return new PathQualityContribution(id(), score, 1.8, String.format("risk %.2f max %.2f", averageRisk, maxRisk));
    }
}
