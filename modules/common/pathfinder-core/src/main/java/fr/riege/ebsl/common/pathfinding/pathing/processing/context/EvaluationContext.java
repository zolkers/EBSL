package fr.riege.ebsl.common.pathfinding.pathing.processing.context;

import fr.riege.ebsl.common.pathfinding.Node;
import fr.riege.ebsl.common.pathfinding.pathing.configuration.PathfinderConfiguration;
import fr.riege.ebsl.common.pathfinding.pathing.context.EnvironmentContext;
import fr.riege.ebsl.common.pathfinding.provider.NavigationPointProvider;
import fr.riege.ebsl.common.pathfinding.wrapper.PathPosition;
import java.util.Map;

/**
 * Describes one candidate transition currently being evaluated by path processors.
 *
 * <p>The context exposes ancestry, movement type, costs, configuration, environment, and shared search data for validators and cost models.</p>
 */
public interface EvaluationContext {
    /**
     * Returns the candidate position currently being evaluated.
 *
     * @return the value defined by this contract
     */
    PathPosition getCurrentPathPosition();
    /**
     * Returns the predecessor position for the candidate transition.
 *
     * @return the value defined by this contract
     */
    PathPosition getPreviousPathPosition();
    /**
     * Returns the search depth of the candidate node.
 *
     * @return the value defined by this contract
     */
    int getCurrentNodeDepth();
    /**
     * Returns the heuristic value already calculated for the candidate node.
 *
     * @return the value defined by this contract
     */
    double getCurrentNodeHeuristicValue();
    /**
     * Returns the accumulated path cost up to the predecessor position.
 *
     * @return the value defined by this contract
     */
    double getPathCostToPreviousPosition();
    /**
     * Returns the base movement cost before processor contributions are applied.
 *
     * @return the value defined by this contract
     */
    double getBaseTransitionCost();
    /**
     * Returns the search-wide context backing this evaluation.
 *
     * @return the value defined by this contract
     */
    SearchContext getSearchContext();
    /**
     * Returns the predecessor of the previous path position, when available.
 *
     * @return the value defined by this contract
     */
    PathPosition getGrandparentPathPosition();
    /**
     * Returns the third ancestor of the candidate path position, when available.
 *
     * @return the value defined by this contract
     */
    default PathPosition getGreatGrandparentPathPosition() { return null; }
    /**
     * Returns the movement type assigned to the candidate transition, when known.
 *
     * @return the value defined by this contract
     */
    default Node.MoveType getCurrentMoveType() { return null; }

    /**
     * Returns the pathfinder configuration for the active search.
 *
     * @return the value defined by this contract
     */
    default PathfinderConfiguration getPathfinderConfiguration() {
        return getSearchContext().getPathfinderConfiguration();
    }

    /**
     * Returns the navigation point provider used by the active search.
 *
     * @return the value defined by this contract
     */
    default NavigationPointProvider getNavigationPointProvider() {
        return getSearchContext().getNavigationPointProvider();
    }

    /**
     * Returns the shared mutable data map for cooperating processors.
 *
     * @return the value defined by this contract
     */
    default Map<String, Object> getSharedData() {
        return getSearchContext().getSharedData();
    }

    /**
     * Returns the start position for the active search.
 *
     * @return the value defined by this contract
     */
    default PathPosition getStartPathPosition() {
        return getSearchContext().getStartPathPosition();
    }

    /**
     * Returns the target position for the active search.
 *
     * @return the value defined by this contract
     */
    default PathPosition getTargetPathPosition() {
        return getSearchContext().getTargetPathPosition();
    }

    /**
     * Returns optional environment metadata for the active search.
 *
     * @return the value defined by this contract
     */
    default EnvironmentContext getEnvironmentContext() {
        return getSearchContext().getEnvironmentContext();
    }
}
