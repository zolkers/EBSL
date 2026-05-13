package fr.riege.ebsl.common.pathfinding.quality;

import fr.riege.ebsl.common.pathfinding.Node;

public enum DefaultMovementCostModel implements MovementCostModel {
    INSTANCE;

    @Override
    public double risk(Node.MoveType type) {
        return MovementRiskScorer.risk(type);
    }

    @Override
    public double planningPenalty(Node.MoveType type) {
        return MovementRiskScorer.planningPenalty(type);
    }
}
