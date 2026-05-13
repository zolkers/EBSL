package fr.riege.ebsl.common.pathfinding.movement;

import fr.riege.ebsl.common.pathfinding.Node;

/**
 * Defines the contract for {@code MovementTypeClassifier} implementations.
 */
@FunctionalInterface
public interface MovementTypeClassifier {
    Node.MoveType classify(MovementClassificationContext context);
}
