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

package fr.riege.ebsl.common.pathfinding.provider;

import fr.riege.ebsl.common.pathfinding.movement.MovementTerrain;

/**
 * Resolves navigation points from a world-backed movement terrain contract.
 *
 * <p>This contract is used by movement processors that need both cached navigation-point lookups
 * and lower-level terrain checks, while still avoiding a dependency on the concrete provider
 * implementation.</p>
 */
public interface WorldNavigationPointProvider extends NavigationPointProvider {
    /**
     * Returns the movement terrain backing this provider.
     *
     * @return the movement terrain used for terrain queries
     */
    MovementTerrain checker();

    /**
     * Clears cached navigation-point lookups.
     */
    void clearCache();
}
