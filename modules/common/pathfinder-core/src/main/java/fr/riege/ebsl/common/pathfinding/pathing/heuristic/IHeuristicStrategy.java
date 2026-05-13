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

import fr.riege.ebsl.common.pathfinding.wrapper.PathPosition;

/**
 * Scores distance and transition cost for A* pathfinding.
 *
 * <p>Heuristics must remain stable for a search run and should balance admissibility, terrain preference, and movement realism.</p>
 */
public interface IHeuristicStrategy {
    /**
     * Calculates the heuristic score for the supplied context.
 *
     * @param context the context describing the operation being performed
     * @return the value defined by this contract
     */
    double calculate(HeuristicContext context);
    /**
     * Calculates the movement cost between two adjacent path positions.
 *
     * @param from the first value or starting position
     * @param to the second value or ending position
     * @return the value defined by this contract
     */
    double calculateTransitionCost(PathPosition from, PathPosition to);
}
