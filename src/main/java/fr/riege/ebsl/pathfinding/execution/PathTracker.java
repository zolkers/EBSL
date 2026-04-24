package fr.riege.ebsl.pathfinding.execution;

import fr.riege.ebsl.pathfinding.Node;
import fr.riege.ebsl.pathfinding.check.PathProximitySnapshot;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

final class PathTracker {
    private static final double OFF_PATH_VERTICAL_CURSOR_OFFSET = 0.15;

    private List<Node> path = Collections.emptyList();
    private int pursuitSegment;
    private Vec3 lastPos = Vec3.ZERO;
    private long lastProgressTime;
    private double bestPathProgress = Double.NEGATIVE_INFINITY;
    private long lastPathProgressTime;
    private long severeOffPathSince;

    void start(List<Node> path) {
        this.path = snapshot(path);
        this.pursuitSegment = 0;
        this.lastPos = Vec3.ZERO;
        this.lastProgressTime = System.currentTimeMillis();
        this.bestPathProgress = Double.NEGATIVE_INFINITY;
        this.lastPathProgressTime = this.lastProgressTime;
        this.severeOffPathSince = 0;
    }

    void continueWith(List<Node> continuationPath) {
        if (continuationPath == null || continuationPath.isEmpty()) {
            return;
        }

        List<Node> merged = new ArrayList<>();
        if (!path.isEmpty()) {
            int startIdx = Math.max(0, Math.min(pursuitSegment, path.size() - 1));
            for (int i = startIdx; i < path.size(); i++) {
                appendDistinct(merged, path.get(i));
            }
        }
        for (Node node : continuationPath) {
            appendDistinct(merged, node);
        }
        if (merged.isEmpty()) {
            return;
        }

        resetPath(merged);
    }

    void trimAndContinueWith(double trimRatio, List<Node> newPath) {
        if (newPath == null || newPath.isEmpty()) return;
        int trimIndex = (int)(path.size() * trimRatio);
        trimIndex = Math.max(pursuitSegment + 1, Math.min(path.size(), trimIndex));

        List<Node> merged = new ArrayList<>();
        for (int i = pursuitSegment; i < trimIndex; i++) {
            appendDistinct(merged, path.get(i));
        }
        for (Node node : newPath) {
            appendDistinct(merged, node);
        }
        if (merged.isEmpty()) return;

        resetPath(merged);
    }

    boolean applySmartCutoff(int segmentIndex) {
        if (path.size() < 2) {
            return false;
        }
        int targetSegment = Math.max(0, Math.min(segmentIndex, path.size() - 2));
        if (targetSegment <= 0) {
            return false;
        }

        List<Node> trimmed = new ArrayList<>(path.subList(targetSegment, path.size()));
        if (trimmed.size() < 2) {
            return false;
        }

        resetPath(trimmed);
        return true;
    }

    private void resetPath(List<Node> newPath) {
        this.path = snapshot(newPath);
        this.pursuitSegment = 0;
        this.bestPathProgress = Double.NEGATIVE_INFINITY;
        this.lastPathProgressTime = System.currentTimeMillis();
        this.severeOffPathSince = 0;
    }

    Optional<PathRepairRequest> createRepairRequest(int segmentIndex, String reason) {
        if (path.size() < 2) {
            return Optional.empty();
        }
        int targetSegment = Math.max(0, Math.min(segmentIndex, path.size() - 2));
        Node joinNode = path.get(targetSegment);
        List<Node> remaining = new ArrayList<>(path.subList(targetSegment, path.size()));
        if (remaining.size() < 2) {
            return Optional.empty();
        }
        return Optional.of(new PathRepairRequest(joinNode, remaining, reason));
    }

    double noteMovementProgress(Vec3 playerPos, double stuckDistanceThreshold) {
        double distMoved = playerPos.distanceTo(lastPos);
        if (distMoved >= stuckDistanceThreshold) {
            lastProgressTime = System.currentTimeMillis();
        }
        lastPos = playerPos;
        return distMoved;
    }

