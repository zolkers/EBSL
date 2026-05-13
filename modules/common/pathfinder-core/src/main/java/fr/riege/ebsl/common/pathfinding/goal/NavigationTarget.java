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
package fr.riege.ebsl.common.pathfinding.goal;

/**
 * Identifies the concrete target produced by a navigation goal.
 *
 * <p>The sealed hierarchy separates exact block targets from column-level targets while keeping request handling exhaustive.</p>
 */
public sealed interface NavigationTarget {
    record Block(int x, int y, int z) implements NavigationTarget {}
    record Column(int x, int z) implements NavigationTarget {}
}
