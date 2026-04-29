package fr.riege.ebsl.pathfinding;

import fr.riege.ebsl.pathfinding.movement.PathSmoother;
import fr.riege.ebsl.pathfinding.movement.WalkabilityChecker;
import fr.riege.ebsl.pathfinding.settings.PathfinderSettings;
import fr.riege.ebsl.pathfinding.annotation.PathingStage;
import fr.riege.ebsl.pathfinding.movement.geometry.StairEntryClassifier;
import fr.riege.ebsl.pathfinding.parkour.ParkourGeometry;
import fr.riege.ebsl.pathfinding.pathing.configuration.PathfinderConfiguration;
import fr.riege.ebsl.pathfinding.wrapper.PathPosition;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@PathingStage(PathingStage.Stage.PATH_POST_PROCESSING)
final class WalkPathProcessor {
    private WalkPathProcessor() {
    }

    static ProcessedPath processWalkPath(Collection<PathPosition> positions,
                                         PathfinderConfiguration config,
                                         WalkabilityChecker checker) {
        List<Node> nodes = toNodeList(positions, config, checker);
        List<Node> smoothed = PathSmoother.smooth(nodes, checker);
        relabelMoveTypes(smoothed, checker);
        List<Node> keynodes = collapseAscendingStacks(smoothed);
        relabelMoveTypes(keynodes, checker);
        for (Node node : keynodes) {
            node.isKeynode = true;
        }
        List<Node> navigationPath = insertIntermediates(keynodes, checker);
        relabelMoveTypes(navigationPath, checker);
        return new ProcessedPath(nodes, navigationPath, computePathLength(nodes));
    }

