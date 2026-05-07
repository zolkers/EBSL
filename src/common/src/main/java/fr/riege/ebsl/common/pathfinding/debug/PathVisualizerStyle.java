package fr.riege.ebsl.common.pathfinding.debug;

import fr.riege.ebsl.common.pathfinding.Node;
import fr.riege.ebsl.common.pathfinding.settings.PathfinderSettings;
import fr.riege.ebsl.common.platform.render.RenderColor;

record PathVisualizerStyle(
    boolean renderPathNodes,
    boolean renderPathLines,
    boolean renderCameraRail,
    int maxPathNodes,
    int maxCameraNodes,
    float pathLineWidth,
    float cameraLineWidth,
    RenderColor nodeColor,
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
    private static final RenderColor CAMERA_NODE = RenderColor.rgba(0.57f, 0.57f, 0.57f, 0.55f);
    private static final RenderColor CAMERA_NODE_ACTIVE = RenderColor.rgba(1.0f, 0.47f, 1.0f, 0.95f);

    static PathVisualizerStyle from(PathfinderSettings settings) {
        return new PathVisualizerStyle(
            settings.renderPathNodes.value(),
            settings.renderPathLines.value(),
            settings.renderCameraRail.value(),
            settings.renderMaxPathNodes.value(),
            settings.renderMaxCameraNodes.value(),
            settings.renderPathLineWidth.value().floatValue(),
            settings.renderCameraLineWidth.value().floatValue(),
            RenderColor.argb(settings.renderNodeColor.value()),
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

    RenderColor cameraLineColor(boolean done) {
        return done ? CAMERA_LINE_DONE : CAMERA_LINE;
    }

    RenderColor cameraNodeColor(boolean active) {
        return active ? CAMERA_NODE_ACTIVE : CAMERA_NODE;
    }
}
