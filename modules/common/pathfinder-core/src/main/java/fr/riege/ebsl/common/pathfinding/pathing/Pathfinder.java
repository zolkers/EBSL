package fr.riege.ebsl.common.pathfinding.pathing;

import fr.riege.ebsl.common.pathfinding.pathing.context.EnvironmentContext;
import fr.riege.ebsl.common.pathfinding.pathing.result.PathfinderResult;
import fr.riege.ebsl.common.pathfinding.wrapper.PathPosition;
import java.util.concurrent.CompletionStage;

/**
 * Defines the contract for {@code Pathfinder} implementations.
 */
public interface Pathfinder {
    default CompletionStage<PathfinderResult> findPath(PathPosition start, PathPosition target) {
        return findPath(start, target, null);
    }

    CompletionStage<PathfinderResult> findPath(PathPosition start, PathPosition target,
                                               EnvironmentContext context);

    void abort();
}
