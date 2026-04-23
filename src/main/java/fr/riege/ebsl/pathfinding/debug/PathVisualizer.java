package fr.riege.ebsl.pathfinding.debug;

import fr.riege.ebsl.event.events.render.RenderWorldEvent;
import fr.riege.ebsl.pathfinding.Node;
import fr.riege.ebsl.pathfinding.util.BlockPosUtil;
import fr.riege.ebsl.render.MinecraftRenderHandle;
import fr.riege.ebsl.render.TemplateMeshRenderer;
import fr.riege.ebsl.render.RenderHandle;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;

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
    private static final float LINE_WIDTH = 1.5f;
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

    private static final TemplateMeshRenderer MESH_RENDERER = new TemplateMeshRenderer();

    private static boolean enabled = false;
    private static List<Node> currentPath = Collections.emptyList();
    private static int currentWaypointIndex = 0;
    private static List<Vec3> cameraPath = Collections.emptyList();
    private static int currentCameraRailIndex = -1;
    private static final Set<Long> exploredNodes = Collections.synchronizedSet(new LinkedHashSet<>());

    private PathVisualizer() {
    }

    public static void register() {
    }

    public static void captureCamera(Minecraft mc) {
    }

    public static void endFrame() {
        MESH_RENDERER.endFrame();
    }

    public static void setPath(List<Node> path, int wpIndex) {
        currentPath = path != null ? path : Collections.emptyList();
        currentWaypointIndex = wpIndex;
    }

    public static void setCameraPath(List<Vec3> path) {
        cameraPath = path != null ? path : Collections.emptyList();
        if (currentCameraRailIndex >= cameraPath.size()) {
            currentCameraRailIndex = -1;
        }
    }

    public static void addExplored(int x, int y, int z) {
        if (exploredNodes.size() < MAX_EXPLORED) {
            exploredNodes.add(BlockPosUtil.pack(x, y, z));
        }
    }

    public static void toggle() {
        enabled = !enabled;
        if (!enabled) {
            clear();
        }
    }

    public static void updateExecution(int wpIndex, int camTargetIdx) {
        currentWaypointIndex = wpIndex;
        if (camTargetIdx >= 0) {
            currentCameraRailIndex = camTargetIdx;
        }
    }

    public static void updateCameraExecution(int camRailIdx) {
        currentCameraRailIndex = camRailIdx;
    }

    public static void clear() {
        currentPath = Collections.emptyList();
        cameraPath = Collections.emptyList();
        currentWaypointIndex = 0;
        currentCameraRailIndex = -1;
        exploredNodes.clear();
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static void renderWorld(RenderWorldEvent event) {
        if (!enabled) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) {
            return;
        }

        RenderHandle handle = new MinecraftRenderHandle(
            event.getProjection(),
            MESH_RENDERER,
            event.getCamX(),
            event.getCamY(),
            event.getCamZ()
        );

        List<Node> path = currentPath;
        int limit = Math.min(path.size(), MAX_RENDER);
        if (limit == 0) {
            renderCameraRail(handle);
            return;
        }
        renderNodeBoxes(path, limit, handle, event.getCamX(), event.getCamY(), event.getCamZ());
        renderStartEnd(path, limit, handle, event.getCamX(), event.getCamY(), event.getCamZ());
        renderPathLine(path, limit, handle, event.getCamX(), event.getCamY(), event.getCamZ());

        renderCameraRail(handle);
    }

    public static void renderWorld() {
    }

    private static void renderNodeBoxes(List<Node> path, int limit, RenderHandle handle,
                                        double camX, double camY, double camZ) {
        for (int i = 0; i < limit; i++) {
            Node node = path.get(i);
            drawBox(handle,
                node.position.centeredX() - camX,
                node.position.flooredY() + RENDER_Y_OFFSET - camY,
                node.position.centeredZ() - camZ,
                NODE_BOX_SIZE,
                COLOR_NODE,
                false);
        }
    }

    private static void renderStartEnd(List<Node> path, int limit, RenderHandle handle,
                                       double camX, double camY, double camZ) {
        Node start = path.getFirst();
        Node end = path.get(limit - 1);

        drawBox(handle,
            start.position.centeredX() - camX,
            start.position.flooredY() + RENDER_Y_OFFSET - camY,
            start.position.centeredZ() - camZ,
            NODE_BOX_SIZE,
            COLOR_START,
            false);

        drawBox(handle,
            end.position.centeredX() - camX,
            end.position.flooredY() + RENDER_Y_OFFSET - camY,
            end.position.centeredZ() - camZ,
            NODE_BOX_SIZE,
            COLOR_END,
            false);
    }

    private static void renderPathLine(List<Node> path, int limit, RenderHandle handle,
                                       double camX, double camY, double camZ) {
        handle.beginLines(COLOR_PATH[0], COLOR_PATH[1], COLOR_PATH[2], COLOR_PATH[3]);
        for (int i = 0; i + 1 < limit; i++) {
            Node a = path.get(i);
            Node b = path.get(i + 1);
            handle.emitLine(
                a.position.centeredX() - camX,
                a.position.flooredY() + RENDER_Y_OFFSET - camY,
                a.position.centeredZ() - camZ,
                b.position.centeredX() - camX,
                b.position.flooredY() + RENDER_Y_OFFSET - camY,
                b.position.centeredZ() - camZ,
                PATH_LINE_WIDTH
            );
        }
        handle.end(false);
    }

    private static void renderCameraRail(RenderHandle handle) {
        if (cameraPath.isEmpty()) {
            return;
        }

        int limit = Math.min(cameraPath.size(), MAX_CAMERA_RENDER);
        for (int i = 0; i < limit - 1; i++) {
            Vec3 a = cameraPath.get(i);
            Vec3 b = cameraPath.get(i + 1);
            float[] color = i < currentCameraRailIndex ? COLOR_CAMERA_LINE_DONE : COLOR_CAMERA_LINE;
            handle.beginLines(color[0], color[1], color[2], color[3]);
            handle.emitLine(
                a.x - handle.cameraX(), a.y - handle.cameraY(), a.z - handle.cameraZ(),
                b.x - handle.cameraX(), b.y - handle.cameraY(), b.z - handle.cameraZ(),
                CAMERA_LINE_WIDTH
            );
            handle.end(true);
        }

        for (int i = 0; i < limit; i++) {
            Vec3 point = cameraPath.get(i);
            boolean active = i == currentCameraRailIndex;
            drawBox(handle,
                point.x - handle.cameraX(),
                point.y - handle.cameraY(),
                point.z - handle.cameraZ(),
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

}
