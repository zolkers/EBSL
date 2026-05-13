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

package fr.riege.ebsl.common.pathfinding.pathing;

/**
 * Extends the pathfinder contract with diagnostic counters.
 *
 * <p>This interface is intended for tooling, tests, and debug UI that need to inspect how a
 * concrete search behaved without depending on the concrete algorithm class.</p>
 */
public interface InspectablePathfinder extends Pathfinder {
    /**
     * Returns how many nodes were expanded by the most recent search.
     *
     * @return the number of expanded nodes
     */
    long getExploredCount();

    /**
     * Returns a human-readable profiling summary for the most recent search.
     *
     * @return the profiling summary, or a disabled message when profiling is not active
     */
    String getProfilingReport();
}
