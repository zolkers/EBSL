/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 *
 * Copyright (C) 2026 Victor Riegert and EBSL contributors
 *
 * This file is part of EBSL.
 *
 * EBSL is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * EBSL is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public
 * License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with EBSL. If not, see <https://www.gnu.org/licenses/>.
 */

package fr.riege.ebsl.common.pathfinding.execution;

import fr.riege.ebsl.common.math.Vec3d;
import fr.riege.ebsl.common.pathfinding.Node;
import fr.riege.ebsl.common.pathfinding.check.PathProximitySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

final class PathTracker {
    static final double OFF_PATH_VERTICAL_CURSOR_OFFSET = 0.15;
    private static final double CONTINUATION_ALIGN_MAX_DISTANCE = 12.0;
    private static final int REPAIR_MIN_FORWARD_SEGMENTS = 2;

    private List<Node> path = Collections.emptyList();
    private ExecutionPathCache pathCache = ExecutionPathCache.of(path);
    private int pursuitSegment;
    private Vec3d lastPos = new Vec3d(0.0, 0.0, 0.0);
    private long lastProgressTime;
    private double bestPathProgress = Double.NEGATIVE_INFINITY;
    private long lastPathProgressTime;
    private long severeOffPathSince;

    void start(List<Node> path) {
        this.path = snapshot(path);
        this.pathCache = ExecutionPathCache.of(this.path);
        this.pursuitSegment = 0;
        this.lastPos = new Vec3d(0.0, 0.0, 0.0);
        this.lastProgressTime = System.currentTimeMillis();
        this.bestPathProgress = Double.NEGATIVE_INFINITY;
        this.lastPathProgressTime = this.lastProgressTime;
        this.severeOffPathSince = 0;
    }

    void continueWith(List<Node> continuationPath) {
        if (continuationPath == null || continuationPath.isEmpty()) return;
        List<Node> merged = new ArrayList<>();
        if (!path.isEmpty()) {
            int startIdx = Math.clamp(pursuitSegment, 0, path.size() - 1);
            for (int i = startIdx; i < path.size(); i++) appendDistinct(merged, path.get(i));
        }
        List<Node> aligned = alignContinuationStart(merged, continuationPath);
        if (aligned.isEmpty()) return;
        for (Node node : aligned) appendDistinct(merged, node);
        if (!merged.isEmpty()) resetPath(merged);
    }

    void trimAndContinueWith(double trimRatio, List<Node> newPath) {
        if (newPath == null || newPath.isEmpty()) return;
        if (path.isEmpty()) {
            resetPath(newPath);
            return;
        }
        int trimIndex = (int) (path.size() * trimRatio);
        trimIndex = Math.clamp(trimIndex, pursuitSegment + 1, path.size());

        List<Node> merged = new ArrayList<>();
        for (int i = pursuitSegment; i < trimIndex; i++) appendDistinct(merged, path.get(i));
        List<Node> aligned = alignContinuationStart(merged, newPath);
        if (aligned.isEmpty()) return;
        for (Node node : aligned) appendDistinct(merged, node);
        if (!merged.isEmpty()) resetPath(merged);
    }

    boolean applySmartCutoff(int segmentIndex) {
        if (path.size() < 2) return false;
        int targetSegment = Math.clamp(segmentIndex, 0, path.size() - 2);
        if (targetSegment <= 0) return false;
        List<Node> trimmed = new ArrayList<>(path.subList(targetSegment, path.size()));
        if (trimmed.size() < 2) return false;
        resetPath(trimmed);
        return true;
    }

    Optional<PathRepairRequest> createRepairRequest(int segmentIndex, String reason, int goalX, int goalY, int goalZ) {
        if (path.size() < 2) return Optional.empty();
        int earliestForwardSegment = Math.max(
            pursuitSegment + REPAIR_MIN_FORWARD_SEGMENTS,
            (int) Math.ceil(bestPathProgress) + 1);
        int targetSegment = Math.max(earliestForwardSegment, segmentIndex);
        targetSegment = Math.clamp(targetSegment, 0, path.size() - 2);
        if (targetSegment < earliestForwardSegment) return Optional.empty();
        Node joinNode = path.get(targetSegment);
        List<Node> remaining = new ArrayList<>(path.subList(targetSegment, path.size()));
        if (remaining.size() < 2) return Optional.empty();
        return Optional.of(new PathRepairRequest(joinNode, remaining, reason, goalX, goalY, goalZ));
    }

    double noteMovementProgress(Vec3d playerPos, double stuckDistanceThreshold) {
        double dx = playerPos.x() - lastPos.x();
        double dz = playerPos.z() - lastPos.z();
        double distMoved = Math.sqrt(dx * dx + dz * dz);
        if (distMoved >= stuckDistanceThreshold) {
            lastProgressTime = System.currentTimeMillis();
        }
        lastPos = playerPos;
        return distMoved;
    }

