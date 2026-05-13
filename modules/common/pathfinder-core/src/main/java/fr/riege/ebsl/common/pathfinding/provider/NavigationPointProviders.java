package fr.riege.ebsl.common.pathfinding.provider;

import fr.riege.ebsl.common.pathfinding.movement.WalkabilityChecker;

import java.util.Objects;

/**
 * Creates navigation point providers behind provider contracts.
 */
public final class NavigationPointProviders {
    private NavigationPointProviders() {
    }

    /**
     * Creates a world-backed provider that resolves points through a walkability checker.
     *
     * @param checker the walkability checker to query
     * @return a world-backed navigation point provider
     */
    public static WorldNavigationPointProvider worldBacked(WalkabilityChecker checker) {
        return new LayerNavigationPointProvider(Objects.requireNonNull(checker, "checker"));
    }
}
