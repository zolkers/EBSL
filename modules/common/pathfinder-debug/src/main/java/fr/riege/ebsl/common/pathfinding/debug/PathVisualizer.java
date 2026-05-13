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

package fr.riege.ebsl.common.pathfinding.debug;

import fr.riege.ebsl.common.math.Vec3d;
import fr.riege.ebsl.common.pathfinding.Node;
import fr.riege.ebsl.common.pathfinding.diagnostics.PathExecutionDiagnostics;
import fr.riege.ebsl.common.pathfinding.diagnostics.PathfindingDiagnostics;
import fr.riege.ebsl.common.pathfinding.settings.PathfinderSettings;
import fr.riege.ebsl.common.pathfinding.util.BlockPosUtil;
import fr.riege.ebsl.common.platform.render.RenderHandle;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

public final class PathVisualizer {
    private static final int MAX_EXPLORED = 3000;
    private static final long MASK_Y = 0xFFFL;
    private static final long MASK_XZ = 0x3FFFFFFL;
    private static final int SHIFT_Z = 12;
    private static final int SHIFT_X = 38;

    private static final AtomicReference<List<Node>> currentPath = new AtomicReference<>(Collections.emptyList());
    private static final AtomicReference<List<Vec3d>> cameraPath = new AtomicReference<>(Collections.emptyList());
    private static volatile int currentCameraRailIndex = -1;
    private static double visualCameraRailProgress = -1.0;
    private static Vec3d visualCameraRailPosition;
    private static long lastVisualUpdateNanos;
    private static final Set<Long> exploredNodes = Collections.synchronizedSet(new LinkedHashSet<>());

    private static boolean enabled = true;

    static {
        PathfindingDiagnostics.setExploredNodeSink(PathVisualizer::isEnabled, PathVisualizer::addPackedExplored);
        PathExecutionDiagnostics.setSink(new PathExecutionDiagnostics.Sink() {
            @Override public void clear() {
                PathVisualizer.clear();
            }

            @Override public void setPath(List<Node> path) {
                PathVisualizer.setPath(path);
            }

            @Override public void setCameraPath(List<Vec3d> path) {
                PathVisualizer.setCameraPath(path);
            }

            @Override public void updateExecution(int cameraTargetIndex) {
                PathVisualizer.updateExecution(cameraTargetIndex);
            }
        });
    }

    private PathVisualizer() {
    }

    public static void setPath(List<Node> path) {
        currentPath.set(path != null ? List.copyOf(path) : Collections.emptyList());
    }

    public static void setCameraPath(List<Vec3d> path) {
        List<Vec3d> snapshot = path != null ? List.copyOf(path) : Collections.emptyList();
        cameraPath.set(snapshot);
        if (currentCameraRailIndex >= snapshot.size()) {
            currentCameraRailIndex = -1;
        }
        visualCameraRailProgress = currentCameraRailIndex;
        visualCameraRailPosition = currentCameraRailIndex >= 0 && currentCameraRailIndex < snapshot.size()
            ? snapshot.get(currentCameraRailIndex)
            : null;
        lastVisualUpdateNanos = 0L;
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static void setEnabled(boolean e) {
        enabled = e;
    }

    public static void addExplored(int x, int y, int z) {
        if (exploredNodes.size() < MAX_EXPLORED) {
            exploredNodes.add(BlockPosUtil.pack(x, y, z));
        }
    }

    private static void addPackedExplored(long packedPosition) {
        addExplored(unpackX(packedPosition), unpackY(packedPosition), unpackZ(packedPosition));
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

    public static void updateExecution(int camTargetIdx) {
        if (camTargetIdx >= 0) {
            currentCameraRailIndex = clampIndex(camTargetIdx, cameraPath.get().size());
        }
    }

    public static void clear() {
        currentPath.set(Collections.emptyList());
        cameraPath.set(Collections.emptyList());
        currentCameraRailIndex = -1;
        visualCameraRailProgress = -1.0;
        visualCameraRailPosition = null;
        lastVisualUpdateNanos = 0L;
        exploredNodes.clear();
    }

    public static void renderWorld(RenderHandle handle) {
        PathfinderSettings settings = PathfinderSettings.instance();
        if (!shouldRender(settings)) {
            return;
        }
        PathVisualizerRenderer.render(handle, snapshot(), PathVisualizerStyle.from(settings));
    }

    private static boolean shouldRender(PathfinderSettings settings) {
        return enabled && settings.showDebug.value();
    }

    private static PathVisualizerSnapshot snapshot() {
        updateVisualCameraRail();
        return new PathVisualizerSnapshot(
            currentPath.get(),
            cameraPath.get(),
            currentCameraRailIndex,
            visualCameraRailProgress,
            visualCameraRailPosition);
    }

    private static void updateVisualCameraRail() {
        List<Vec3d> path = cameraPath.get();
        int targetIndex = clampIndex(currentCameraRailIndex, path.size());
        if (targetIndex < 0) {
            visualCameraRailProgress = -1.0;
            visualCameraRailPosition = null;
            lastVisualUpdateNanos = 0L;
            return;
        }

        Vec3d target = path.get(targetIndex);
        long now = System.nanoTime();
        if (visualCameraRailPosition == null || visualCameraRailProgress < 0.0 || lastVisualUpdateNanos == 0L) {
            visualCameraRailProgress = targetIndex;
            visualCameraRailPosition = target;
            lastVisualUpdateNanos = now;
            return;
        }

        double dt = Math.clamp((now - lastVisualUpdateNanos) / 1_000_000_000.0, 0.0, 0.10);
        lastVisualUpdateNanos = now;
        double response = 1.0 - Math.exp(-dt * 12.0);
        visualCameraRailProgress += (targetIndex - visualCameraRailProgress) * response;
        visualCameraRailPosition = lerp(visualCameraRailPosition, target, response);
        if (Math.abs(targetIndex - visualCameraRailProgress) < 0.01
            && visualCameraRailPosition.distanceToSq(target) < 1.0e-4) {
            visualCameraRailProgress = targetIndex;
            visualCameraRailPosition = target;
        }
    }

    private static Vec3d lerp(Vec3d from, Vec3d to, double t) {
        return new Vec3d(
            from.x() + (to.x() - from.x()) * t,
            from.y() + (to.y() - from.y()) * t,
            from.z() + (to.z() - from.z()) * t);
    }

    private static int clampIndex(int index, int size) {
        if (size <= 0) {
            return -1;
        }
        return Math.clamp(index, 0, size - 1);
    }
}
