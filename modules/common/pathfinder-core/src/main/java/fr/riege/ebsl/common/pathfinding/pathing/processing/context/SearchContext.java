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
