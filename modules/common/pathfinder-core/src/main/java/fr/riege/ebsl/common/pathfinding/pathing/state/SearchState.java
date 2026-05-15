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

package fr.riege.ebsl.common.pathfinding.pathing.state;

import fr.riege.ebsl.common.pathfinding.Node;
import fr.riege.ebsl.common.pathfinding.wrapper.PathPosition;

import java.util.Objects;

/**
 * Extensible identity for path search nodes.
 *
 * <p>The current engine still keys closed/open sets by position for speed, but this type is the
 * contract for richer planners that need to distinguish stance, movement phase, or medium.</p>
 */
public record SearchState(PathPosition position, Node.MoveType arrivalMoveType) {
    public SearchState {
        Objects.requireNonNull(position, "position");
        arrivalMoveType = arrivalMoveType == null ? Node.MoveType.WALK : arrivalMoveType;
    }

    public static SearchState at(PathPosition position) {
        return new SearchState(position, Node.MoveType.WALK);
    }
}
