package fr.riege.ebsl.common.pathfinding.debug;

import fr.riege.ebsl.common.math.Vec3d;
import fr.riege.ebsl.common.pathfinding.Node;
import fr.riege.ebsl.common.pathfinding.settings.PathfinderSettings;
import fr.riege.ebsl.common.pathfinding.util.BlockPosUtil;
import fr.riege.ebsl.common.platform.render.RenderHandle;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public final class PathVisualizer {
    private static final int MAX_EXPLORED = 3000;

    private static volatile List<Node> currentPath = Collections.emptyList();
    private static volatile List<Vec3d> cameraPath = Collections.emptyList();
    private static volatile int currentCameraRailIndex = -1;
    private static double visualCameraRailProgress = -1.0;
    private static Vec3d visualCameraRailPosition;
    private static long lastVisualUpdateNanos;
    private static final Set<Long> exploredNodes = Collections.synchronizedSet(new LinkedHashSet<>());

    private static boolean enabled = true;

    private PathVisualizer() {
    }

    public static void setPath(List<Node> path) {
        currentPath = path != null ? List.copyOf(path) : Collections.emptyList();
    }

    public static void setCameraPath(List<Vec3d> path) {
        List<Vec3d> snapshot = path != null ? List.copyOf(path) : Collections.emptyList();
        cameraPath = snapshot;
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

    public static void updateExecution(int camTargetIdx) {
        if (camTargetIdx >= 0) {
            currentCameraRailIndex = clampIndex(camTargetIdx, cameraPath.size());
        }
    }

    public static void clear() {
        currentPath = Collections.emptyList();
        cameraPath = Collections.emptyList();
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
            currentPath,
            cameraPath,
            currentCameraRailIndex,
            visualCameraRailProgress,
            visualCameraRailPosition);
    }

    private static void updateVisualCameraRail() {
        List<Vec3d> path = cameraPath;
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
