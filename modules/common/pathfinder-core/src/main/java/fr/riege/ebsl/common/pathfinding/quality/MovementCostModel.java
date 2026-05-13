package fr.riege.ebsl.common.pathfinding.quality;

import fr.riege.ebsl.common.pathfinding.Node;

/**
 * Defines the contract for {@code MovementCostModel} implementations.
 */
public interface MovementCostModel {
    double risk(Node.MoveType type);

    double planningPenalty(Node.MoveType type);
}
