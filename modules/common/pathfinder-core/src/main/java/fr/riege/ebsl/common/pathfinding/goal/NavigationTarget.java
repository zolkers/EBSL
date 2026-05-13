package fr.riege.ebsl.common.pathfinding.goal;

/**
 * Defines the contract for {@code NavigationTarget} implementations.
 */
public sealed interface NavigationTarget {
    record Block(int x, int y, int z) implements NavigationTarget {}
    record Column(int x, int z) implements NavigationTarget {}
}
