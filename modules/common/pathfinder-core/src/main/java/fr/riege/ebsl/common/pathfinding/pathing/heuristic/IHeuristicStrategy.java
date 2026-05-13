package fr.riege.ebsl.common.pathfinding.pathing.heuristic;

import fr.riege.ebsl.common.pathfinding.wrapper.PathPosition;

/**
 * Scores distance and transition cost for A* pathfinding.
 *
 * <p>Heuristics must remain stable for a search run and should balance admissibility, terrain preference, and movement realism.</p>
 */
public interface IHeuristicStrategy {
    /**
     * Calculates the heuristic score for the supplied context.
 *
     * @param context the context describing the operation being performed
     * @return the value defined by this contract
     */
    double calculate(HeuristicContext context);
    /**
     * Calculates the movement cost between two adjacent path positions.
 *
     * @param from the first value or starting position
     * @param to the second value or ending position
     * @return the value defined by this contract
     */
    double calculateTransitionCost(PathPosition from, PathPosition to);
}
