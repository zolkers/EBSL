package fr.riege.ebsl.common.pathfinding.movement;

import fr.riege.ebsl.common.pathfinding.pathing.context.EnvironmentContext;
import fr.riege.ebsl.common.pathfinding.provider.NavigationPointProvider;
import fr.riege.ebsl.common.pathfinding.wrapper.PathPosition;

public record MovementClassificationContext(
    PathPosition previous,
    PathPosition current,
    NavigationPointProvider provider,
    EnvironmentContext environmentContext,
    WalkabilityChecker checker
) {
}
