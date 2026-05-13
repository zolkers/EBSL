package fr.riege.ebsl.common.pathfinding.provider;

import fr.riege.ebsl.common.pathfinding.movement.WalkabilityChecker;

/**
 * Resolves navigation points from a world-backed walkability checker.
 *
 * <p>This contract is used by movement processors that need both cached navigation-point lookups
 * and lower-level terrain checks, while still avoiding a dependency on the concrete provider
 * implementation.</p>
 */
public interface WorldNavigationPointProvider extends NavigationPointProvider {
    /**
     * Returns the walkability checker backing this provider.
     *
     * @return the walkability checker used for terrain queries
     */
    WalkabilityChecker checker();

    /**
     * Clears cached navigation-point lookups.
     */
    void clearCache();
}