    boolean advancePursuit(Vec3 playerPos, long now) {
        boolean changed = false;
        while (pursuitSegment + 1 < path.size()) {
            Node from = path.get(pursuitSegment);
            Node to = path.get(pursuitSegment + 1);
            double ax = from.position.centeredX();
            double ay = from.position.flooredY();
            double az = from.position.centeredZ();
            double bx = to.position.centeredX();
            double by = to.position.flooredY();
            double bz = to.position.centeredZ();
            double dx = bx - ax;
            double dy = by - ay;
            double dz = bz - az;
            double lenSq = dx * dx + dy * dy + dz * dz;
            if (lenSq < 1e-6) {
                pursuitSegment++;
                lastProgressTime = now;
                changed = true;
                continue;
            }
            double t = ((playerPos.x - ax) * dx + (playerPos.y - ay) * dy + (playerPos.z - az) * dz) / lenSq;
            if (t < 1.0) {
                break;
            }
            pursuitSegment++;
            lastProgressTime = now;
            changed = true;
        }
        return changed;
    }

    double computeAndTrackPathProgress(Vec3 playerPos, double epsilon, long now) {
        double pathProgress = computePathProgress(playerPos);
        if (pathProgress > bestPathProgress + epsilon) {
            bestPathProgress = pathProgress;
            lastPathProgressTime = now;
        }
        return pathProgress;
    }

    PathProximitySnapshot analyzePathProximity(Vec3 pos) {
        if (path.isEmpty()) {
            return new PathProximitySnapshot(0, 0, 0.0, 0.0, 0.0, 0.0, 0.0);
        }
        if (path.size() == 1) {
            Node node = path.getFirst();
            double dx = node.position.centeredX() - pos.x;
            double dy = (node.position.flooredY() - OFF_PATH_VERTICAL_CURSOR_OFFSET) - pos.y;
            double dz = node.position.centeredZ() - pos.z;
            double hDist = Math.sqrt(dx * dx + dz * dz);
            return new PathProximitySnapshot(0, 0, 0.0, hDist, Math.abs(dy), Math.sqrt(hDist * hDist + dy * dy), 0.0);
        }

        int start = Math.max(0, pursuitSegment - 3);
        int end = Math.min(path.size() - 2, pursuitSegment + 24);
        double bestDist3d = Double.MAX_VALUE;
        double bestHorizontal = Double.MAX_VALUE;
        double bestVertical = Double.MAX_VALUE;
        double bestProgress = pursuitSegment;
        double bestT = 0.0;
        int bestSegment = pursuitSegment;
        int bestNode = Math.min(path.size() - 1, pursuitSegment + 1);

        for (int i = start; i <= end; i++) {
            Node from = path.get(i);
            Node to = path.get(i + 1);
            double ax = from.position.centeredX();
            double ay = from.position.flooredY() - OFF_PATH_VERTICAL_CURSOR_OFFSET;
            double az = from.position.centeredZ();
            double bx = to.position.centeredX();
            double by = to.position.flooredY() - OFF_PATH_VERTICAL_CURSOR_OFFSET;
            double bz = to.position.centeredZ();

            double dx = bx - ax;
            double dy = by - ay;
            double dz = bz - az;
            double lenSq = dx * dx + dy * dy + dz * dz;
            double t = lenSq < 1.0e-6 ? 0.0
                : Math.max(0.0, Math.min(1.0, ((pos.x - ax) * dx + (pos.y - ay) * dy + (pos.z - az) * dz) / lenSq));

            double projX = ax + dx * t;
            double projY = ay + (by - ay) * t;
            double projZ = az + dz * t;
            double offX = pos.x - projX;
            double offY = pos.y - projY;
            double offZ = pos.z - projZ;
            double horizontal = Math.sqrt(offX * offX + offZ * offZ);
            double vertical = Math.abs(offY);
            double dist3d = Math.sqrt(horizontal * horizontal + vertical * vertical);
            double progress = i + t;

            boolean better = dist3d < bestDist3d - 1.0e-4
                || (Math.abs(dist3d - bestDist3d) <= 1.0e-4 && progress > bestProgress);
            if (!better) {
                continue;
            }

            bestDist3d = dist3d;
            bestHorizontal = horizontal;
            bestVertical = vertical;
            bestProgress = progress;
            bestT = t;
            bestSegment = i;
            bestNode = t >= 0.5 ? i + 1 : i;
        }

        return new PathProximitySnapshot(
            bestSegment,
            bestNode,
            bestT,
            bestHorizontal,
            bestVertical,
            bestDist3d,
            bestProgress
        );
    }

