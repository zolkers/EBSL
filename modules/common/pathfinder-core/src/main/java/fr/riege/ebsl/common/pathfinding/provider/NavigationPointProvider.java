package fr.riege.ebsl.common.pathfinding.provider;

import fr.riege.ebsl.common.pathfinding.pathing.context.EnvironmentContext;
import fr.riege.ebsl.common.pathfinding.wrapper.PathPosition;

/**
 * Resolves navigation points from path positions.
 *
 * <p>Providers are the world abstraction used by planners to query traversability, floors, liquids, and climbable blocks.</p>
 */
public interface NavigationPointProvider {
    /**
     * Returns the navigation point resolved for the supplied position.
 *
     * @param position the position to inspect
     * @return the value defined by this contract
     */
    default NavigationPoint getNavigationPoint(PathPosition position) {
        return getNavigationPoint(position, null);
    }

    /**
     * Returns the navigation point resolved for the supplied position.
 *
     * @param position the position to inspect
     * @param environmentContext optional environment metadata for the lookup
     * @return the value defined by this contract
     */
    NavigationPoint getNavigationPoint(PathPosition position, EnvironmentContext environmentContext);
}
