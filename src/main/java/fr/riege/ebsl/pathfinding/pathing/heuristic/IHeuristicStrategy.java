package fr.riege.ebsl.pathfinding.pathing.heuristic;

import fr.riege.ebsl.pathfinding.wrapper.PathPosition;

public interface IHeuristicStrategy {
    double calculate(HeuristicContext context);
    double calculateTransitionCost(PathPosition from, PathPosition to);
}
