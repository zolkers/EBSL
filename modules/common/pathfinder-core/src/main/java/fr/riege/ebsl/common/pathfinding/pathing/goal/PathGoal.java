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

package fr.riege.ebsl.common.pathfinding.pathing.goal;

import fr.riege.ebsl.common.pathfinding.pathing.heuristic.HeuristicWeights;
import fr.riege.ebsl.common.pathfinding.pathing.heuristic.IHeuristicStrategy;
import fr.riege.ebsl.common.pathfinding.wrapper.PathPosition;

/**
 * Describes a search objective that can be broader than one exact block.
 */
public interface PathGoal {
    /**
     * Representative endpoint used by legacy path reports and processors.
     *
     * @return the nominal target position for this goal
     */
    PathPosition representative();

    /**
     * Returns whether a search position satisfies the goal.
     *
     * @param position the position to evaluate
     * @return true when the search can stop at the supplied position
     */
    boolean isSatisfiedBy(PathPosition position);

    /**
     * Estimates the remaining cost from a position to this goal.
     *
     * @param position the current search position
     * @param start the request start position
     * @param weights heuristic weights configured for the search
     * @return an admissibility-oriented estimate for prioritizing candidates
     */
    double estimate(PathPosition position, PathPosition start, HeuristicWeights weights, IHeuristicStrategy strategy);
}
