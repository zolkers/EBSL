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

package fr.riege.ebsl.common.pathfinding.pathing.result;

import fr.riege.ebsl.common.pathfinding.quality.PathQualityReport;

/**
 * Wraps the outcome of a pathfinding request.
 *
 * <p>Results expose success/failure state, fallback information, the resolved path, and optional quality analysis.</p>
 */
public interface PathfinderResult {
    /**
     * Returns whether pathfinding completed with a usable path.
 *
     * @return true when the condition is satisfied; false otherwise
     */
    boolean successful();
    /**
     * Returns whether pathfinding ended in a failed state.
 *
     * @return true when the condition is satisfied; false otherwise
     */
    boolean hasFailed();
    /**
     * Returns whether the result uses a fallback path rather than the preferred target path.
 *
     * @return true when the condition is satisfied; false otherwise
     */
    boolean hasFallenBack();
    /**
     * Returns the state that best describes the pathfinding outcome.
 *
     * @return the value defined by this contract
     */
    PathState getPathState();
    /**
     * Returns the path produced by the search, when one is available.
 *
     * @return the value defined by this contract
     */
    Path getPath();

    /**
     * Returns the optional quality report attached to this result.
 *
     * @return the value defined by this contract
     */
    default PathQualityReport quality() {
        return PathQualityReport.UNKNOWN;
    }
}
