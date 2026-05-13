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
package fr.riege.ebsl.common.pathfinding.pathing.processing.context;

import fr.riege.ebsl.common.pathfinding.pathing.configuration.PathfinderConfiguration;
import fr.riege.ebsl.common.pathfinding.pathing.context.EnvironmentContext;
import fr.riege.ebsl.common.pathfinding.provider.NavigationPointProvider;
import fr.riege.ebsl.common.pathfinding.wrapper.PathPosition;
import java.util.Map;

/**
 * Describes immutable and shared state for one pathfinding search.
 *
 * <p>Search-scoped data is passed to processors so they can coordinate without depending on the concrete pathfinder implementation.</p>
 */
public interface SearchContext {
    /**
     * Returns the start position for the active search.
 *
     * @return the value defined by this contract
     */
    PathPosition getStartPathPosition();
    /**
     * Returns the target position for the active search.
 *
     * @return the value defined by this contract
     */
    PathPosition getTargetPathPosition();
    /**
     * Returns the pathfinder configuration for the active search.
 *
     * @return the value defined by this contract
     */
    PathfinderConfiguration getPathfinderConfiguration();
    /**
     * Returns the navigation point provider used by the active search.
 *
     * @return the value defined by this contract
     */
    NavigationPointProvider getNavigationPointProvider();
    /**
     * Returns the shared mutable data map for cooperating processors.
 *
     * @return the value defined by this contract
     */
    Map<String, Object> getSharedData();
    /**
     * Returns optional environment metadata for the active search.
 *
     * @return the value defined by this contract
     */
    EnvironmentContext getEnvironmentContext();
}
