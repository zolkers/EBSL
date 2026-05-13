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
package fr.riege.ebsl.common.pathfinding.check;

import fr.riege.ebsl.common.math.Vec3d;
import fr.riege.ebsl.common.pathfinding.Node;

import java.util.List;

public record PathCheckContext(
    Vec3d playerPos,
    List<Node> path,
    int pursuitSegment,
    int goalX,
    int goalY,
    int goalZ,
    long severeOffPathDurationMs,
    PathProximitySnapshot proximity
) {
    public Node.MoveType currentMoveType() {
        if (path == null || path.isEmpty()) {
            return Node.MoveType.WALK;
        }
        int index = Math.clamp(pursuitSegment, 0, path.size() - 1);
        return path.get(index).moveType();
    }

    public boolean hasMoveTypeInWindow(Node.MoveType moveType, int lookahead) {
        if (path == null || path.isEmpty()) {
            return false;
        }
        int start = Math.clamp(pursuitSegment, 0, path.size() - 1);
        int end = (int) Math.clamp(start + (long) Math.max(0, lookahead), 0L, path.size() - 1L);
        for (int i = start; i <= end; i++) {
            if (path.get(i).moveType() == moveType) {
                return true;
            }
        }
        return false;
    }
}
