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
import fr.riege.ebsl.common.pathfinding.ProcessedPath;
import fr.riege.ebsl.common.pathfinding.movement.MovementTerrain;
import fr.riege.ebsl.common.pathfinding.pathing.configuration.PathfinderConfiguration;
import fr.riege.ebsl.common.pathfinding.pathing.result.PathState;
import fr.riege.ebsl.common.pathfinding.pathing.result.PathfinderResult;
import fr.riege.ebsl.common.pathfinding.quality.PathQualityContext;
import fr.riege.ebsl.common.pathfinding.quality.PathQualityRegistry;
import fr.riege.ebsl.common.pathfinding.quality.PathQualityReport;
import fr.riege.ebsl.common.pathfinding.wrapper.PathPosition;

import java.util.Collection;
import java.util.List;

public record PathPlan(
    PathfinderResult result,
    PathfinderConfiguration configuration,
    List<PathPosition> positions,
    List<Node> rawNodes,
    List<Node> navigationNodes,
    double pathLength,
    PathQualityReport quality
) {
    public PathPlan {
        quality = quality == null ? PathQualityReport.UNKNOWN : quality;
    }

    public static PathPlan empty(PathfinderResult result, PathfinderConfiguration configuration) {
        PathQualityReport quality = PathQualityRegistry.evaluate(new PathQualityContext(
            result,
            configuration,
            List.of(),
            List.of(),
            List.of(),
            0.0
        ));
        return new PathPlan(result, configuration, List.of(), List.of(), List.of(), 0.0, quality);
    }

    public static PathPlan from(PathfinderResult result,
                                PathfinderConfiguration configuration,
                                Collection<PathPosition> positions,
                                ProcessedPath processedPath) {
        return from(result, configuration, positions, processedPath, null);
    }

    public static PathPlan from(PathfinderResult result,
                                PathfinderConfiguration configuration,
                                Collection<PathPosition> positions,
                                ProcessedPath processedPath,
                                MovementTerrain checker) {
        List<PathPosition> positionList = positions instanceof List<PathPosition> list
            ? List.copyOf(list)
            : List.copyOf(positions);
        if (processedPath == null) {
            PathQualityReport quality = PathQualityRegistry.evaluate(PathQualityContext.of(result, configuration, positionList));
            return new PathPlan(result, configuration, positionList, List.of(), List.of(), 0.0, quality);
        }
        PathQualityReport quality = PathQualityRegistry.evaluate(new PathQualityContext(
            result,
            configuration,
            positionList,
            processedPath.rawNodes(),
            processedPath.navigationPath(),
            processedPath.pathLength(),
            checker
        ));
        return new PathPlan(
            result,
            configuration,
            positionList,
            List.copyOf(processedPath.rawNodes()),
            List.copyOf(processedPath.navigationPath()),
            processedPath.pathLength(),
            quality);
    }

    public boolean successful() {
        return result != null && result.successful();
    }

    public boolean usable() {
        return result != null
            && result.getPath() != null
            && (result.successful() || result.hasFallenBack())
            && !positions.isEmpty()
            && result.getPathState() != PathState.ABORTED;
    }

    public boolean complete() {
        return result != null && result.successful() && result.getPathState() == PathState.FOUND;
    }

    public PathPosition start() {
        return positions.isEmpty() ? null : positions.getFirst();
    }

    public PathPosition end() {
        return positions.isEmpty() ? null : positions.getLast();
    }
}
