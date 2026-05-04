package fr.riege.ebsl.pathfinding.pathing.processing.context;

import fr.riege.ebsl.pathfinding.pathing.configuration.PathfinderConfiguration;
import fr.riege.ebsl.pathfinding.pathing.context.EnvironmentContext;
import fr.riege.ebsl.pathfinding.provider.NavigationPointProvider;
import fr.riege.ebsl.pathfinding.wrapper.PathPosition;
import java.util.Map;

public interface SearchContext {
    PathPosition getStartPathPosition();
    PathPosition getTargetPathPosition();
    PathfinderConfiguration getPathfinderConfiguration();
    NavigationPointProvider getNavigationPointProvider();
    Map<String, Object> getSharedData();
    EnvironmentContext getEnvironmentContext();
}