    boolean advancePursuit(Vec3d playerPos, long now) {
        boolean changed = false;
        boolean advancing = true;
        while (pursuitSegment + 1 < path.size() && advancing) {
            double ax = pathCache.x(pursuitSegment);
            double ay = pathCache.y(pursuitSegment);
            double az = pathCache.z(pursuitSegment);
            double dx = pathCache.segmentDx(pursuitSegment);
            double dy = pathCache.segmentDy(pursuitSegment);
            double dz = pathCache.segmentDz(pursuitSegment);
            double lenSq = pathCache.segmentLenSq(pursuitSegment);
            if (lenSq < 1.0e-6) {
                pursuitSegment++;
                lastProgressTime = now;
                changed = true;
            } else {
                double t = ((playerPos.x() - ax) * dx + (playerPos.y() - ay) * dy + (playerPos.z() - az) * dz) / lenSq;
                if (t < 1.0) {
                    advancing = false;
                } else {
                    pursuitSegment++;
                    lastProgressTime = now;
                    changed = true;
                }
            }
        }
        return changed;
    }

    double computeAndTrackPathProgress(Vec3d playerPos, double epsilon, long now) {
        double pathProgress = computePathProgress(playerPos);
        if (pathProgress > bestPathProgress + epsilon) {
            bestPathProgress = pathProgress;
            lastPathProgressTime = now;
        }
        return pathProgress;
    }

    PathProximitySnapshot analyzePathProximity(Vec3d pos) {
        if (path.isEmpty()) {
            return new PathProximitySnapshot(0, 0, 0.0, 0.0, 0.0, 0.0, 0.0);
        }
        if (path.size() == 1) {
            double dx = pathCache.x(0) - pos.x();
            double dy = pathCache.offPathY(0) - pos.y();
            double dz = pathCache.z(0) - pos.z();
            double hDist = Math.sqrt(dx * dx + dz * dz);
            return new PathProximitySnapshot(0, 0, 0.0, hDist, Math.abs(dy), Math.sqrt(hDist * hDist + dy * dy), 0.0);
        }

        int start = (int) Math.clamp(pursuitSegment - 3L, 0L, path.size() - 2L);
        int end = (int) Math.clamp(pursuitSegment + 24L, 0L, path.size() - 2L);
        double bestDist3d = Double.MAX_VALUE;
        double bestHorizontal = Double.MAX_VALUE;
        double bestVertical = Double.MAX_VALUE;
        double bestProgress = pursuitSegment;
        double bestT = 0.0;
        int bestSegment = pursuitSegment;
        int bestNode = (int) Math.clamp(pursuitSegment + 1L, 0L, path.size() - 1L);

        for (int i = start; i <= end; i++) {
            double ax = pathCache.x(i);
            double ay = pathCache.offPathY(i);
            double az = pathCache.z(i);
            double dx = pathCache.segmentDx(i);
            double dy = pathCache.segmentDy(i);
            double dz = pathCache.segmentDz(i);
            double lenSq = pathCache.segmentLenSq(i);
            double t = lenSq < 1.0e-6 ? 0.0
                : Math.clamp(((pos.x() - ax) * dx + (pos.y() - ay) * dy + (pos.z() - az) * dz) / lenSq, 0.0, 1.0);
            double projX = ax + dx * t;
            double projY = ay + dy * t;
            double projZ = az + dz * t;
            double offX = pos.x() - projX;
            double offY = pos.y() - projY;
            double offZ = pos.z() - projZ;
            double horizontalSq = offX * offX + offZ * offZ;
            double vertical = Math.abs(offY);
            double dist3dSq = horizontalSq + offY * offY;
            double progress = i + t;

            boolean better = dist3dSq < bestDist3d * bestDist3d - 1.0e-4
                || (Math.abs(dist3dSq - bestDist3d * bestDist3d) <= 1.0e-4 && progress > bestProgress);
            if (!better) continue;

            bestDist3d = Math.sqrt(dist3dSq);
            bestHorizontal = Math.sqrt(horizontalSq);
            bestVertical = vertical;
            bestProgress = progress;
            bestT = t;
            bestSegment = i;
            bestNode = t >= 0.5 ? i + 1 : i;
        }

        return new PathProximitySnapshot(bestSegment, bestNode, bestT, bestHorizontal, bestVertical, bestDist3d, bestProgress);
    }

    Vec3d computeCorrectionToPath(Vec3d pos, PathProximitySnapshot proximity) {
        if (path.size() < 2) return new Vec3d(0.0, 0.0, 0.0);
        int segment = Math.clamp(proximity.nearestSegmentIndex(), 0, path.size() - 2);
        double ax = pathCache.x(segment);
        double az = pathCache.z(segment);
        double dx = pathCache.segmentDx(segment);
        double dz = pathCache.segmentDz(segment);
        double lenSq = dx * dx + dz * dz;
        double t = lenSq < 1.0e-6 ? 0.0
            : Math.clamp(((pos.x() - ax) * dx + (pos.z() - az) * dz) / lenSq, 0.0, 1.0);
        return new Vec3d((ax + dx * t) - pos.x(), 0.0, (az + dz * t) - pos.z());
    }

