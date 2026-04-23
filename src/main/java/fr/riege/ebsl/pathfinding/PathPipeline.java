package fr.riege.ebsl.pathfinding;

import fr.riege.ebsl.pathfinding.debug.PathVisualizer;
import fr.riege.ebsl.pathfinding.movement.PathSmoother;
import fr.riege.ebsl.pathfinding.movement.WalkabilityChecker;
import fr.riege.ebsl.pathfinding.pathfinder.AStarPathfinder;
import fr.riege.ebsl.pathfinding.pathing.NeighborStrategies;
import fr.riege.ebsl.pathfinding.pathing.configuration.PathfinderConfiguration;
import fr.riege.ebsl.pathfinding.pathing.processing.impl.MinecraftPathProcessor;
import fr.riege.ebsl.pathfinding.provider.impl.MinecraftNavigationProvider;
import fr.riege.ebsl.pathfinding.wrapper.PathPosition;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

final class PathPipeline {
    private static final fr.riege.ebsl.pathfinding.pathing.heuristic.IHeuristicStrategy ZERO_HEURISTIC =
        new fr.riege.ebsl.pathfinding.pathing.heuristic.LinearHeuristicStrategy();

    private static final double[][] LOS_OFFSETS = {
        {0.05, 0.05}, {0.05, 0.95}, {0.95, 0.05}, {0.95, 0.95}
    };

    private static final long MASK_Y = 0xFFFL;
    private static final long MASK_XZ = 0x3FFFFFFL;
    private static final int SHIFT_Z = 12;
    private static final int SHIFT_X = 38;

    private static final double INTERMEDIATE_SPACING = 4.0;

    private PathPipeline() {
    }

    static PathfinderConfiguration createWalkPathfinderConfiguration(WalkabilityChecker checker, boolean async) {
        return PathfinderConfiguration.builder()
            .provider(new MinecraftNavigationProvider(checker))
            .processors(List.of(new MinecraftPathProcessor(checker,
                PathfinderConfig.PATHFINDER_MAX_JUMP_HEIGHT.get())))
            .neighborStrategy(NeighborStrategies.horizontalDiagonalAndVertical(
                PathfinderConfig.PATHFINDER_MAX_JUMP_HEIGHT.get()))
            .maxIterations(300000)
            .maxLength(25000)
            .async(async)
            .fallback(true)
            .build();
    }

    static int resolveStartY(WalkabilityChecker checker, double playerX, double playerY, double playerZ) {
        int x = Mth.floor(playerX);
        int y = Mth.floor(playerY);
        int z = Mth.floor(playerZ);
        if (checker == null) {
            return y;
        }
        if (checker.isPassable(x, y, z)) {
            return y;
        }
        if (checker.isPassable(x, y + 1, z)) {
            return y + 1;
        }
        return Mth.ceil(playerY);
    }

    static List<Node> buildLinearFlyPath(PathPosition start, PathPosition target) {
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
            nodes.add(makeNode(x0, y0, z0, Node.MoveType.FLY));
            return nodes;
        }

