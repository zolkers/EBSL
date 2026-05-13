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
package fr.riege.ebsl.common.pathfinding.provider;

import fr.riege.ebsl.common.pathfinding.movement.WalkabilityChecker;

/**
 * Resolves navigation points from a world-backed walkability checker.
 *
 * <p>This contract is used by movement processors that need both cached navigation-point lookups
 * and lower-level terrain checks, while still avoiding a dependency on the concrete provider
 * implementation.</p>
 */
public interface WorldNavigationPointProvider extends NavigationPointProvider {
    /**
     * Returns the walkability checker backing this provider.
     *
     * @return the walkability checker used for terrain queries
     */
    WalkabilityChecker checker();

    /**
     * Clears cached navigation-point lookups.
     */
    void clearCache();
}
