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

package fr.riege.ebsl.common.pathfinding;

import fr.riege.ebsl.common.pathfinding.wrapper.PathPosition;

import java.util.List;
import java.util.Objects;

/**
 * Semantic long-range plan passed between planning, acceptance, memory, and execution.
 */
public record LongRangePathPlan(
    List<PathPosition> positions,
    List<Node.MoveType> moveTypes,
    double estimatedCost,
    double riskScore,
    boolean partial,
    LongRangePlanningStrategy planningStrategy
) {
    public LongRangePathPlan {
        positions = List.copyOf(Objects.requireNonNull(positions, "positions"));
        moveTypes = List.copyOf(Objects.requireNonNull(moveTypes, "moveTypes"));
        if (!Double.isFinite(estimatedCost) || estimatedCost < 0.0) {
            throw new IllegalArgumentException("estimatedCost must be finite and non-negative");
        }
        if (!Double.isFinite(riskScore) || riskScore < 0.0) {
            throw new IllegalArgumentException("riskScore must be finite and non-negative");
        }
        planningStrategy = planningStrategy == null ? LongRangePlanningStrategy.ROLLING_HORIZON : planningStrategy;
    }

    public static LongRangePathPlan fromNodes(List<Node> nodes, boolean partial,
                                              LongRangePlanningStrategy planningStrategy) {
        Objects.requireNonNull(nodes, "nodes");
        List<PathPosition> positions = nodes.stream().map(node -> node.position).toList();
        List<Node.MoveType> moveTypes = nodes.stream().map(Node::moveType).toList();
        double cost = estimateCost(nodes);
        double risk = estimateRisk(nodes);
        return new LongRangePathPlan(positions, moveTypes, cost, risk, partial, planningStrategy);
    }

    public double qualityScore() {
        double partialPenalty = partial ? 250.0 : 0.0;
        return estimatedCost + riskScore * 3.0 + partialPenalty;
    }

    private static double estimateCost(List<Node> nodes) {
        if (nodes.isEmpty()) {
            return 0.0;
        }
        double knownCost = nodes.getLast().gCost();
        if (Double.isFinite(knownCost) && knownCost > 0.0) {
            return knownCost;
        }
        return Math.max(0, nodes.size() - 1);
    }

    private static double estimateRisk(List<Node> nodes) {
        double risk = 0.0;
        for (Node node : nodes) {
            risk += switch (node.moveType()) {
                case PARKOUR -> 8.0;
                case JUMP, FALL -> 4.0;
                case SWIM, CLIMB, FLY -> 2.0;
                default -> 0.0;
            };
        }
        return risk;
    }
}
