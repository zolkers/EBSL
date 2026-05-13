package fr.riege.ebsl.common.pathfinding;

import fr.riege.ebsl.common.pathfinding.movement.MovementClassifier;
import fr.riege.ebsl.common.pathfinding.movement.PathSmoother;
import fr.riege.ebsl.common.pathfinding.movement.WalkabilityChecker;
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
            node.setKeynode(true);
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
                node.setMoveType(MovementClassifier.classify(previous, position, effectiveConfig.provider, null, checker));
            }
            nodes.add(node);
            previous = position;
        }
        return nodes;
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
            node.setMoveType(MovementClassifier.classify(nodes.get(i - 1).position, node.position, null, null, checker));
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
            if (canInsertIntermediates(from, to)) {
                insertIntermediateNodes(result, from, to, checker);
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
                && isAscendingMove(last.moveType(), current.moveType())) {
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

    private static boolean canInsertIntermediates(Node from, Node to) {
        return to.moveType() != Node.MoveType.PARKOUR
            && from.position.flooredY() == to.position.flooredY()
            && horizontalDistance(from, to) > PathfinderSettings.instance().intermediateSpacing.value();
    }

    private static void insertIntermediateNodes(List<Node> result, Node from, Node to, WalkabilityChecker checker) {
        double dx = to.position.centeredX() - from.position.centeredX();
        double dz = to.position.centeredZ() - from.position.centeredZ();
        double spacing = PathfinderSettings.instance().intermediateSpacing.value();
        int steps = (int) (Math.sqrt(dx * dx + dz * dz) / spacing);
        int y = from.position.flooredY();
        for (int step = 1; step < steps; step++) {
            double t = (double) step / steps;
            int ix = (int) Math.floor(from.position.centeredX() + dx * t);
            int iz = (int) Math.floor(from.position.centeredZ() + dz * t);
            if (checker == null || checker.isWalkable(ix, y, iz)) {
                Node intermediate = new Node(new PathPosition(ix, y, iz));
                intermediate.setMoveType(from.moveType());
                appendDistinct(result, intermediate);
            }
        }
    }

    private static double horizontalDistance(Node from, Node to) {
        double dx = to.position.centeredX() - from.position.centeredX();
        double dz = to.position.centeredZ() - from.position.centeredZ();
        return Math.sqrt(dx * dx + dz * dz);
    }
}
