package fr.riege.ebsl.common.pathfinding.debug;

import fr.riege.ebsl.common.math.Vec3d;
import fr.riege.ebsl.common.pathfinding.Node;
import fr.riege.ebsl.common.pathfinding.util.BlockPosUtil;
import fr.riege.ebsl.common.render.RenderHandle;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public final class PathVisualizer {
    private static final int MAX_EXPLORED = 3000;
    private static final int MAX_RENDER = 300;
    private static final int MAX_CAMERA_RENDER = 480;
    private static final double RENDER_Y_OFFSET = 0.9;

    private static final float NODE_BOX_SIZE = 0.25f;
    private static final float PATH_LINE_WIDTH = 2.0f;
    private static final float CAMERA_NODE_SIZE = 0.10f;
    private static final float CAMERA_LINE_WIDTH = 1.25f;

    private static final float[] COLOR_START = {0.0f, 1.0f, 1.0f, 0.9f};
    private static final float[] COLOR_END = {1.0f, 0.0f, 1.0f, 0.9f};
    private static final float[] COLOR_NODE = {0.5f, 0.5f, 1.0f, 0.7f};
    private static final float[] COLOR_PATH = {1.0f, 1.0f, 1.0f, 0.6f};
    private static final float[] COLOR_CAMERA_LINE = {0.47f, 0.86f, 1.0f, 0.78f};
    private static final float[] COLOR_CAMERA_LINE_DONE = {0.47f, 0.55f, 0.63f, 0.47f};
    private static final float[] COLOR_CAMERA_NODE = {0.57f, 0.57f, 0.57f, 0.55f};
    private static final float[] COLOR_CAMERA_NODE_ACTIVE = {1.0f, 0.47f, 1.0f, 0.95f};

    private static volatile List<Node> currentPath = Collections.emptyList();
    private static volatile List<Vec3d> cameraPath = Collections.emptyList();
    private static volatile int currentCameraRailIndex = -1;
    private static final Set<Long> exploredNodes = Collections.synchronizedSet(new LinkedHashSet<>());

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
        return true;
    }

    public static void setEnabled(boolean value) {
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
        List<Node> path = currentPath;
        List<Vec3d> railPath = cameraPath;
        int railIndex = currentCameraRailIndex;
        int limit = Math.min(path.size(), MAX_RENDER);
        if (limit == 0) {
            renderCameraRail(handle, railPath, railIndex);
            return;
        }
        renderNodeBoxes(path, limit, handle);
        renderStartEnd(path, limit, handle);
        renderPathLine(path, limit, handle);
        renderCameraRail(handle, railPath, railIndex);
    }

    private static void renderNodeBoxes(List<Node> path, int limit, RenderHandle handle) {
        for (int i = 0; i < limit; i++) {
            Node node = path.get(i);
            drawBox(handle,
                node.position.centeredX() - handle.cameraX(),
                node.position.flooredY() + RENDER_Y_OFFSET - handle.cameraY(),
                node.position.centeredZ() - handle.cameraZ(),
                NODE_BOX_SIZE,
                COLOR_NODE,
                false);
        }
    }

    private static void renderStartEnd(List<Node> path, int limit, RenderHandle handle) {
        Node start = path.getFirst();
        Node end = path.get(limit - 1);
        drawBox(handle,
            start.position.centeredX() - handle.cameraX(),
            start.position.flooredY() + RENDER_Y_OFFSET - handle.cameraY(),
            start.position.centeredZ() - handle.cameraZ(),
            NODE_BOX_SIZE,
            COLOR_START,
            false);
        drawBox(handle,
            end.position.centeredX() - handle.cameraX(),
            end.position.flooredY() + RENDER_Y_OFFSET - handle.cameraY(),
            end.position.centeredZ() - handle.cameraZ(),
            NODE_BOX_SIZE,
            COLOR_END,
            false);
    }

    private static void renderPathLine(List<Node> path, int limit, RenderHandle handle) {
        handle.beginLines(COLOR_PATH[0], COLOR_PATH[1], COLOR_PATH[2], COLOR_PATH[3]);
        for (int i = 0; i + 1 < limit; i++) {
            Node a = path.get(i);
            Node b = path.get(i + 1);
            handle.emitLine(
                a.position.centeredX() - handle.cameraX(),
                a.position.flooredY() + RENDER_Y_OFFSET - handle.cameraY(),
                a.position.centeredZ() - handle.cameraZ(),
                b.position.centeredX() - handle.cameraX(),
                b.position.flooredY() + RENDER_Y_OFFSET - handle.cameraY(),
                b.position.centeredZ() - handle.cameraZ(),
                PATH_LINE_WIDTH);
        }
        handle.end(false);
    }

    private static void renderCameraRail(RenderHandle handle, List<Vec3d> railPath, int railIndex) {
        if (railPath.isEmpty()) {
            return;
        }

        int limit = Math.min(railPath.size(), MAX_CAMERA_RENDER);
        for (int i = 0; i < limit - 1; i++) {
            Vec3d a = railPath.get(i);
            Vec3d b = railPath.get(i + 1);
            float[] color = i < railIndex ? COLOR_CAMERA_LINE_DONE : COLOR_CAMERA_LINE;
            handle.beginLines(color[0], color[1], color[2], color[3]);
            handle.emitLine(
                a.x() - handle.cameraX(), a.y() - handle.cameraY(), a.z() - handle.cameraZ(),
                b.x() - handle.cameraX(), b.y() - handle.cameraY(), b.z() - handle.cameraZ(),
                CAMERA_LINE_WIDTH);
            handle.end(true);
        }

        for (int i = 0; i < limit; i++) {
            Vec3d point = railPath.get(i);
            boolean active = i == railIndex;
            drawBox(handle,
                point.x() - handle.cameraX(),
                point.y() - handle.cameraY(),
                point.z() - handle.cameraZ(),
                active ? CAMERA_NODE_SIZE * 1.8f : CAMERA_NODE_SIZE,
                active ? COLOR_CAMERA_NODE_ACTIVE : COLOR_CAMERA_NODE,
                true);
        }
    }

    private static void drawBox(RenderHandle handle, double cx, double cy, double cz, float size, float[] color, boolean ignoreDepth) {
        float half = size / 2.0f;
        double x1 = cx - half;
        double y1 = cy - half;
        double z1 = cz - half;
        double x2 = cx + half;
        double y2 = cy + half;
        double z2 = cz + half;

        handle.beginLines(color[0], color[1], color[2], color[3]);
        handle.emitLine(x1, y1, z1, x2, y1, z1, 1.5f);
        handle.emitLine(x2, y1, z1, x2, y1, z2, 1.5f);
        handle.emitLine(x2, y1, z2, x1, y1, z2, 1.5f);
        handle.emitLine(x1, y1, z2, x1, y1, z1, 1.5f);
        handle.emitLine(x1, y2, z1, x2, y2, z1, 1.5f);
        handle.emitLine(x2, y2, z1, x2, y2, z2, 1.5f);
        handle.emitLine(x2, y2, z2, x1, y2, z2, 1.5f);
        handle.emitLine(x1, y2, z2, x1, y2, z1, 1.5f);
        handle.emitLine(x1, y1, z1, x1, y2, z1, 1.5f);
        handle.emitLine(x2, y1, z1, x2, y2, z1, 1.5f);
        handle.emitLine(x2, y1, z2, x2, y2, z2, 1.5f);
        handle.emitLine(x1, y1, z2, x1, y2, z2, 1.5f);
        handle.end(ignoreDepth);
    }

    private static int clampIndex(int index, int size) {
        if (size <= 0) {
            return -1;
        }
        return Math.max(0, Math.min(index, size - 1));
    }
}
