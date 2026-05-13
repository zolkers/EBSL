package fr.riege.ebsl.common.pathfinding.movement;

import fr.riege.ebsl.common.pathfinding.Node;
import fr.riege.ebsl.common.pathfinding.parkour.ParkourGeometry;
import fr.riege.ebsl.common.pathfinding.provider.NavigationPoint;
import fr.riege.ebsl.common.pathfinding.settings.PathfinderSettings;
import fr.riege.ebsl.common.pathfinding.wrapper.PathPosition;

public enum DefaultMovementTypeClassifier implements MovementTypeClassifier {
    INSTANCE;

    private static final double STEP_DOWN_DY_THRESHOLD = -1.1;

    @Override
    public Node.MoveType classify(MovementClassificationContext context) {
        if (context == null || context.previous() == null || context.current() == null) {
            return Node.MoveType.WALK;
        }

        PathPosition previous = context.previous();
        PathPosition current = context.current();
        NavigationPoint previousPoint = context.provider() == null
            ? null
            : context.provider().getNavigationPoint(previous, context.environmentContext());
        NavigationPoint currentPoint = context.provider() == null
            ? null
            : context.provider().getNavigationPoint(current, context.environmentContext());
        WalkabilityChecker checker = context.checker();

        if (isSwim(previous, current, previousPoint, currentPoint, checker)) {
            return Node.MoveType.SWIM;
        }
        if (isClimb(previousPoint, currentPoint)) {
            return Node.MoveType.CLIMB;
        }

        int dx = current.flooredX() - previous.flooredX();
        int dz = current.flooredZ() - previous.flooredZ();
        double dy = floorLevel(current, currentPoint, checker) - floorLevel(previous, previousPoint, checker);

        if (checker != null && checker.world().requiresJumpForStep(
            current.flooredX(), current.flooredY(), current.flooredZ(), Integer.signum(dx), Integer.signum(dz))) {
            return Node.MoveType.JUMP;
        }
        if (isParkourMove(previous, current, checker, dx, dz)) {
            return Node.MoveType.PARKOUR;
        }
        return classifyGroundMove(dx, dz, dy);
    }

    private static boolean isSwim(PathPosition previous, PathPosition current,
                                  NavigationPoint previousPoint, NavigationPoint currentPoint,
                                  WalkabilityChecker checker) {
        if ((currentPoint != null && currentPoint.isLiquid()) || (previousPoint != null && previousPoint.isLiquid())) {
            return true;
        }
        if (checker == null) {
            return false;
        }
        return isSwimPosition(checker, previous) || isSwimPosition(checker, current);
    }

    private static boolean isClimb(NavigationPoint previousPoint, NavigationPoint currentPoint) {
        return isClimbable(previousPoint) || isClimbable(currentPoint);
    }

    private static boolean isClimbable(NavigationPoint point) {
        return point != null && point.isClimbable();
    }

    private static Node.MoveType classifyGroundMove(int dx, int dz, double dy) {
        PathfinderSettings settings = PathfinderSettings.instance();
        if (dy > settings.partialAscentThreshold.value()) {
            return Node.MoveType.STEP_UP;
        }
        if (dy < settings.descentThreshold.value()) {
            return classifyDescent(dy);
        }
        return Math.abs(dx) + Math.abs(dz) >= 2 ? Node.MoveType.WALK_DIAGONAL : Node.MoveType.WALK;
    }

    private static Node.MoveType classifyDescent(double dy) {
        return dy >= STEP_DOWN_DY_THRESHOLD ? Node.MoveType.STEP_DOWN : Node.MoveType.FALL;
    }

    private static boolean isParkourMove(PathPosition previous, PathPosition current,
                                         WalkabilityChecker checker, int dx, int dz) {
        if (!ParkourGeometry.isCandidateOffset(dx, dz)) {
            return false;
        }
        if (checker == null) {
            return true;
        }
        int distance = ParkourGeometry.distanceBlocks(dx, dz);
        if (distance <= 1 || !hasWalkableSupport(checker, previous) || !hasWalkableSupport(checker, current)) {
            return false;
        }
        int checks = Math.max(2, distance * 2);
        for (int step = 1; step < checks; step++) {
            double t = (double) step / checks;
            int x = (int) Math.floor(previous.centeredX() + (current.centeredX() - previous.centeredX()) * t);
            int z = (int) Math.floor(previous.centeredZ() + (current.centeredZ() - previous.centeredZ()) * t);
            if (!checker.isWalkable(x, Math.min(previous.flooredY(), current.flooredY()), z)) {
                return true;
            }
        }
        return false;
    }

    private static boolean hasWalkableSupport(WalkabilityChecker checker, PathPosition position) {
        return checker.isWalkable(position.flooredX(), position.flooredY(), position.flooredZ());
    }

    private static boolean isSwimPosition(WalkabilityChecker checker, PathPosition position) {
        int x = position.flooredX();
        int y = position.flooredY();
        int z = position.flooredZ();
        return checker.isWater(x, y, z)
            || (checker.isPassable(x, y, z) && checker.isWater(x, y - 1, z));
    }

    private static double floorLevel(PathPosition position, NavigationPoint point, WalkabilityChecker checker) {
        if (point != null) {
            return point.getFloorLevel();
        }
        if (checker == null) {
            return position.flooredY();
        }
        int x = position.flooredX();
        int y = position.flooredY();
        int z = position.flooredZ();
        if (checker.isWater(x, y, z)) {
            return y + 0.5;
        }
        if (checker.isLowPartialSupport(x, y, z)) {
            return y + checker.getTopY(x, y, z);
        }
        double topY = checker.getTopY(x, y - 1, z);
        return topY <= 0.0 ? y - 1.0 : y - 1.0 + topY;
    }
}
