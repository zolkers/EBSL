package fr.riege.ebsl.pathfinding.pathing.configuration;

import fr.riege.ebsl.pathfinding.pathing.context.EnvironmentContext;
import fr.riege.ebsl.pathfinding.provider.NavigationPoint;
import fr.riege.ebsl.pathfinding.provider.NavigationPointProvider;
import fr.riege.ebsl.pathfinding.wrapper.PathPosition;

/** Package-private default provider: everything is traversable. */
final class DefaultNavigationPointProvider implements NavigationPointProvider {
    static final DefaultNavigationPointProvider INSTANCE = new DefaultNavigationPointProvider();
    private DefaultNavigationPointProvider() {}

    @Override
    public NavigationPoint getNavigationPoint(PathPosition position, EnvironmentContext ctx) {
        return new NavigationPoint() {
            @Override public boolean isTraversable() { return true;  }
            @Override public boolean hasFloor()      { return true;  }
            @Override public double  getFloorLevel() { return 0.0;  }
            @Override public boolean isClimbable()   { return false; }
            @Override public boolean isLiquid()      { return false; }
        };
    }
}
