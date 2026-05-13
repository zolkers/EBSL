package fr.riege.ebsl.common.pathfinding.pathing.processing;

import fr.riege.ebsl.common.pathfinding.pathing.processing.context.EvaluationContext;

/**
 * Participates in validation and cost calculation for candidate path nodes.
 *
 * <p>Processors may reject impossible transitions or add structured cost contributions without owning the full search algorithm.</p>
 */
public interface NodeProcessor extends Processor {
    /**
     * Returns whether valid is true for the current state.
 *
     * @param context the context describing the operation being performed
     * @return true when the condition is satisfied; false otherwise
     */
    default boolean isValid(EvaluationContext context) {
        return true;
    }

    /**
     * Calculates this processor cost contribution for the current candidate transition.
 *
     * @param context the context describing the operation being performed
     * @return the value defined by this contract
     */
    default Cost calculateCostContribution(EvaluationContext context) {
        return Cost.ZERO;
    }
}
