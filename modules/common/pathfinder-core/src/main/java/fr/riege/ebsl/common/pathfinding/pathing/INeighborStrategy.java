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

package fr.riege.ebsl.common.pathfinding.pathing;

import fr.riege.ebsl.common.pathfinding.pathing.action.MovementAction;
import fr.riege.ebsl.common.pathfinding.pathing.state.SearchState;
import fr.riege.ebsl.common.pathfinding.wrapper.PathPosition;
import fr.riege.ebsl.common.pathfinding.wrapper.PathVector;

import java.util.ArrayList;
import java.util.List;

/**
 * Supplies candidate offsets explored by the pathfinder.
 *
 * <p>Strategies can return global offsets or offsets tailored to the current position while keeping search expansion pluggable.</p>
 */
@FunctionalInterface
public interface INeighborStrategy {
    /**
     * Returns candidate neighbor offsets for the supplied search state.
 *
     * @return the requested values
     */
    Iterable<PathVector> getOffsets();

    /**
     * Returns candidate neighbor offsets for the supplied search state.
 *
     * @param currentPosition the current path position being expanded
     * @return the requested values
     */
    default Iterable<PathVector> getOffsets(PathPosition currentPosition) {
        return getOffsets();
    }

    /**
     * Returns candidate movement actions for the supplied search state.
     *
     * <p>The default adapter preserves legacy offset-only strategies while allowing advanced
     * strategies to expose executable movement metadata.</p>
     *
     * @param currentPosition the current path position being expanded
     * @return the requested values
     */
    default Iterable<MovementAction> getActions(PathPosition currentPosition) {
        List<MovementAction> actions = new ArrayList<>();
        for (PathVector offset : getOffsets(currentPosition)) {
            actions.add(MovementAction.offset(offset));
        }
        return actions;
    }

    /**
     * Returns candidate movement actions for the supplied search state.
     *
     * <p>Strategies that care about arrival movement type, stance, medium, or other stateful
     * information can override this method while legacy strategies can keep exposing offsets.</p>
     *
     * @param currentState the state currently being expanded
     * @return the requested values
     */
    default Iterable<MovementAction> getActions(SearchState currentState) {
        return getActions(currentState.position());
    }
}
