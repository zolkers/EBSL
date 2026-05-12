package fr.riege.ebsl.common.pathfinding;

import fr.riege.ebsl.common.pathfinding.movement.PathSmoother;
import fr.riege.ebsl.common.pathfinding.movement.WalkabilityChecker;
import fr.riege.ebsl.common.pathfinding.parkour.ParkourGeometry;
import fr.riege.ebsl.common.pathfinding.pathing.configuration.PathfinderConfiguration;
import fr.riege.ebsl.common.pathfinding.settings.PathfinderSettings;
import fr.riege.ebsl.common.pathfinding.wrapper.PathPosition;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public final class WalkPathProcessor {
    private WalkPathProcessor() {
    }

    public static ProcessedPath process(Collection<PathPosition> positions,
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

        List<PathPosition> positionList = positions instanceof List<PathPosition> list
            ? list
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
                if (checker != null && isSwimPosition(checker, position)) {
                    node.moveType = Node.MoveType.SWIM;
                }
            }
            nodes.add(node);
            previous = position;
        }
        return nodes;
    }

    private static final double STEP_DOWN_DY_THRESHOLD = -1.1;

    private static Node.MoveType inferMoveType(PathPosition previous, PathPosition current,
                                               WalkabilityChecker checker) {
        double dy = checker == null
            ? current.flooredY() - previous.flooredY()
            : floorLevel(checker, current) - floorLevel(checker, previous);
        int dx = current.flooredX() - previous.flooredX();
        int dz = current.flooredZ() - previous.flooredZ();
        int absDx = Math.abs(dx);
        int absDz = Math.abs(dz);
        if (checker != null && checker.world().requiresJumpForStep(
            current.flooredX(), current.flooredY(), current.flooredZ(),
            Integer.compare(current.flooredX(), previous.flooredX()),
            Integer.compare(current.flooredZ(), previous.flooredZ()))) {
            return Node.MoveType.JUMP;
        }
        if (checker != null && isParkourMove(previous, current, checker, absDx, absDz)) {
            return Node.MoveType.PARKOUR;
        }
        if (dy > PathfinderSettings.instance().partialAscentThreshold.value()) {
            return Node.MoveType.STEP_UP;
        }
        if (dy < PathfinderSettings.instance().descentThreshold.value()) {
            return dy >= STEP_DOWN_DY_THRESHOLD ? Node.MoveType.STEP_DOWN : Node.MoveType.FALL;
        }
        if (absDx + absDz >= 2) {
            return Node.MoveType.WALK_DIAGONAL;
        }
        return Node.MoveType.WALK;
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
        double total = 0.0;
        for (int i = 0; i < path.size() - 1; i++) {
            total += path.get(i).position.distance(path.get(i + 1).position);
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
            if (to.moveType == Node.MoveType.PARKOUR || from.position.flooredY() != to.position.flooredY()) {
                continue;
            }

            double dx = to.position.centeredX() - from.position.centeredX();
            double dz = to.position.centeredZ() - from.position.centeredZ();
            double dist = Math.sqrt(dx * dx + dz * dz);
            double spacing = PathfinderSettings.instance().intermediateSpacing.value();
            if (dist <= spacing) {
                continue;
            }

            int steps = (int) (dist / spacing);
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
        if (list.isEmpty() || !sameBlock(list.getLast(), node)) {
            list.add(node);
            return true;
        }
        return false;
    }

    private static boolean sameXZ(Node a, Node b) {
        return a.position.flooredX() == b.position.flooredX()
            && a.position.flooredZ() == b.position.flooredZ();
    }

    private static boolean sameBlock(Node a, Node b) {
        return sameXZ(a, b) && a.position.flooredY() == b.position.flooredY();
    }

    private static boolean isAscendingMove(Node.MoveType a, Node.MoveType b) {
        return a == Node.MoveType.STEP_UP || b == Node.MoveType.STEP_UP
            || a == Node.MoveType.JUMP || b == Node.MoveType.JUMP
            || a == Node.MoveType.PARKOUR || b == Node.MoveType.PARKOUR;
    }

    private static boolean isSwimPosition(WalkabilityChecker checker, PathPosition pos) {
        int x = pos.flooredX(), y = pos.flooredY(), z = pos.flooredZ();
        return checker.isWater(x, y, z)
            || (checker.isPassable(x, y, z) && checker.isWater(x, y - 1, z));
    }
}
