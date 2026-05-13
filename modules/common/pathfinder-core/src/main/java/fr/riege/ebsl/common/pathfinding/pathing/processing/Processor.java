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

package fr.riege.ebsl.common.pathfinding.pathing.processing;

import fr.riege.ebsl.common.pathfinding.pathing.processing.context.SearchContext;

/**
 * Receives lifecycle callbacks around a pathfinding search.
 *
 * <p>Processors use these hooks to initialize shared data, clear caches, and release state after the search completes.</p>
 */
public interface Processor {
    /**
     * Initializes processor state before a search starts.
 *
     * @param context the context describing the operation being performed
     */
    default void initializeSearch(SearchContext context) {}
    /**
     * Finalizes processor state after a search completes or aborts.
 *
     * @param context the context describing the operation being performed
     */
    default void finalizeSearch(SearchContext context) {}
}
