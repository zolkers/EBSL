package fr.riege.ebsl.common.pathfinding.movement;

import fr.riege.ebsl.common.pathfinding.Node;

/**
 * Classifies a transition between path nodes into the movement type that should own it.
 *
 * <p>The classifier is the shared boundary between planning, quality scoring, and execution so all stages reason about the same movement label.</p>
 */
@FunctionalInterface
public interface MovementTypeClassifier {
    /**
     * Classifies the supplied movement context into the movement type used by planning, quality, and execution.
 *
     * @param context the context describing the operation being performed
     * @return the value defined by this contract
     */
    Node.MoveType classify(MovementClassificationContext context);
}
