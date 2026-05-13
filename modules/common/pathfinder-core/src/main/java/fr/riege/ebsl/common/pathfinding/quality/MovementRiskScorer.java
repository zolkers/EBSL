package fr.riege.ebsl.common.pathfinding.quality;

import fr.riege.ebsl.common.pathfinding.Node;
import fr.riege.ebsl.common.pathfinding.settings.PathfinderSettings;

public final class MovementRiskScorer {
    private MovementRiskScorer() {
    }

    public static double risk(Node.MoveType type) {
        PathfinderSettings settings = PathfinderSettings.instance();
        return switch (type) {
            case WALK -> settings.qualityWalkRisk.value();
            case WALK_DIAGONAL -> settings.qualityDiagonalRisk.value();
            case STEP_DOWN -> settings.qualityStepDownRisk.value();
            case STEP_UP -> settings.qualityStepUpRisk.value();
            case SWIM -> settings.qualitySwimRisk.value();
            case CLIMB -> settings.qualityClimbRisk.value();
            case JUMP -> settings.qualityJumpRisk.value();
            case FALL -> settings.qualityFallRisk.value();
            case PARKOUR -> settings.qualityParkourRisk.value();
            case FLY -> settings.qualityFlyRisk.value();
        };
    }

    public static double planningPenalty(Node.MoveType type) {
        double risk = risk(type);
        if (risk <= 0.0) {
            return 0.0;
        }
        return risk + risk * risk * 2.0;
    }
}