    void updateSevereOffPathState(PathProximitySnapshot proximity, long now) {
        boolean severeDeviation = proximity.distance3d() >= 3.0 || proximity.verticalDistance() >= 3.0;
        if (!severeDeviation) {
            severeOffPathSince = 0;
            return;
        }
        if (severeOffPathSince == 0) severeOffPathSince = now;
    }

    long getSevereOffPathDuration(long now) {
        return severeOffPathSince == 0 ? 0 : now - severeOffPathSince;
    }

    void markReplanTriggered(long now) {
        this.lastPathProgressTime = now;
        this.severeOffPathSince = 0;
    }

    double getProgressRatio(Vec3d playerPos) {
        if (path.size() <= 1) return 1.0;
        return Math.clamp(computePathProgress(playerPos) / (path.size() - 1), 0.0, 1.0);
    }

    double getRemainingDistance(Vec3d playerPos) {
        if (path.isEmpty()) return 0.0;
        int nextIndex = (int) Math.clamp(pursuitSegment + 1L, 0L, path.size() - 1L);
        double distance = distanceToNode(playerPos, path.get(nextIndex));
        return distance + pathCache.remainingFromNode(nextIndex);
    }

    Node getMovementWaypoint() {
        if (path.isEmpty()) return null;
        int targetIdx = (int) Math.clamp(pursuitSegment + 1L, 0L, path.size() - 1L);
        return path.get(targetIdx);
    }

    Node getNodeAtRatio(double ratio) {
        if (path.isEmpty()) return null;
        int index = Math.clamp((int) (path.size() * ratio), 0, path.size() - 1);
        return path.get(index);
    }

    List<Node> getPath() { return path; }
    List<Node> getPathSnapshot() { return path.isEmpty() ? Collections.emptyList() : List.copyOf(path); }
    int getPursuitSegment() { return pursuitSegment; }
    long getLastProgressTime() { return lastProgressTime; }
    long getLastPathProgressTime() { return lastPathProgressTime; }

    private void resetPath(List<Node> newPath) {
        this.path = snapshot(newPath);
        this.pathCache = ExecutionPathCache.of(this.path);
        this.pursuitSegment = 0;
        this.bestPathProgress = Double.NEGATIVE_INFINITY;
        this.lastPathProgressTime = System.currentTimeMillis();
        this.severeOffPathSince = 0;
    }

    private double computePathProgress(Vec3d playerPos) {
        if (path.isEmpty()) return 0.0;
        if (pursuitSegment + 1L >= path.size()) return path.size() - 1.0;
        double dx = pathCache.segmentDx(pursuitSegment);
        double dy = pathCache.segmentDy(pursuitSegment);
        double dz = pathCache.segmentDz(pursuitSegment);
        double lenSq = pathCache.segmentLenSq(pursuitSegment);
        if (lenSq < 1.0e-6) return pursuitSegment;
        double px = playerPos.x() - pathCache.x(pursuitSegment);
        double py = playerPos.y() - pathCache.y(pursuitSegment);
        double pz = playerPos.z() - pathCache.z(pursuitSegment);
        double t = (px * dx + py * dy + pz * dz) / lenSq;
        return pursuitSegment + Math.clamp(t, 0.0, 0.999);
    }

    private static double distanceToNode(Vec3d position, Node node) {
        double dx = node.position.centeredX() - position.x();
        double dy = node.position.flooredY() - position.y();
        double dz = node.position.centeredZ() - position.z();
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    private static double distanceBetween(Node from, Node to) {
        double dx = to.position.centeredX() - from.position.centeredX();
        double dy = (double) to.position.flooredY() - from.position.flooredY();
        double dz = to.position.centeredZ() - from.position.centeredZ();
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    private static List<Node> alignContinuationStart(List<Node> currentPrefix, List<Node> continuationPath) {
        if (currentPrefix.isEmpty() || continuationPath == null || continuationPath.size() < 2) return continuationPath;
        Node anchor = currentPrefix.getLast();
        int bestIndex = 0;
        double bestDistance = Double.MAX_VALUE;
        for (int i = 0; i < continuationPath.size(); i++) {
            double distance = distanceBetween(anchor, continuationPath.get(i));
            if (distance < bestDistance) {
                bestDistance = distance;
                bestIndex = i;
            }
        }
        if (bestDistance > CONTINUATION_ALIGN_MAX_DISTANCE) return List.of();
        if (bestIndex <= 0) return continuationPath;
        return continuationPath.subList(bestIndex, continuationPath.size());
    }

    private static List<Node> snapshot(List<Node> nodes) {
        return nodes == null || nodes.isEmpty() ? Collections.emptyList() : List.copyOf(nodes);
    }

    private static void appendDistinct(List<Node> nodes, Node candidate) {
        if (candidate == null) return;
        if (nodes.isEmpty() || !nodes.getLast().position.equals(candidate.position)) {
            nodes.add(candidate);
        }
    }
}
