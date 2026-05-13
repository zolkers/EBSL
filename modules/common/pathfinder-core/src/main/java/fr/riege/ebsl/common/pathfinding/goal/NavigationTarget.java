package fr.riege.ebsl.common.pathfinding.goal;

/**
 * Identifies the concrete target produced by a navigation goal.
 *
 * <p>The sealed hierarchy separates exact block targets from column-level targets while keeping request handling exhaustive.</p>
 */
public sealed interface NavigationTarget {
    record Block(int x, int y, int z) implements NavigationTarget {}
    record Column(int x, int z) implements NavigationTarget {}
}
