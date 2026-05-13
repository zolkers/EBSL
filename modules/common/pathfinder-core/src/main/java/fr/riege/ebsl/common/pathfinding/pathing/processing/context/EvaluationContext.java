package fr.riege.ebsl.common.pathfinding.pathing.processing.context;

import fr.riege.ebsl.common.pathfinding.Node;
import fr.riege.ebsl.common.pathfinding.pathing.configuration.PathfinderConfiguration;
import fr.riege.ebsl.common.pathfinding.pathing.context.EnvironmentContext;
import fr.riege.ebsl.common.pathfinding.provider.NavigationPointProvider;
import fr.riege.ebsl.common.pathfinding.wrapper.PathPosition;
import java.util.Map;

public interface EvaluationContext {
    PathPosition getCurrentPathPosition();
    PathPosition getPreviousPathPosition();       
    int getCurrentNodeDepth();
    double getCurrentNodeHeuristicValue();
    double getPathCostToPreviousPosition();
    double getBaseTransitionCost();
    SearchContext getSearchContext();
    PathPosition getGrandparentPathPosition();    
    default PathPosition getGreatGrandparentPathPosition() { return null; }
    default Node.MoveType getCurrentMoveType() { return null; }

    default PathfinderConfiguration getPathfinderConfiguration() {
        return getSearchContext().getPathfinderConfiguration();
    }

    default NavigationPointProvider getNavigationPointProvider() {
        return getSearchContext().getNavigationPointProvider();
    }

    default Map<String, Object> getSharedData() {
        return getSearchContext().getSharedData();
    }

    default PathPosition getStartPathPosition() {
        return getSearchContext().getStartPathPosition();
    }

    default PathPosition getTargetPathPosition() {
        return getSearchContext().getTargetPathPosition();
    }

    default EnvironmentContext getEnvironmentContext() {
        return getSearchContext().getEnvironmentContext();
    }
}
