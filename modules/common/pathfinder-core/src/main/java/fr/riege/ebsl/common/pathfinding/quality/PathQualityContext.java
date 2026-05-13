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

import fr.riege.ebsl.common.pathfinding.Node;
import fr.riege.ebsl.common.pathfinding.movement.MovementTerrain;
import fr.riege.ebsl.common.pathfinding.pathing.configuration.PathfinderConfiguration;
import fr.riege.ebsl.common.pathfinding.pathing.result.PathfinderResult;
import fr.riege.ebsl.common.pathfinding.wrapper.PathPosition;

import java.util.Collection;
import java.util.List;

public record PathQualityContext(
    PathfinderResult result,
    PathfinderConfiguration configuration,
    List<PathPosition> positions,
    List<Node> rawNodes,
    List<Node> navigationNodes,
    double pathLength,
    MovementTerrain checker
) {
    public PathQualityContext(PathfinderResult result, PathfinderConfiguration configuration,
                              List<PathPosition> positions, List<Node> rawNodes,
                              List<Node> navigationNodes, double pathLength) {
        this(result, configuration, positions, rawNodes, navigationNodes, pathLength, null);
    }

    public PathQualityContext {
        positions = positions == null ? List.of() : List.copyOf(positions);
        rawNodes = rawNodes == null ? List.of() : List.copyOf(rawNodes);
        navigationNodes = navigationNodes == null ? List.of() : List.copyOf(navigationNodes);
        pathLength = pathLength > 0.0 ? pathLength : directPathLength(positions);
    }

    public static PathQualityContext of(PathfinderResult result, PathfinderConfiguration configuration,
                                        Collection<PathPosition> positions) {
        List<PathPosition> positionList = positions == null ? List.of() : List.copyOf(positions);
        return new PathQualityContext(result, configuration, positionList, List.of(), List.of(), directPathLength(positionList), null);
    }

    public PathPosition start() {
        return positions.isEmpty() ? null : positions.getFirst();
    }

    public PathPosition end() {
        return positions.isEmpty() ? null : positions.getLast();
    }

    private static double directPathLength(List<PathPosition> positions) {
        if (positions == null || positions.size() < 2) {
            return 0.0;
        }
        double total = 0.0;
        for (int i = 1; i < positions.size(); i++) {
            total += positions.get(i - 1).distance(positions.get(i));
        }
        return total;
    }
}
