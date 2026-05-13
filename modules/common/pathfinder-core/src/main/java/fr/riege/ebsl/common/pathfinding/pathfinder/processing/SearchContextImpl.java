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
package fr.riege.ebsl.common.pathfinding.pathfinder.processing;

import fr.riege.ebsl.common.pathfinding.pathing.configuration.PathfinderConfiguration;
import fr.riege.ebsl.common.pathfinding.pathing.context.EnvironmentContext;
import fr.riege.ebsl.common.pathfinding.pathing.processing.context.SearchContext;
import fr.riege.ebsl.common.pathfinding.provider.NavigationPointProvider;
import fr.riege.ebsl.common.pathfinding.wrapper.PathPosition;
import java.util.HashMap;
import java.util.Map;

public final class SearchContextImpl implements SearchContext {

    private final PathPosition startPathPosition;
    private final PathPosition targetPathPosition;
    private final PathfinderConfiguration pathfinderConfiguration;
    private final NavigationPointProvider navigationPointProvider;
    private final EnvironmentContext environmentContext;
    private final Map<String, Object> sharedData = new HashMap<>();

    public SearchContextImpl(PathPosition start, PathPosition target,
                              PathfinderConfiguration configuration,
                              NavigationPointProvider provider,
                              EnvironmentContext environmentContext) {
        this.startPathPosition = start;
        this.targetPathPosition = target;
        this.pathfinderConfiguration= configuration;
        this.navigationPointProvider= provider;
        this.environmentContext = environmentContext;
    }

    @Override public PathPosition getStartPathPosition() { return startPathPosition; }
    @Override public PathPosition getTargetPathPosition() { return targetPathPosition; }
    @Override public PathfinderConfiguration getPathfinderConfiguration() { return pathfinderConfiguration; }
    @Override public NavigationPointProvider getNavigationPointProvider() { return navigationPointProvider; }
    @Override public Map<String, Object> getSharedData() { return sharedData; }
    @Override public EnvironmentContext getEnvironmentContext() { return environmentContext; }
}
