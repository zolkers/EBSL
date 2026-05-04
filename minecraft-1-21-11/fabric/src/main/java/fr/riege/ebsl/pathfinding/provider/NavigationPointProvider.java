package fr.riege.ebsl.pathfinding.provider;

import fr.riege.ebsl.pathfinding.pathing.context.EnvironmentContext;
import fr.riege.ebsl.pathfinding.wrapper.PathPosition;

public interface NavigationPointProvider {
    default NavigationPoint getNavigationPoint(PathPosition position) {
        return getNavigationPoint(position, null);
    }

    NavigationPoint getNavigationPoint(PathPosition position, EnvironmentContext environmentContext);
}
