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
    int maxPathNodes,
    int maxCameraNodes,
    float pathLineWidth,
    float cameraLineWidth,
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
            settings.renderMaxPathNodes.value(),
            settings.renderMaxCameraNodes.value(),
            settings.renderPathLineWidth.value().floatValue(),
            settings.renderCameraLineWidth.value().floatValue(),
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
            case RAINBOW -> RenderPaint.solid(rainbowColor(index, limit, nodeColor.a()));
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

    RenderColor cameraNodeColor(boolean active) {
        return active ? CAMERA_NODE_ACTIVE : CAMERA_NODE;
    }

    float cameraNodeLineWidth() {
        return CAMERA_NODE_LINE_WIDTH;
    }
}
