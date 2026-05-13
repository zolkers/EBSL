package fr.riege.ebsl.common.pathfinding.movement;

import fr.riege.ebsl.common.pathfinding.Node;

@FunctionalInterface
public interface MovementTypeClassifier {
    Node.MoveType classify(MovementClassificationContext context);
}
