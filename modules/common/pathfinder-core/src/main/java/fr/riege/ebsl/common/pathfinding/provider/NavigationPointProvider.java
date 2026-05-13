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

import fr.riege.ebsl.common.pathfinding.pathing.context.EnvironmentContext;
import fr.riege.ebsl.common.pathfinding.wrapper.PathPosition;

/**
 * Resolves navigation points from path positions.
 *
 * <p>Providers are the world abstraction used by planners to query traversability, floors, liquids, and climbable blocks.</p>
 */
public interface NavigationPointProvider {
    /**
     * Returns the navigation point resolved for the supplied position.
 *
     * @param position the position to inspect
     * @return the value defined by this contract
     */
    default NavigationPoint getNavigationPoint(PathPosition position) {
        return getNavigationPoint(position, null);
    }

    /**
     * Returns the navigation point resolved for the supplied position.
 *
     * @param position the position to inspect
     * @param environmentContext optional environment metadata for the lookup
     * @return the value defined by this contract
     */
    NavigationPoint getNavigationPoint(PathPosition position, EnvironmentContext environmentContext);
}
