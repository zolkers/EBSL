package fr.riege.ebsl.common.pathfinding.pathing.heuristic;

import fr.riege.ebsl.common.pathfinding.wrapper.PathPosition;

/**
 * Defines the contract for {@code IHeuristicStrategy} implementations.
 */
public interface IHeuristicStrategy {
    double calculate(HeuristicContext context);
    double calculateTransitionCost(PathPosition from, PathPosition to);
}
