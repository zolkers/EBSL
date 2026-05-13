package fr.riege.ebsl.common.pathfinding.debug;

import fr.riege.ebsl.common.math.Vec3d;
import fr.riege.ebsl.common.pathfinding.Node;
import fr.riege.ebsl.common.platform.render.WorldRender;
import fr.riege.ebsl.common.platform.render.RenderHandle;
import fr.riege.ebsl.common.platform.render.WorldRenderSession;

import java.util.List;

final class PathVisualizerRenderer {
    private static final double PATH_LINE_Y_OFFSET = 0.5;
    private static final float CAMERA_NODE_SIZE = 0.10f;
    private static final float CAMERA_NODE_ACTIVE_SIZE = 0.18f;

    private PathVisualizerRenderer() {
    }

    static void render(RenderHandle handle, PathVisualizerSnapshot snapshot, PathVisualizerStyle style) {
        int pathLimit = Math.min(snapshot.path().size(), style.maxPathNodes());
        if (pathLimit > 0) {
            renderPath(handle, snapshot.path(), pathLimit, style);
        }
        renderCameraRail(handle, snapshot.cameraPath(), snapshot.cameraRailIndex(),
            snapshot.cameraRailVisualProgress(), snapshot.cameraRailVisualPosition(), style);
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
        try (WorldRenderSession session = WorldRender.session(handle).depthTested()) {
            for (int i = 0; i < limit; i++) {
                Node node = path.get(i);
                session.paint(style.nodePaint(i, limit))
                    .filledBlock(node.position.flooredX(), node.position.flooredY(), node.position.flooredZ());
            }
        }
    }

    private static void renderStartEnd(RenderHandle handle, List<Node> path, int limit, PathVisualizerStyle style) {
        Node start = path.getFirst();
        Node end = path.get(limit - 1);
        try (WorldRenderSession session = WorldRender.session(handle).depthTested()) {
            session.color(style.startColor())
                .filledBlock(start.position.flooredX(), start.position.flooredY(), start.position.flooredZ());
            session.color(style.endColor())
                .filledBlock(end.position.flooredX(), end.position.flooredY(), end.position.flooredZ());
        }
    }

    private static void renderPathSegments(RenderHandle handle, List<Node> path, int limit, PathVisualizerStyle style) {
        try (WorldRenderSession session = WorldRender.session(handle)
            .lineWidth(style.pathLineWidth())
            .depthTested()) {
            for (int i = 0; i + 1 < limit; i++) {
                Node a = path.get(i);
                Node b = path.get(i + 1);
                session.paint(style.pathPaint(b.moveType(), i, limit))
                    .line(
                        a.position.centeredX(), a.position.flooredY() + PATH_LINE_Y_OFFSET, a.position.centeredZ(),
                        b.position.centeredX(), b.position.flooredY() + PATH_LINE_Y_OFFSET, b.position.centeredZ());
            }
        }
    }

    private static void renderCameraRail(RenderHandle handle, List<Vec3d> railPath, int railIndex,
                                         double visualProgress, Vec3d visualPosition, PathVisualizerStyle style) {
        if (railPath.isEmpty() || !style.renderCameraRail()) {
            return;
        }

        int limit = Math.min(railPath.size(), style.maxCameraNodes());
        try (WorldRenderSession session = WorldRender.session(handle)
            .lineWidth(style.cameraLineWidth())
            .throughWalls()) {
            for (int i = 0; i < limit - 1; i++) {
                Vec3d a = railPath.get(i);
                Vec3d b = railPath.get(i + 1);
                session.color(style.cameraLineColor(visualProgress, i))
                    .line(a.x(), a.y(), a.z(), b.x(), b.y(), b.z());
            }
        }

        try (WorldRenderSession session = WorldRender.session(handle)
            .color(style.cameraNodeColor(false))
            .lineWidth(style.cameraNodeLineWidth())
            .throughWalls()) {
            for (int i = 0; i < limit; i++) {
                if (i == railIndex) {
                    continue;
                }
                Vec3d point = railPath.get(i);
                session.wireBox(
                    point.x() - CAMERA_NODE_SIZE, point.y() - CAMERA_NODE_SIZE, point.z() - CAMERA_NODE_SIZE,
                    point.x() + CAMERA_NODE_SIZE, point.y() + CAMERA_NODE_SIZE, point.z() + CAMERA_NODE_SIZE);
            }
        }

        if (railIndex >= 0 && railIndex < limit) {
            Vec3d point = visualPosition != null ? visualPosition : railPath.get(railIndex);
            WorldRender.builder(handle)
                .color(style.cameraNodeColor(true))
                .throughWalls()
                .filledBox(
                    point.x() - CAMERA_NODE_ACTIVE_SIZE, point.y() - CAMERA_NODE_ACTIVE_SIZE, point.z() - CAMERA_NODE_ACTIVE_SIZE,
                    point.x() + CAMERA_NODE_ACTIVE_SIZE, point.y() + CAMERA_NODE_ACTIVE_SIZE, point.z() + CAMERA_NODE_ACTIVE_SIZE);
        }
    }
}
