package fr.riege.ebsl.pathfinding.pathing;

import fr.riege.ebsl.pathfinding.pathing.context.EnvironmentContext;
import fr.riege.ebsl.pathfinding.pathing.result.PathfinderResult;
import fr.riege.ebsl.pathfinding.wrapper.PathPosition;
import java.util.concurrent.CompletionStage;

public interface Pathfinder {
    default CompletionStage<PathfinderResult> findPath(PathPosition start, PathPosition target) {
        return findPath(start, target, null);
    }

    CompletionStage<PathfinderResult> findPath(PathPosition start, PathPosition target,
                                               EnvironmentContext context);

    void abort();
}
