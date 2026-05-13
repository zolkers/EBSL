package fr.riege.ebsl.common.pathfinding.provider;

record NavigationPointImpl(
    boolean traversable,
    boolean floor,
    double floorLevel,
    boolean climbable,
    boolean liquid
) implements NavigationPoint {
    @Override public boolean isTraversable() { return traversable; }
    @Override public boolean hasFloor() { return floor; }
    @Override public double getFloorLevel() { return floorLevel; }
    @Override public boolean isClimbable() { return climbable; }
    @Override public boolean isLiquid() { return liquid; }
}
