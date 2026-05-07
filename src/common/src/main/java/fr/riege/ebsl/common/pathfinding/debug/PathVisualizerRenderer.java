package fr.riege.ebsl.common.pathfinding.debug;

import fr.riege.ebsl.common.math.Vec3d;
import fr.riege.ebsl.common.pathfinding.Node;
import fr.riege.ebsl.common.platform.render.WorldRender;
import fr.riege.ebsl.common.platform.render.RenderHandle;

import java.util.List;

final class PathVisualizerRenderer {
    private static final double PATH_LINE_Y_OFFSET = 0.5;
    private static final float CAMERA_NODE_SIZE = 0.10f;

    private PathVisualizerRenderer() {
    }

    static void render(RenderHandle handle, PathVisualizerSnapshot snapshot, PathVisualizerStyle style) {
        int pathLimit = Math.min(snapshot.path().size(), style.maxPathNodes());
        if (pathLimit > 0) {
            renderPath(handle, snapshot.path(), pathLimit, style);
        }
        renderCameraRail(handle, snapshot.cameraPath(), snapshot.cameraRailIndex(), style);
    }

    private static void renderPath(RenderHandle handle, List<Node> path, int limit, PathVisualizerStyle style) {
        if (style.renderPathNodes()) {
            renderNodeBlocks(handle, path, limit, style);
            renderStartEnd(handle, path, limit, style);
        }
        if (style.renderPathLines()) {
            renderPathSegments(handle, path, limit, style);
        }
    }

    private static void renderNodeBlocks(RenderHandle handle, List<Node> path, int limit, PathVisualizerStyle style) {
        for (int i = 0; i < limit; i++) {
            Node node = path.get(i);
            WorldRender.builder(handle)
                .color(style.nodeColor())
                .depthTested()
                .filledBlock(node.position.flooredX(), node.position.flooredY(), node.position.flooredZ());
        }
    }

    private static void renderStartEnd(RenderHandle handle, List<Node> path, int limit, PathVisualizerStyle style) {
        Node start = path.getFirst();
        Node end = path.get(limit - 1);
        WorldRender.builder(handle)
            .color(style.startColor())
            .depthTested()
            .filledBlock(start.position.flooredX(), start.position.flooredY(), start.position.flooredZ());
        WorldRender.builder(handle)
            .color(style.endColor())
            .depthTested()
            .filledBlock(end.position.flooredX(), end.position.flooredY(), end.position.flooredZ());
    }

    private static void renderPathSegments(RenderHandle handle, List<Node> path, int limit, PathVisualizerStyle style) {
        for (int i = 0; i + 1 < limit; i++) {
            Node a = path.get(i);
            Node b = path.get(i + 1);
            WorldRender.builder(handle)
                .color(style.pathColor(b.moveType))
                .lineWidth(style.pathLineWidth())
                .depthTested()
                .line(
                    a.position.centeredX(), a.position.flooredY() + PATH_LINE_Y_OFFSET, a.position.centeredZ(),
                    b.position.centeredX(), b.position.flooredY() + PATH_LINE_Y_OFFSET, b.position.centeredZ());
        }
    }

    private static void renderCameraRail(RenderHandle handle, List<Vec3d> railPath, int railIndex, PathVisualizerStyle style) {
        if (railPath.isEmpty() || !style.renderCameraRail()) {
            return;
        }

        int limit = Math.min(railPath.size(), style.maxCameraNodes());
        for (int i = 0; i < limit - 1; i++) {
            Vec3d a = railPath.get(i);
            Vec3d b = railPath.get(i + 1);
            WorldRender.builder(handle)
                .color(style.cameraLineColor(i < railIndex))
                .lineWidth(style.cameraLineWidth())
                .throughWalls()
                .line(a.x(), a.y(), a.z(), b.x(), b.y(), b.z());
        }

        for (int i = 0; i < limit; i++) {
            Vec3d point = railPath.get(i);
            boolean active = i == railIndex;
            float size = active ? CAMERA_NODE_SIZE * 1.8f : CAMERA_NODE_SIZE;
            WorldRender.builder(handle)
                .color(style.cameraNodeColor(active))
                .throughWalls()
                .filledBox(
                    point.x() - size, point.y() - size, point.z() - size,
                    point.x() + size, point.y() + size, point.z() + size);
        }
    }
}
