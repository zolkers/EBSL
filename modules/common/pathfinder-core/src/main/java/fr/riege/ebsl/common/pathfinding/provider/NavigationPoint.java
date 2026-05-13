package fr.riege.ebsl.common.pathfinding.provider;

/**
 * Defines the contract for {@code NavigationPoint} implementations.
 */
public interface NavigationPoint {
    boolean isTraversable();
    boolean hasFloor();
    double getFloorLevel();
    boolean isClimbable();
    boolean isLiquid();
}