    private static List<Node> toNodeList(Collection<PathPosition> positions, PathfinderConfiguration config,
                                          WalkabilityChecker checker) {
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
                node.moveType = inferMoveType(previous, position, checker);
                // Water always wins — intentionally overrides FALL/JUMP for nodes entering a pool.
                if (checker != null && isSwimPosition(checker, position)) {
                    node.moveType = Node.MoveType.SWIM;
                }
            }
            nodes.add(node);
            previous = position;
        }
        return nodes;
    }

    private static Node.MoveType inferMoveType(PathPosition previous, PathPosition current,
                                               WalkabilityChecker checker) {
        if (checker == null) {
            return PathGeometry.inferMoveType(previous, current);
        }

        double dy = floorLevel(checker, current) - floorLevel(checker, previous);
        int dx = Math.abs(current.flooredX() - previous.flooredX());
        int dz = Math.abs(current.flooredZ() - previous.flooredZ());
        if (isStairEntryRequiringJump(previous, current, checker)) {
            return Node.MoveType.JUMP;
        }
        if (isParkourMove(previous, current, checker, dx, dz)) {
            return Node.MoveType.PARKOUR;
        }
        if (dy > PathfinderSettings.instance().partialAscentThreshold.value()) {
            return Node.MoveType.STEP_UP;
        }
        if (dy < PathfinderSettings.instance().descentThreshold.value()) {
            return Node.MoveType.FALL;
        }
        if (dx + dz >= 2) {
            return Node.MoveType.WALK_DIAGONAL;
        }
        return Node.MoveType.WALK;
    }

    private static boolean isStairEntryRequiringJump(PathPosition previous, PathPosition current,
                                                     WalkabilityChecker checker) {
        int cx = current.flooredX();
        int cy = current.flooredY();
        int cz = current.flooredZ();
        BlockState support = checker.getState(cx, cy - 1, cz);
        int moveDx = Integer.compare(cx, previous.flooredX());
        int moveDz = Integer.compare(cz, previous.flooredZ());
        return StairEntryClassifier.requiresJump(support, moveDx, moveDz);
    }

    private static boolean isParkourMove(PathPosition previous, PathPosition current,
                                         WalkabilityChecker checker, int absDx, int absDz) {
        if (!ParkourGeometry.isCandidateOffset(
            current.flooredX() - previous.flooredX(),
            current.flooredZ() - previous.flooredZ())) {
            return false;
        }
        int distance = ParkourGeometry.distanceBlocks(
            current.flooredX() - previous.flooredX(),
            current.flooredZ() - previous.flooredZ());
        if (!hasWalkableSupport(checker, previous) || !hasWalkableSupport(checker, current)) {
            return false;
        }
        int checks = Math.max(2, distance * 2);
        for (int step = 1; step < checks; step++) {
            double t = (double) step / checks;
            int x = (int) Math.floor(previous.centeredX() + (current.centeredX() - previous.centeredX()) * t);
            int z = (int) Math.floor(previous.centeredZ() + (current.centeredZ() - previous.centeredZ()) * t);
            if (!checker.isWalkable(x, previous.flooredY(), z)) {
                return true;
            }
        }
        return false;
    }

    private static boolean hasWalkableSupport(WalkabilityChecker checker, PathPosition position) {
        return checker.isWalkable(position.flooredX(), position.flooredY(), position.flooredZ());
    }

    private static double floorLevel(WalkabilityChecker checker, PathPosition position) {
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
        return topY <= 0.0 ? y - 1 : y - 1 + topY;
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

    private static void relabelMoveTypes(List<Node> nodes, WalkabilityChecker checker) {
        if (checker == null || nodes == null || nodes.size() < 2) {
            return;
        }
        for (int i = 1; i < nodes.size(); i++) {
            Node node = nodes.get(i);
            node.moveType = inferMoveType(nodes.get(i - 1).position, node.position, checker);
            if (isSwimPosition(checker, node.position)) {
                node.moveType = Node.MoveType.SWIM;
            }
        }
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
            if (to.moveType == Node.MoveType.PARKOUR) {
                continue;
            }
            if (from.position.flooredY() != to.position.flooredY()) {
                continue;
            }

            double dx = to.position.centeredX() - from.position.centeredX();
            double dz = to.position.centeredZ() - from.position.centeredZ();
            double dist = Math.sqrt(dx * dx + dz * dz);
            double intermediateSpacing = PathfinderSettings.instance().intermediateSpacing.value();
            if (dist <= intermediateSpacing) {
                continue;
            }

            int steps = (int) (dist / intermediateSpacing);
            int y = from.position.flooredY();
            for (int step = 1; step < steps; step++) {
                double t = (double) step / steps;
                int ix = (int) Math.floor(from.position.centeredX() + dx * t);
                int iz = (int) Math.floor(from.position.centeredZ() + dz * t);
                if (checker != null && !checker.isWalkable(ix, y, iz)) {
                    continue;
                }
                Node intermediate = new Node(new PathPosition(ix, y, iz));
                intermediate.moveType = from.moveType;
                if (checker != null && isSwimPosition(checker, intermediate.position)) {
                    intermediate.moveType = Node.MoveType.SWIM;
                }
                appendDistinct(result, intermediate);
            }
        }
        appendDistinct(result, keynodes.getLast());
        return result;
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

    private static boolean sameXZ(Node a, Node b) {
        return a.position.flooredX() == b.position.flooredX()
            && a.position.flooredZ() == b.position.flooredZ();
    }

    private static boolean sameBlock(Node a, Node b) {
        return a.position.flooredX() == b.position.flooredX()
            && a.position.flooredY() == b.position.flooredY()
            && a.position.flooredZ() == b.position.flooredZ();
    }

    private static boolean isAscendingMove(Node.MoveType a, Node.MoveType b) {
        return a == Node.MoveType.STEP_UP || b == Node.MoveType.STEP_UP
            || a == Node.MoveType.JUMP || b == Node.MoveType.JUMP
            || a == Node.MoveType.PARKOUR || b == Node.MoveType.PARKOUR;
    }

    // True if the node is in water, or if it is a non-solid block sitting one above a water column
    // (player treading water at the surface, feet in air but standing block is water).
    private static boolean isSwimPosition(WalkabilityChecker checker, PathPosition pos) {
        int x = pos.flooredX(), y = pos.flooredY(), z = pos.flooredZ();
        if (checker.isWater(x, y, z)) return true;
        return checker.isPassable(x, y, z) && checker.isWater(x, y - 1, z);
    }
}
