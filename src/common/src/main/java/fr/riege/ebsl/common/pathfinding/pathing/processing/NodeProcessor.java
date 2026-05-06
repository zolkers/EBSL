package fr.riege.ebsl.common.pathfinding.pathing.processing;

import fr.riege.ebsl.common.pathfinding.pathing.processing.context.EvaluationContext;

public interface NodeProcessor extends Processor {
    default boolean isValid(EvaluationContext context) {
        return true;
    }

    default Cost calculateCostContribution(EvaluationContext context) {
        return Cost.ZERO;
    }
}
