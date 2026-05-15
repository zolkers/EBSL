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

package fr.riege.ebsl.common.pathfinding.pathing.action;

import fr.riege.ebsl.common.pathfinding.Node;
import fr.riege.ebsl.common.pathfinding.wrapper.PathVector;

import java.util.Objects;

/**
 * Search candidate representing an executable movement, not only a geometric offset.
 */
public record MovementAction(
    String id,
    PathVector offset,
    Node.MoveType moveTypeHint,
    int durationTicks,
    double costMultiplier,
    double extraCost
) {
    public MovementAction {
        id = id == null || id.isBlank() ? "offset" : id.trim();
        Objects.requireNonNull(offset, "offset");
        durationTicks = Math.max(1, durationTicks);
        costMultiplier = Double.isFinite(costMultiplier) ? Math.max(0.0, costMultiplier) : 1.0;
        extraCost = Double.isFinite(extraCost) ? Math.max(0.0, extraCost) : 0.0;
    }

    public static MovementAction offset(PathVector offset) {
        return new MovementAction("offset:" + offset.x + "," + offset.y + "," + offset.z, offset, null, 1, 1.0, 0.0);
    }

    public MovementAction withMoveTypeHint(Node.MoveType moveType) {
        return new MovementAction(id, offset, moveType, durationTicks, costMultiplier, extraCost);
    }

    public MovementAction withCost(double multiplier, double extra) {
        return new MovementAction(id, offset, moveTypeHint, durationTicks, multiplier, extra);
    }
}
