package fr.riege.ebsl.common.pathfinding.pathing;

import fr.riege.ebsl.common.pathfinding.pathing.context.EnvironmentContext;
import fr.riege.ebsl.common.pathfinding.pathing.result.PathfinderResult;
import fr.riege.ebsl.common.pathfinding.wrapper.PathPosition;
import java.util.concurrent.CompletionStage;

/**
 * Finds paths asynchronously between navigation positions.
 *
 * <p>Implementations own search state, processor coordination, result reporting, and abort behavior for the active planning request.</p>
 */
public interface Pathfinder {
    /**
     * Starts an asynchronous path search for the supplied navigation endpoints.
 *
     * @param start the starting path position
     * @param target the target path position
     * @return a completion stage that resolves to the asynchronous operation result
     */
    default CompletionStage<PathfinderResult> findPath(PathPosition start, PathPosition target) {
        return findPath(start, target, null);
    }

    /**
     * Starts an asynchronous path search for the supplied navigation endpoints.
 *
     * @param start the starting path position
     * @param target the target path position
     * @param context the context describing the operation being performed
     * @return a completion stage that resolves to the asynchronous operation result
     */
    CompletionStage<PathfinderResult> findPath(PathPosition start, PathPosition target,
                                               EnvironmentContext context);

    /**
     * Requests cancellation of the active operation.
     */
    void abort();
}
