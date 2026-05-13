package fr.riege.ebsl.common.pathfinding.movement;

import fr.riege.ebsl.common.pathfinding.Node;

/**
 * Assigns the semantic movement type for one path transition.
 */
@FunctionalInterface
public interface MovementTypeClassifier {
    Node.MoveType classify(MovementClassificationContext context);
}
