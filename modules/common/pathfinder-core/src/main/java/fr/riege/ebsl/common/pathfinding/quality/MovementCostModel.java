package fr.riege.ebsl.common.pathfinding.quality;

import fr.riege.ebsl.common.pathfinding.Node;

/**
 * Centralizes movement risk and planning penalties so A* quality costs and path quality reports use compatible semantics.
 */
public interface MovementCostModel {
    double risk(Node.MoveType type);

    double planningPenalty(Node.MoveType type);
}
