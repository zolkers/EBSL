package fr.riege.ebsl.common.pathfinding.pathfinder;

import fr.riege.ebsl.common.pathfinding.pathing.configuration.PathfinderConfiguration;
import fr.riege.ebsl.common.pathfinding.pathing.result.PathState;
import fr.riege.ebsl.common.pathfinding.pathing.result.PathfinderResult;
import fr.riege.ebsl.common.pathfinding.wrapper.PathPosition;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AStarPathfinderTest {
    @Test
    void returnsAsSoonAsAValidSuccessorReachesTarget() throws Exception {
        AStarPathfinder pathfinder = new AStarPathfinder(PathfinderConfiguration.builder()
            .async(false)
            .fallback(false)
            .build());

        PathfinderResult result = pathfinder.findPath(
            new PathPosition(0, 64, 0),
            new PathPosition(1, 64, 0),
            null
        ).toCompletableFuture().get(1, TimeUnit.SECONDS);

        assertEquals(PathState.FOUND, result.getPathState());
        assertEquals(2, result.getPath().length());
        assertEquals(1, pathfinder.getExploredCount());
    }

    @Test
    void fallbackStillReturnsForwardProgressWhenSearchIsCapped() throws Exception {
        AStarPathfinder pathfinder = new AStarPathfinder(PathfinderConfiguration.builder()
            .async(false)
            .fallback(true)
            .maxIterations(8)
            .build());

        PathfinderResult result = pathfinder.findPath(
            new PathPosition(0, 64, 0),
            new PathPosition(100, 64, 0),
            null
        ).toCompletableFuture().get(1, TimeUnit.SECONDS);

        assertEquals(PathState.MAX_ITERATIONS_REACHED, result.getPathState());
        assertEquals(8, pathfinder.getExploredCount());
        assertEquals(9, result.getPath().length());
    }
}
