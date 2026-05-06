package fr.riege.ebsl.common.pathfinding.provider;

import fr.riege.ebsl.common.pathfinding.pathing.context.EnvironmentContext;
import fr.riege.ebsl.common.pathfinding.wrapper.PathPosition;

public interface NavigationPointProvider {
    default NavigationPoint getNavigationPoint(PathPosition position) {
        return getNavigationPoint(position, null);
    }

    NavigationPoint getNavigationPoint(PathPosition position, EnvironmentContext environmentContext);
}
