package fr.riege.ebsl.common.pathfinding.quality;

import fr.riege.ebsl.common.pathfinding.Node;
import fr.riege.ebsl.common.pathfinding.movement.WalkabilityChecker;
import fr.riege.ebsl.common.pathfinding.parkour.ParkourGeometry;
import fr.riege.ebsl.common.pathfinding.pathing.context.EnvironmentContext;
import fr.riege.ebsl.common.pathfinding.provider.NavigationPoint;
import fr.riege.ebsl.common.pathfinding.provider.NavigationPointProvider;
import fr.riege.ebsl.common.pathfinding.settings.PathfinderSettings;
import fr.riege.ebsl.common.pathfinding.wrapper.PathPosition;

public final class PathMoveClassifier {
    private static final double STEP_DOWN_DY_THRESHOLD = -1.1;

    private PathMoveClassifier() {
    }

    public static Node.MoveType classify(PathPosition previous,
                                         PathPosition current,
                                         NavigationPointProvider provider,
                                         EnvironmentContext environmentContext,
                                         WalkabilityChecker checker) {
        if (previous == null || current == null) {
            return Node.MoveType.WALK;
        }

        NavigationPoint previousPoint = provider == null ? null : provider.getNavigationPoint(previous, environmentContext);
        NavigationPoint currentPoint = provider == null ? null : provider.getNavigationPoint(current, environmentContext);
        if ((currentPoint != null && currentPoint.isLiquid()) || (previousPoint != null && previousPoint.isLiquid())) {
            return Node.MoveType.SWIM;
        }
        if ((currentPoint != null && currentPoint.isClimbable()) || (previousPoint != null && previousPoint.isClimbable())) {
            return Node.MoveType.CLIMB;
        }

        int dx = current.flooredX() - previous.flooredX();
        int dz = current.flooredZ() - previous.flooredZ();
        int absDx = Math.abs(dx);
        int absDz = Math.abs(dz);
        double dy = floorLevel(current, currentPoint, checker) - floorLevel(previous, previousPoint, checker);

        if (checker != null && checker.world().requiresJumpForStep(
            current.flooredX(), current.flooredY(), current.flooredZ(), Integer.signum(dx), Integer.signum(dz))) {
            return Node.MoveType.JUMP;
        }
        if (ParkourGeometry.isCandidateOffset(dx, dz)) {
            return Node.MoveType.PARKOUR;
        }
        PathfinderSettings settings = PathfinderSettings.instance();
        if (dy > settings.partialAscentThreshold.value()) {
            return Node.MoveType.STEP_UP;
        }
        if (dy < settings.descentThreshold.value()) {
            return dy >= STEP_DOWN_DY_THRESHOLD ? Node.MoveType.STEP_DOWN : Node.MoveType.FALL;
        }
        if (absDx + absDz >= 2) {
            return Node.MoveType.WALK_DIAGONAL;
        }
        return Node.MoveType.WALK;
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
            return y + 0.5;
        }
        double topY = checker.getTopY(x, y - 1, z);
        return topY <= 0.0 ? y - 1.0 : y - 1.0 + topY;
    }
}
