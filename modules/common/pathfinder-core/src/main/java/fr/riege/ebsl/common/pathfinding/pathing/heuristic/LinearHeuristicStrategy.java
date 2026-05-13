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
package fr.riege.ebsl.common.pathfinding.pathing.heuristic;

import fr.riege.ebsl.common.pathfinding.pathing.PathfindingProgress;
import fr.riege.ebsl.common.pathfinding.wrapper.PathPosition;

public final class LinearHeuristicStrategy implements IHeuristicStrategy {
    private static final double D1 = 1.0;
    private static final double D2 = Math.sqrt(2.0);
    private static final double D3 = Math.sqrt(3.0);

    @Override
    public double calculate(HeuristicContext context) {
        PathfindingProgress progress = context.pathfindingProgress;
        HeuristicWeights weights = context.heuristicWeights;

        PathPosition position = progress.current;
        PathPosition target = progress.target;

        int ax = Math.abs(position.flooredX() - target.flooredX());
        int ay = Math.abs(position.flooredY() - target.flooredY());
        int az = Math.abs(position.flooredZ() - target.flooredZ());

        double manhattan = (double) ax + ay + az;

        int min = Math.min(ax, Math.min(ay, az));
        int max = Math.max(ax, Math.max(ay, az));
        int mid = ax + ay + az - min - max;
        double octile = (D3 - D2) * min + (D2 - D1) * mid + D1 * max;

        double perpendicular = InternalHeuristicUtils.calculatePerpendicularDistance(progress);
        double height = Math.abs(position.flooredY() - target.flooredY());

        return manhattan * weights.manhattanWeight
             + octile * weights.octileWeight
             + perpendicular * weights.perpendicularWeight
             + height * weights.heightWeight;
    }

    @Override
    public double calculateTransitionCost(PathPosition from, PathPosition to) {
        double dx = to.centeredX() - from.centeredX();
        double dy = to.centeredY() - from.centeredY();
        double dz = to.centeredZ() - from.centeredZ();
        return Math.sqrt(dx*dx + dy*dy + dz*dz);
    }
}