    void updateSevereOffPathState(PathProximitySnapshot proximity, long now) {
        boolean severeDeviation = proximity.distance3d() >= 3.0 || proximity.verticalDistance() >= 3.0;
        if (!severeDeviation) {
            severeOffPathSince = 0;
            return;
        }
        if (severeOffPathSince == 0) {
            severeOffPathSince = now;
        }
    }

    long getSevereOffPathDuration(long now) {
        return severeOffPathSince == 0 ? 0 : now - severeOffPathSince;
    }

    void markReplanTriggered(long now) {
        this.lastPathProgressTime = now;
        this.severeOffPathSince = 0;
    }

    double getProgressRatio(Vec3 playerPos) {
        if (path.size() <= 1) {
            return 1.0;
        }
        return Math.max(0.0, Math.min(1.0, computePathProgress(playerPos) / (path.size() - 1)));
    }

    double getRemainingDistance(Vec3 playerPos) {
        if (path.isEmpty()) {
            return 0.0;
        }
        int nextIndex = Math.min(path.size() - 1, pursuitSegment + 1);
        double distance = distanceToNode(playerPos, path.get(nextIndex));
        for (int i = nextIndex; i + 1 < path.size(); i++) {
            distance += distanceBetween(path.get(i), path.get(i + 1));
        }
        return distance;
    }

    Node getMovementWaypoint() {
        int targetIdx = Math.min(path.size() - 1, pursuitSegment + 1);
        return path.get(targetIdx);
    }

    Node getNodeAtRatio(double ratio) {
        if (path.isEmpty()) return null;
        int index = Math.max(0, Math.min(path.size() - 1, (int)(path.size() * ratio)));
        return path.get(index);
    }

    List<Node> getPath() {
        return path;
    }

    List<Node> getPathSnapshot() {
        return path.isEmpty() ? Collections.emptyList() : List.copyOf(path);
    }

    int getPursuitSegment() {
        return pursuitSegment;
    }

    long getLastProgressTime() {
        return lastProgressTime;
    }

    long getLastPathProgressTime() {
        return lastPathProgressTime;
    }

    private double computePathProgress(Vec3 playerPos) {
        if (path.isEmpty()) {
            return 0.0;
        }
        if (pursuitSegment + 1 >= path.size()) {
            return path.size() - 1;
        }

        Node from = path.get(pursuitSegment);
        Node to = path.get(pursuitSegment + 1);
        double dx = to.position.centeredX() - from.position.centeredX();
        double dy = to.position.flooredY() - from.position.flooredY();
        double dz = to.position.centeredZ() - from.position.centeredZ();
        double lenSq = dx * dx + dy * dy + dz * dz;
        if (lenSq < 1.0e-6) {
            return pursuitSegment;
        }

        double px = playerPos.x - from.position.centeredX();
        double py = playerPos.y - from.position.flooredY();
        double pz = playerPos.z - from.position.centeredZ();
        double t = (px * dx + py * dy + pz * dz) / lenSq;
        return pursuitSegment + Math.max(0.0, Math.min(0.999, t));
    }

    private static double distanceToNode(Vec3 position, Node node) {
        double dx = node.position.centeredX() - position.x;
        double dy = node.position.flooredY() - position.y;
        double dz = node.position.centeredZ() - position.z;
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    private static double horizontalDistanceToNode(Vec3 position, Node node) {
        double dx = node.position.centeredX() - position.x;
        double dz = node.position.centeredZ() - position.z;
        return Math.sqrt(dx * dx + dz * dz);
    }

    private static double distanceBetween(Node from, Node to) {
        double dx = to.position.centeredX() - from.position.centeredX();
        double dy = to.position.flooredY() - from.position.flooredY();
        double dz = to.position.centeredZ() - from.position.centeredZ();
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    private static List<Node> snapshot(List<Node> nodes) {
        return nodes == null || nodes.isEmpty() ? Collections.emptyList() : List.copyOf(nodes);
    }

    private static void appendDistinct(List<Node> nodes, Node candidate) {
        if (candidate == null) {
            return;
        }
        if (nodes.isEmpty()) {
            nodes.add(candidate);
            return;
        }
        Node last = nodes.getLast();
        if (last.position.equals(candidate.position)) {
            return;
        }
        nodes.add(candidate);
    }
}
