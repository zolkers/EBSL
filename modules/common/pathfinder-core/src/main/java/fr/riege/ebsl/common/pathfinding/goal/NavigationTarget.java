package fr.riege.ebsl.common.pathfinding.goal;

public sealed interface NavigationTarget {
    record Block(int x, int y, int z) implements NavigationTarget {}
    record Column(int x, int z) implements NavigationTarget {}
}
