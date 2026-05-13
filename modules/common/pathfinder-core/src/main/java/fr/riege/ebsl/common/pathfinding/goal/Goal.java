package fr.riege.ebsl.common.pathfinding.goal;

/**
 * Defines the contract for {@code Goal} implementations.
 */
public interface Goal {
    boolean isInGoal(int x, int y, int z);

    double heuristic(int x, int y, int z);

    String debugName();

    NavigationTarget resolve(int px, int py, int pz);
}
