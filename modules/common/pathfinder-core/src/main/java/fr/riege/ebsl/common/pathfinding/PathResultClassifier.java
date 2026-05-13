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

package fr.riege.ebsl.common.pathfinding;

import fr.riege.ebsl.common.pathfinding.annotation.PathingStage;
import fr.riege.ebsl.common.pathfinding.pathing.result.PathState;
import fr.riege.ebsl.common.pathfinding.pathing.result.PathfinderResult;
import fr.riege.ebsl.common.pathfinding.wrapper.PathPosition;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@PathingStage(PathingStage.Stage.RESULT_CLASSIFICATION)
public final class PathResultClassifier {
    private PathResultClassifier() {
    }

    public enum PathAvailability {
        COMPLETE,
        PARTIAL_USABLE,
        FAILED
    }

    public static PathAvailability classifyWalkResult(PathfinderResult result, Collection<PathPosition> positions,
                                               int requestedX, int requestedY, int requestedZ) {
        if (!hasUsablePath(result, positions)) {
            return PathAvailability.FAILED;
        }
        return isPartialWalkResult(result, positions, requestedX, requestedY, requestedZ)
            ? PathAvailability.PARTIAL_USABLE
            : PathAvailability.COMPLETE;
    }

    public static boolean hasUsablePath(PathfinderResult result, Collection<PathPosition> positions) {
        return result != null
            && result.getPath() != null
            && (result.successful() || result.hasFallenBack())
            && positions != null
            && !positions.isEmpty()
            && result.getPathState() != PathState.ABORTED;
    }

    public static boolean isPartialWalkResult(PathfinderResult result, Collection<PathPosition> positions,
                                       int requestedX, int requestedY, int requestedZ) {
        if (result == null || positions == null || positions.isEmpty()) {
            return true;
        }
        PathState state = result.getPathState();

        if (state == PathState.ABORTED || state == PathState.FAILED) {
            return true;
        }
        if (state != PathState.FOUND) {
            return true;
        }
        PathPosition last = positions instanceof List<?>
            ? ((List<PathPosition>) positions).getLast()
            : new ArrayList<>(positions).getLast();
        return last.flooredX() != requestedX
            || last.flooredY() != requestedY
            || last.flooredZ() != requestedZ;
    }
}
