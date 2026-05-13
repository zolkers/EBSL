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

import fr.riege.ebsl.common.pathfinding.Node;
import fr.riege.ebsl.common.pathfinding.settings.PathRenderColorMode;
import fr.riege.ebsl.common.pathfinding.settings.PathfinderSettings;
import fr.riege.ebsl.common.platform.render.RenderColor;
import fr.riege.ebsl.common.platform.render.RenderPaint;

record PathVisualizerStyle(
    boolean renderPathNodes,
    boolean renderPathLines,
    boolean renderCameraRail,
    boolean renderDepthPaths,
    int maxPathNodes,
    int maxCameraNodes,
    int maxDepthPathNodes,
    float pathLineWidth,
    float cameraLineWidth,
    float depthLineWidth,
    PathRenderColorMode pathColorMode,
    RenderColor nodeColor,
    RenderColor gradientStartColor,
    RenderColor gradientEndColor,
    RenderColor startColor,
    RenderColor endColor,
    RenderColor walkColor,
    RenderColor jumpColor,
    RenderColor fallColor,
    RenderColor waterColor,
    RenderColor climbColor,
    RenderColor flyColor
) {
    private static final RenderColor CAMERA_LINE = RenderColor.rgba(0.47f, 0.86f, 1.0f, 0.78f);
    private static final RenderColor CAMERA_LINE_DONE = RenderColor.rgba(0.47f, 0.55f, 0.63f, 0.47f);
    private static final RenderColor CAMERA_NODE = RenderColor.rgba(0.57f, 0.57f, 0.57f, 0.36f);
    private static final RenderColor CAMERA_NODE_ACTIVE = RenderColor.rgba(1.0f, 0.47f, 1.0f, 0.95f);
    private static final float CAMERA_NODE_LINE_WIDTH = 1.0f;

    static PathVisualizerStyle from(PathfinderSettings settings) {
        return new PathVisualizerStyle(
            settings.renderPathNodes.value(),
            settings.renderPathLines.value(),
            settings.renderCameraRail.value(),
            settings.renderDepthPaths.value(),
            settings.renderMaxPathNodes.value(),
            settings.renderMaxCameraNodes.value(),
            settings.renderMaxDepthPathNodes.value(),
            settings.renderPathLineWidth.value().floatValue(),
            settings.renderCameraLineWidth.value().floatValue(),
            settings.renderDepthLineWidth.value().floatValue(),
            settings.renderPathColorMode.value(),
            RenderColor.argb(settings.renderNodeColor.value()),
            RenderColor.argb(settings.renderGradientStartColor.value()),
            RenderColor.argb(settings.renderGradientEndColor.value()),
            RenderColor.argb(settings.renderStartColor.value()),
            RenderColor.argb(settings.renderEndColor.value()),
            RenderColor.argb(settings.renderWalkColor.value()),
            RenderColor.argb(settings.renderJumpColor.value()),
            RenderColor.argb(settings.renderFallColor.value()),
            RenderColor.argb(settings.renderWaterColor.value()),
            RenderColor.argb(settings.renderClimbColor.value()),
            RenderColor.argb(settings.renderFlyColor.value()));
    }

    RenderColor pathColor(Node.MoveType moveType) {
        return switch (moveType) {
            case JUMP, PARKOUR, STEP_UP -> jumpColor;
            case FALL, STEP_DOWN -> fallColor;
            case SWIM -> waterColor;
            case CLIMB -> climbColor;
            case FLY -> flyColor;
            case WALK, WALK_DIAGONAL -> walkColor;
        };
    }

    RenderPaint nodePaint(int index, int limit) {
        return switch (pathColorMode) {
            case GRADIENT -> RenderPaint.solid(gradientColor(index, limit));
            case RAINBOW -> RenderPaint.solid(nodeColor);
            case SOLID, MOVE_TYPE -> RenderPaint.solid(nodeColor);
        };
    }

    RenderPaint pathPaint(Node.MoveType moveType, int index, int limit) {
        return switch (pathColorMode) {
            case SOLID -> RenderPaint.solid(walkColor);
            case GRADIENT -> RenderPaint.gradient(gradientColor(index, limit), gradientColor(index + 1, limit));
            case RAINBOW -> RenderPaint.gradient(
                rainbowColor(index, limit, pathColor(moveType).a()),
                rainbowColor(index + 1, limit, pathColor(moveType).a()));
            case MOVE_TYPE -> RenderPaint.solid(pathColor(moveType));
        };
    }

    private RenderColor gradientColor(int index, int limit) {
        int max = Math.max(1, limit - 1);
        return gradientStartColor.lerp(gradientEndColor, index / (float) max);
    }

    private RenderColor rainbowColor(int index, int limit, float alpha) {
        int max = Math.max(1, limit - 1);
        return RenderPaint.rainbow(alpha, 1.0f, 1.0f, 0.82f, 0.18f).colorAt(index / (float) max);
    }

    RenderColor cameraLineColor(boolean done) {
        return done ? CAMERA_LINE_DONE : CAMERA_LINE;
    }

    RenderColor cameraLineColor(double visualProgress, int segmentIndex) {
        double fade = Math.clamp(visualProgress - segmentIndex, 0.0, 1.0);
        return CAMERA_LINE.lerp(CAMERA_LINE_DONE, (float) fade);
    }

    RenderColor cameraNodeColor(boolean active) {
        return active ? CAMERA_NODE_ACTIVE : CAMERA_NODE;
    }

    RenderColor depthLineColor(int depth, boolean selected, double qualityScore) {
        float alpha = selected ? 0.92f : 0.34f + (float) (Math.clamp(qualityScore, 0.0, 1.0) * 0.24);
        float brightness = selected ? 1.0f : 0.72f;
        return RenderColor.hsv((depth * 0.173f) % 1.0f, 0.78f, brightness, alpha);
    }

    RenderColor depthEndpointColor(int depth, boolean selected) {
        return depthLineColor(depth, selected, selected ? 1.0 : 0.55).withAlpha(selected ? 0.88f : 0.42f);
    }

    RenderColor depthNodeColor(int depth, boolean selected, double qualityScore) {
        return depthLineColor(depth, selected, qualityScore).withAlpha(selected ? 0.86f : 0.50f);
    }

    float cameraNodeLineWidth() {
        return CAMERA_NODE_LINE_WIDTH;
    }
}
