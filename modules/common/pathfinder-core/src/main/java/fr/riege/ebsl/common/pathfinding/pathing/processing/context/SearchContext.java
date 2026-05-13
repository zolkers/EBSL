package fr.riege.ebsl.common.pathfinding.pathing.processing.context;

import fr.riege.ebsl.common.pathfinding.pathing.configuration.PathfinderConfiguration;
import fr.riege.ebsl.common.pathfinding.pathing.context.EnvironmentContext;
import fr.riege.ebsl.common.pathfinding.provider.NavigationPointProvider;
import fr.riege.ebsl.common.pathfinding.wrapper.PathPosition;
import java.util.Map;

/**
 * Defines the contract for {@code SearchContext} implementations.
 */
public interface SearchContext {
    PathPosition getStartPathPosition();
    PathPosition getTargetPathPosition();
    PathfinderConfiguration getPathfinderConfiguration();
    NavigationPointProvider getNavigationPointProvider();
    Map<String, Object> getSharedData();
    EnvironmentContext getEnvironmentContext();
}
