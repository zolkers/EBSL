package fr.riege.ebsl.common.feature.ui.imgui.panel;

import imgui.ImDrawList;

import java.util.ArrayList;
import java.util.List;

final class GraphEdgePainter {
    private static final float EDGE_NODE_MARGIN = 22.0f;
    private static final float EDGE_PORT_STUB = 26.0f;

    private GraphEdgePainter() {
    }

    static void draw(ImDrawList dl, ScriptGraphNodeLayout from, ScriptGraphNodeLayout to, List<ScriptGraphNodeLayout> layouts, float graphZoom) {
        float portInset = 10.0f * graphZoom;
        float fromX = from.right() - portInset;
        float fromY = from.centerY();
        float toX = to.x() + portInset;
        float toY = to.centerY();
        float stub = EDGE_PORT_STUB * graphZoom;
        float startLaneX = from.right() + stub;
        float endLaneX = to.x() - stub;
        float startRouteY = fromY;
        float endRouteY = toY;
        if (needsSeparatedPortLanes(fromX, startLaneX, fromY, endLaneX, toX, toY)) {
            float separation = Math.max(10.0f, 14.0f * graphZoom);
            float direction = to.centerY() >= from.centerY() ? 1.0f : -1.0f;
            startRouteY = fromY - direction * separation;
            endRouteY = toY + direction * separation;
        }
        int color = to.node().depth() > 0 ? 0xAA67B7FF : 0xCC67B7FF;
        List<GraphEdgeRouter.EdgePoint> route = GraphEdgeRouter.route(
            new GraphEdgeRouter.EdgePoint(startLaneX, startRouteY),
            new GraphEdgeRouter.EdgePoint(endLaneX, endRouteY),
            edgeNodeBounds(layouts),
            EDGE_NODE_MARGIN * graphZoom);

        drawSegment(dl, fromX, fromY, startLaneX, fromY, color);
        drawSegment(dl, startLaneX, fromY, startLaneX, startRouteY, color);
        for (int i = 1; i < route.size(); i++) {
            GraphEdgeRouter.EdgePoint a = route.get(i - 1);
            GraphEdgeRouter.EdgePoint b = route.get(i);
            drawSegment(dl, a.x(), a.y(), b.x(), b.y(), color);
        }
        drawSegment(dl, endLaneX, endRouteY, endLaneX, toY, color);
        drawSegment(dl, endLaneX, toY, toX, toY, color);
        dl.addCircleFilled(fromX, fromY, 3.5f, 0xFF67B7FF);
        dl.addCircleFilled(toX, toY, 3.5f, 0xFF67B7FF);
    }

    private static boolean needsSeparatedPortLanes(float startA, float startB, float startY,
                                                   float endA, float endB, float endY) {
        if (Math.abs(startY - endY) > 1.0f) {
            return false;
        }
        return startB >= endA || horizontalRangesOverlap(startA, startB, endA, endB);
    }

    private static boolean horizontalRangesOverlap(float a1, float a2, float b1, float b2) {
        float aMin = Math.min(a1, a2);
        float aMax = Math.max(a1, a2);
        float bMin = Math.min(b1, b2);
        float bMax = Math.max(b1, b2);
        return Math.min(aMax, bMax) - Math.max(aMin, bMin) > 1.0f;
    }

    private static List<GraphEdgeRouter.NodeBounds> edgeNodeBounds(List<ScriptGraphNodeLayout> layouts) {
        List<GraphEdgeRouter.NodeBounds> nodes = new ArrayList<>();
        for (ScriptGraphNodeLayout layout : layouts) {
            nodes.add(new GraphEdgeRouter.NodeBounds(layout.x(), layout.y(), layout.width(), layout.height()));
        }
        return nodes;
    }

    private static void drawSegment(ImDrawList dl, float x1, float y1, float x2, float y2, int color) {
        if (Math.abs(x1 - x2) < 0.5f && Math.abs(y1 - y2) < 0.5f) {
            return;
        }
        dl.addLine(x1, y1, x2, y2, color, 2.0f);
    }
}
