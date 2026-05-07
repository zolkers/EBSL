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
        return new PathVisualizerSnapshot(currentPath, cameraPath, currentCameraRailIndex);
    }

    private static int clampIndex(int index, int size) {
        if (size <= 0) {
            return -1;
        }
        return Math.clamp(index, 0, size - 1);
    }
}
