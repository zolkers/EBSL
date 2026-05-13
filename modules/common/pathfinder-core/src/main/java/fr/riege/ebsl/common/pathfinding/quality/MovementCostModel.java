package fr.riege.ebsl.common.pathfinding.quality;

import fr.riege.ebsl.common.pathfinding.Node;

public interface MovementCostModel {
    double risk(Node.MoveType type);

    double planningPenalty(Node.MoveType type);
}
