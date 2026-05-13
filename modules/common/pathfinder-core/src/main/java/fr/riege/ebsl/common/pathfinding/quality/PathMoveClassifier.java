package fr.riege.ebsl.common.pathfinding.quality;

import fr.riege.ebsl.common.pathfinding.Node;
import fr.riege.ebsl.common.pathfinding.movement.MovementClassifier;
import fr.riege.ebsl.common.pathfinding.movement.WalkabilityChecker;
import fr.riege.ebsl.common.pathfinding.pathing.context.EnvironmentContext;
import fr.riege.ebsl.common.pathfinding.provider.NavigationPointProvider;
import fr.riege.ebsl.common.pathfinding.wrapper.PathPosition;

@Deprecated
public final class PathMoveClassifier {
    private PathMoveClassifier() {
    }

    public static Node.MoveType classify(PathPosition previous,
                                         PathPosition current,
                                         NavigationPointProvider provider,
                                         EnvironmentContext environmentContext,
                                         WalkabilityChecker checker) {
        return MovementClassifier.classify(previous, current, provider, environmentContext, checker);
    }
}
