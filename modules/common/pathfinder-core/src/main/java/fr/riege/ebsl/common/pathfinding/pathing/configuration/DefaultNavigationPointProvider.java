package fr.riege.ebsl.common.pathfinding.pathing.configuration;

import fr.riege.ebsl.common.pathfinding.pathing.context.EnvironmentContext;
import fr.riege.ebsl.common.pathfinding.provider.NavigationPoint;
import fr.riege.ebsl.common.pathfinding.provider.NavigationPointImpl;
import fr.riege.ebsl.common.pathfinding.provider.NavigationPointProvider;
import fr.riege.ebsl.common.pathfinding.wrapper.PathPosition;

@SuppressWarnings("java:S6548")
final class DefaultNavigationPointProvider implements NavigationPointProvider {
    static final DefaultNavigationPointProvider INSTANCE = new DefaultNavigationPointProvider();
    private DefaultNavigationPointProvider() {}

    @Override
    public NavigationPoint getNavigationPoint(PathPosition position, EnvironmentContext ctx) {
        return NavigationPointImpl.OPEN_FLOOR;
    }
}
