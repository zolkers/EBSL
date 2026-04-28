package fr.riege.ebsl.pathfinding;

import fr.riege.ebsl.pathfinding.pathing.heuristic.IHeuristicStrategy;
import fr.riege.ebsl.pathfinding.pathing.heuristic.LinearHeuristicStrategy;
import fr.riege.ebsl.pathfinding.parkour.ParkourGeometry;
import fr.riege.ebsl.pathfinding.wrapper.PathPosition;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

final class PathGeometry {
    private static final IHeuristicStrategy ZERO_HEURISTIC =
        new LinearHeuristicStrategy();

    private static final double[][] LOS_OFFSETS = {
        {0.05, 0.05}, {0.05, 0.95}, {0.95, 0.05}, {0.95, 0.95}
    };

    private PathGeometry() {
    }

    static List<Node> buildLinearFlyPath(PathPosition start, PathPosition target) {
        return buildLinearPath(start, target, Node.MoveType.FLY, false);
    }

    static List<Node> buildLinearWalkPath(PathPosition start, PathPosition target) {
        return buildLinearPath(start, target, Node.MoveType.WALK, true);
    }

    static List<Node> smoothFlyPath(Minecraft mc, List<Node> path) {
        if (mc.level == null || path.size() < 3) {
            return path;
        }

        List<Node> smoothed = new ArrayList<>();
        smoothed.add(path.getFirst());
        int lowerIdx = 0;

        while (lowerIdx < path.size() - 1) {
            PathPosition from = path.get(lowerIdx).position;
            int lastValid = lowerIdx + 1;

            for (int upper = lowerIdx + 2; upper < path.size(); upper++) {
                PathPosition to = path.get(upper).position;
                if (hasFreePath(mc, from, to)) {
                    lastValid = upper;
                } else {
                    break;
                }
            }

            smoothed.add(path.get(lastValid));
            lowerIdx = lastValid;
        }

        return smoothed;
    }

    static Node createNode(int x, int y, int z, Node.MoveType moveType) {
        PathPosition position = new PathPosition(x, y, z);
        Node node = new Node(position, position, position,
            new fr.riege.ebsl.pathfinding.pathing.heuristic.HeuristicWeights(0, 0, 0, 0),
            ZERO_HEURISTIC, 0);
        node.moveType = moveType;
        return node;
    }

    static Node.MoveType inferMoveType(PathPosition from, PathPosition to) {
        int dy = to.flooredY() - from.flooredY();
        int dx = Math.abs(to.flooredX() - from.flooredX());
        int dz = Math.abs(to.flooredZ() - from.flooredZ());
        if (dy > 1) {
            return Node.MoveType.STEP_UP;
        }
        if (dy < 0) {
            return Node.MoveType.FALL;
        }
        if (ParkourGeometry.isCandidateOffset(to.flooredX() - from.flooredX(), to.flooredZ() - from.flooredZ())) {
            return Node.MoveType.PARKOUR;
        }
        if (dx + dz >= 2) {
            return Node.MoveType.WALK_DIAGONAL;
        }
        return Node.MoveType.WALK;
    }

    private static List<Node> buildLinearPath(PathPosition start, PathPosition target,
                                              Node.MoveType baseMoveType, boolean inferMoves) {
        List<Node> nodes = new ArrayList<>();
        int x0 = start.flooredX();
        int y0 = start.flooredY();
        int z0 = start.flooredZ();
        int x1 = target.flooredX();
        int y1 = target.flooredY();
        int z1 = target.flooredZ();
        int dx = Math.abs(x1 - x0);
        int dy = Math.abs(y1 - y0);
        int dz = Math.abs(z1 - z0);
        int steps = Math.max(Math.max(dx, dy), dz);

        if (steps == 0) {
            nodes.add(createNode(x0, y0, z0, baseMoveType));
            return nodes;
        }

        for (int i = 0; i <= steps; i++) {
            double t = (double) i / steps;
            int nx = (int) Math.round(x0 + t * (x1 - x0));
            int ny = (int) Math.round(y0 + t * (y1 - y0));
            int nz = (int) Math.round(z0 + t * (z1 - z0));
            if (!nodes.isEmpty()) {
                Node previousNode = nodes.getLast();
                if (previousNode.position.flooredX() == nx
                    && previousNode.position.flooredY() == ny
                    && previousNode.position.flooredZ() == nz) {
                    continue;
                }
            }
            nodes.add(createNode(nx, ny, nz, baseMoveType));
        }

        if (inferMoves) {
            for (int i = 1; i < nodes.size(); i++) {
                nodes.get(i).moveType = inferMoveType(nodes.get(i - 1).position, nodes.get(i).position);
            }
        }
        return nodes;
    }

    private static boolean hasFreePath(Minecraft mc, PathPosition from, PathPosition to) {
        double fx = from.flooredX();
        double fz = from.flooredZ();
        double tx = to.flooredX();
        double tz = to.flooredZ();
        double fy = from.flooredY();
        double ty = to.flooredY();

        double[] checkY = {fy + 0.1, fy + 0.9, fy + 1.1, fy + 1.9};
        double[] checkTargetY = {ty + 0.1, ty + 0.9, ty + 1.1, ty + 1.9};

        for (double[] xzOff : LOS_OFFSETS) {
            for (int height = 0; height < checkY.length; height++) {
                Vec3 rayStart = new Vec3(fx + xzOff[0], checkY[height], fz + xzOff[1]);
                Vec3 rayEnd = new Vec3(tx + xzOff[0], checkTargetY[height], tz + xzOff[1]);
                HitResult hit = mc.level.clip(new ClipContext(
                    rayStart, rayEnd,
                    ClipContext.Block.COLLIDER,
                    ClipContext.Fluid.NONE,
                    mc.player));
                if (hit.getType() == HitResult.Type.BLOCK) {
                    return false;
                }
            }
        }
        return true;
    }
}