        for (int i = 0; i <= steps; i++) {
            double t = (double) i / steps;
            int nx = (int) Math.round(x0 + t * (x1 - x0));
            int ny = (int) Math.round(y0 + t * (y1 - y0));
            int nz = (int) Math.round(z0 + t * (z1 - z0));
            nodes.add(makeNode(nx, ny, nz, Node.MoveType.FLY));
        }
        return nodes;
    }

    static List<Node> buildLinearWalkPath(PathPosition start, PathPosition target) {
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
            nodes.add(makeNode(x0, y0, z0, Node.MoveType.WALK));
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
            nodes.add(makeNode(nx, ny, nz, Node.MoveType.WALK));
        }

        for (int i = 1; i < nodes.size(); i++) {
            nodes.get(i).moveType = inferMoveType(nodes.get(i - 1).position, nodes.get(i).position);
        }
        return nodes;
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

    static ProcessedPath processWalkPath(Collection<PathPosition> positions,
                                         PathfinderConfiguration config,
                                         WalkabilityChecker checker) {
        List<Node> nodes = toNodeList(positions, config);
        List<Node> smoothed = PathSmoother.smooth(nodes, checker);
        List<Node> keynodes = collapseAscendingStacks(smoothed);
        for (Node node : keynodes) {
            node.isKeynode = true;
        }
        List<Node> navigationPath = insertIntermediates(keynodes, checker);
        return new ProcessedPath(nodes, navigationPath, computePathLength(nodes));
    }

    static void pushExploredNodesToVisualizer(AStarPathfinder pathfinder) {
        if (pathfinder.getClosedSet() == null) {
            return;
        }
        for (long key : pathfinder.getClosedSet()) {
            PathVisualizer.addExplored(unpackX(key), unpackY(key), unpackZ(key));
        }
    }

    private static Node makeNode(int x, int y, int z, Node.MoveType moveType) {
        PathPosition position = new PathPosition(x, y, z);
        Node node = new Node(position, position, position,
            new fr.riege.ebsl.pathfinding.pathing.heuristic.HeuristicWeights(0, 0, 0, 0),
            ZERO_HEURISTIC, 0);
        node.moveType = moveType;
        return node;
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

    private static List<Node> toNodeList(Collection<PathPosition> positions, PathfinderConfiguration config) {
        if (positions == null || positions.isEmpty()) {
            return Collections.emptyList();
        }

        List<PathPosition> positionList = positions instanceof List<?>
            ? (List<PathPosition>) positions
            : new ArrayList<>(positions);

        PathPosition start = positionList.getFirst();
        PathPosition target = positionList.getLast();
        PathfinderConfiguration effectiveConfig = config != null ? config : PathfinderConfiguration.DEFAULT;

        List<Node> nodes = new ArrayList<>(positionList.size());
        PathPosition previous = null;
        for (PathPosition position : positionList) {
            Node node = new Node(position, start, target,
                effectiveConfig.heuristicWeights, effectiveConfig.heuristicStrategy, nodes.size());
            if (previous != null) {
                node.moveType = inferMoveType(previous, position);
            }
            nodes.add(node);
            previous = position;
        }
        return nodes;
    }

    private static Node.MoveType inferMoveType(PathPosition from, PathPosition to) {
        int dy = to.flooredY() - from.flooredY();
        int dx = Math.abs(to.flooredX() - from.flooredX());
        int dz = Math.abs(to.flooredZ() - from.flooredZ());
        if (dy > 1) {
            return Node.MoveType.STEP_UP;
        }
        if (dy < 0) {
            return Node.MoveType.FALL;
        }
        if (dx + dz >= 2) {
            return Node.MoveType.WALK_DIAGONAL;
        }
        return Node.MoveType.WALK;
    }

    private static double computePathLength(List<Node> path) {
        double total = 0;
        for (int i = 0; i < path.size() - 1; i++) {
            PathPosition from = path.get(i).position;
            PathPosition to = path.get(i + 1).position;
            total += from.distance(to);
        }
        return total;
    }

    private static List<Node> insertIntermediates(List<Node> keynodes, WalkabilityChecker checker) {
        if (keynodes.size() < 2) {
            return keynodes;
        }

        List<Node> result = new ArrayList<>();
        for (int i = 0; i < keynodes.size() - 1; i++) {
            appendDistinct(result, keynodes.get(i));
            Node from = keynodes.get(i);
            Node to = keynodes.get(i + 1);
            if (from.position.flooredY() != to.position.flooredY()) {
                continue;
            }

            double dx = to.position.centeredX() - from.position.centeredX();
            double dz = to.position.centeredZ() - from.position.centeredZ();
            double dist = Math.sqrt(dx * dx + dz * dz);
            if (dist <= INTERMEDIATE_SPACING) {
                continue;
            }

            int steps = (int) (dist / INTERMEDIATE_SPACING);
            int y = from.position.flooredY();
            for (int step = 1; step < steps; step++) {
                double t = (double) step / steps;
                int ix = (int) Math.floor(from.position.centeredX() + dx * t);
                int iz = (int) Math.floor(from.position.centeredZ() + dz * t);
                if (checker != null
                    && (checker.getTopY(ix, y, iz) > 0.0 || checker.getTopY(ix, y + 1, iz) > 0.0)) {
                    continue;
                }
                Node intermediate = new Node(new PathPosition(ix, y, iz));
                intermediate.moveType = from.moveType;
                appendDistinct(result, intermediate);
            }
        }
        appendDistinct(result, keynodes.getLast());
        return result;
    }

    private static boolean appendDistinct(List<Node> list, Node node) {
        if (node == null) {
            return false;
        }
        if (list.isEmpty()) {
            list.add(node);
            return true;
        }
        Node last = list.getLast();
        if (sameBlock(last, node)) {
            return false;
        }
        list.add(node);
        return true;
    }

    private static List<Node> collapseAscendingStacks(List<Node> nodes) {
        if (nodes.size() <= 2) {
            return nodes;
        }

        List<Node> result = new ArrayList<>(nodes.size());
        result.add(nodes.getFirst());

        for (int i = 1; i < nodes.size() - 1; i++) {
            Node current = nodes.get(i);
            Node last = result.getLast();
            if (sameXZ(last, current)
                && current.position.flooredY() >= last.position.flooredY()
                && isAscendingMove(last.moveType, current.moveType)) {
                result.set(result.size() - 1, current);
                continue;
            }
            result.add(current);
        }

        appendDistinct(result, nodes.getLast());
        return result;
    }

    private static boolean sameXZ(Node a, Node b) {
        return a.position.flooredX() == b.position.flooredX()
            && a.position.flooredZ() == b.position.flooredZ();
    }

    private static boolean isAscendingMove(Node.MoveType a, Node.MoveType b) {
        return a == Node.MoveType.STEP_UP || b == Node.MoveType.STEP_UP
            || a == Node.MoveType.JUMP || b == Node.MoveType.JUMP
            || a == Node.MoveType.PARKOUR || b == Node.MoveType.PARKOUR;
    }

    private static boolean sameBlock(Node a, Node b) {
        return a.position.flooredX() == b.position.flooredX()
            && a.position.flooredY() == b.position.flooredY()
            && a.position.flooredZ() == b.position.flooredZ();
    }

    private static int unpackX(long key) {
        long raw = (key >> SHIFT_X) & MASK_XZ;
        return (raw & (1L << 25)) != 0 ? (int) (raw | ~MASK_XZ) : (int) raw;
    }

    private static int unpackY(long key) {
        long raw = key & MASK_Y;
        return (raw & (1L << 11)) != 0 ? (int) (raw | ~MASK_Y) : (int) raw;
    }

    private static int unpackZ(long key) {
        long raw = (key >> SHIFT_Z) & MASK_XZ;
        return (raw & (1L << 25)) != 0 ? (int) (raw | ~MASK_XZ) : (int) raw;
    }
}
