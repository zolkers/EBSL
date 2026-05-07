package fr.riege.ebsl.common.pathfinding.provider;

public record NavigationPointImpl(
    boolean traversable,
    boolean floor,
    double floorLevel,
    boolean climbable,
    boolean liquid
) implements NavigationPoint {
    public static final NavigationPointImpl BLOCKED = new NavigationPointImpl(false, false, 0.0, false, false);
    public static final NavigationPointImpl OPEN_FLOOR = new NavigationPointImpl(true, true, 0.0, false, false);

    @Override public boolean isTraversable() { return traversable; }
    @Override public boolean hasFloor() { return floor; }
    @Override public double getFloorLevel() { return floorLevel; }
    @Override public boolean isClimbable() { return climbable; }
    @Override public boolean isLiquid() { return liquid; }
}
