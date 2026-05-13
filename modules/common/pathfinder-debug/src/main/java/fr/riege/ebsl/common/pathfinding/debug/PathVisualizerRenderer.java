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
import fr.riege.ebsl.common.pathfinding.diagnostics.DepthPathSnapshot;
import fr.riege.ebsl.common.platform.render.RenderHandle;
import fr.riege.ebsl.common.platform.render.WorldRender;
import fr.riege.ebsl.common.platform.render.WorldRenderSession;

import java.util.List;

final class PathVisualizerRenderer {
    private static final double PATH_LINE_Y_OFFSET = 0.5;
    private static final double DEPTH_LINE_Y_OFFSET = 0.66;
    private static final double DEPTH_LINE_STRIDE = 0.08;
    private static final double DEPTH_ENDPOINT_SIZE = 0.11;
    private static final double DEPTH_NODE_SIZE = 0.07;
    private static final float CAMERA_NODE_SIZE = 0.10f;
    private static final float CAMERA_NODE_ACTIVE_SIZE = 0.18f;

    private PathVisualizerRenderer() {
    }

    static void render(RenderHandle handle, PathVisualizerSnapshot snapshot, PathVisualizerStyle style) {
        renderDepthPaths(handle, snapshot.depthPaths(), style);
        int pathLimit = Math.min(snapshot.path().size(), style.maxPathNodes());
        if (pathLimit > 0) {
            renderPath(handle, snapshot.path(), pathLimit, style);
        }
        renderCameraRail(handle, snapshot.cameraPath(), snapshot.cameraRailIndex(),
            snapshot.cameraRailVisualProgress(), snapshot.cameraRailVisualPosition(), style);
    }

    private static void renderDepthPaths(RenderHandle handle, List<DepthPathSnapshot> depthPaths, PathVisualizerStyle style) {
        if (!style.renderDepthPaths() || depthPaths.isEmpty()) {
            return;
        }
        for (DepthPathSnapshot depthPath : depthPaths) {
            List<Node> path = depthPath.path();
            int limit = Math.min(path.size(), style.maxDepthPathNodes());
            if (limit < 2) {
                continue;
            }
            double yOffset = DEPTH_LINE_Y_OFFSET + (depthPath.depth() - 1) * DEPTH_LINE_STRIDE;
            try (WorldRenderSession session = WorldRender.session(handle)
                .lineWidth(style.depthLineWidth() + (depthPath.selected() ? 0.55f : 0.0f))
                .color(style.depthLineColor(depthPath.depth(), depthPath.selected(), depthPath.qualityScore()))
                .throughWalls()) {
                for (int i = 0; i + 1 < limit; i++) {
                    Node a = path.get(i);
                    Node b = path.get(i + 1);
                    session.line(
                        a.position.centeredX(), a.position.flooredY() + yOffset, a.position.centeredZ(),
                        b.position.centeredX(), b.position.flooredY() + yOffset, b.position.centeredZ());
                }
            }
            renderDepthNodes(handle, depthPath, path, limit, yOffset, style);
            renderDepthEndpoints(handle, depthPath, path, limit, yOffset, style);
        }
    }

    private static void renderDepthNodes(RenderHandle handle, DepthPathSnapshot depthPath,
                                         List<Node> path, int limit, double yOffset,
                                         PathVisualizerStyle style) {
        int stride = depthPath.selected() ? 1 : Math.max(1, limit / 80);
        try (WorldRenderSession session = WorldRender.session(handle)
            .lineWidth(Math.max(0.8f, style.depthLineWidth() * 0.75f))
            .color(style.depthNodeColor(depthPath.depth(), depthPath.selected(), depthPath.qualityScore()))
            .throughWalls()) {
            for (int i = 0; i < limit; i += stride) {
                renderDepthNode(session, path.get(i), yOffset);
            }
        }
    }

    private static void renderDepthNode(WorldRenderSession session, Node node, double yOffset) {
        double x = node.position.centeredX();
        double y = node.position.flooredY() + yOffset;
        double z = node.position.centeredZ();
        session.wireBox(
            x - DEPTH_NODE_SIZE, y - DEPTH_NODE_SIZE, z - DEPTH_NODE_SIZE,
            x + DEPTH_NODE_SIZE, y + DEPTH_NODE_SIZE, z + DEPTH_NODE_SIZE);
    }

    private static void renderDepthEndpoints(RenderHandle handle, DepthPathSnapshot depthPath,
                                             List<Node> path, int limit, double yOffset,
                                             PathVisualizerStyle style) {
        Node start = path.getFirst();
        Node end = path.get(limit - 1);
        try (WorldRenderSession session = WorldRender.session(handle)
            .lineWidth(style.depthLineWidth())
            .color(style.depthEndpointColor(depthPath.depth(), depthPath.selected()))
            .throughWalls()) {
            renderDepthEndpoint(session, start, yOffset);
            renderDepthEndpoint(session, end, yOffset);
        }
    }

    private static void renderDepthEndpoint(WorldRenderSession session, Node node, double yOffset) {
        double x = node.position.centeredX();
        double y = node.position.flooredY() + yOffset;
        double z = node.position.centeredZ();
        session.wireBox(
            x - DEPTH_ENDPOINT_SIZE, y - DEPTH_ENDPOINT_SIZE, z - DEPTH_ENDPOINT_SIZE,
            x + DEPTH_ENDPOINT_SIZE, y + DEPTH_ENDPOINT_SIZE, z + DEPTH_ENDPOINT_SIZE);
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
