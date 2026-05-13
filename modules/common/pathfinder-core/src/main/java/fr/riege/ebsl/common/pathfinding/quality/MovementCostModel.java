package fr.riege.ebsl.common.pathfinding.quality;

import fr.riege.ebsl.common.pathfinding.Node;

/**
 * Defines risk and planning penalties for movement types.
 *
 * <p>Quality metrics and processors use this model to prefer human-like routes over fast but fragile obstacle-heavy paths.</p>
 */
public interface MovementCostModel {
    /**
     * Returns the quality risk assigned to the supplied movement type.
 *
     * @param type the movement or event type being evaluated
     * @return the value defined by this contract
     */
    double risk(Node.MoveType type);

    /**
     * Returns the planning penalty assigned to the supplied movement type.
 *
     * @param type the movement or event type being evaluated
     * @return the value defined by this contract
     */
    double planningPenalty(Node.MoveType type);
}
