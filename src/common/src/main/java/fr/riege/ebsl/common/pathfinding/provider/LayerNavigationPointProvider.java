package fr.riege.ebsl.common.pathfinding.provider;

import fr.riege.ebsl.common.pathfinding.movement.WalkabilityChecker;
import fr.riege.ebsl.common.pathfinding.pathing.context.EnvironmentContext;
import fr.riege.ebsl.common.pathfinding.wrapper.PathPosition;

public final class LayerNavigationPointProvider implements NavigationPointProvider {
    private static final NavigationPoint BLOCKED = point(false, false, 0.0, false, false);

    private final WalkabilityChecker checker;

    public LayerNavigationPointProvider(WalkabilityChecker checker) {
        this.checker = checker;
    }

    public WalkabilityChecker checker() {
        return checker;
    }

    @Override
    public NavigationPoint getNavigationPoint(PathPosition position, EnvironmentContext environmentContext) {
        int x = position.flooredX();
        int y = position.flooredY();
        int z = position.flooredZ();

        if (!checker.world().isLoaded(x, y, z)) {
            return BLOCKED;
        }

        boolean lowPartialFeet = checker.isLowPartialSupport(x, y, z);
        boolean canPassFeet = lowPartialFeet || checker.isPassable(x, y, z);
        boolean canPassHead = checker.isPassable(x, y + 1, z);
        boolean liquid = checker.isWater(x, y, z);
        boolean climbable = checker.isClimbable(x, y, z);
        boolean floor = lowPartialFeet || liquid || checker.hasWalkableTop(x, y - 1, z);
        boolean dangerous = checker.isDangerous(x, y, z) || checker.isDangerous(x, y + 1, z);
        double floorLevel = floorLevel(x, y, z, liquid, lowPartialFeet);

        return point(canPassFeet && canPassHead && !dangerous, floor, floorLevel, climbable, liquid);
    }

    private double floorLevel(int x, int y, int z, boolean liquid, boolean lowPartialFeet) {
        if (liquid) return y + 0.5;
        if (lowPartialFeet) return y + checker.getTopY(x, y, z);
        double belowTop = checker.getTopY(x, y - 1, z);
        return belowTop <= 0.0 ? y - 1.0 : y - 1.0 + belowTop;
    }

    private static NavigationPoint point(boolean traversable, boolean floor, double floorLevel,
                                         boolean climbable, boolean liquid) {
        return new NavigationPoint() {
            @Override public boolean isTraversable() { return traversable; }
            @Override public boolean hasFloor() { return floor; }
            @Override public double getFloorLevel() { return floorLevel; }
            @Override public boolean isClimbable() { return climbable; }
            @Override public boolean isLiquid() { return liquid; }
        };
    }
}
