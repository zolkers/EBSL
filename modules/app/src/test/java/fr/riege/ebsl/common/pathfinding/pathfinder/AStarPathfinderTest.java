package fr.riege.ebsl.common.pathfinding.pathfinder;

import fr.riege.ebsl.common.pathfinding.pathing.configuration.PathfinderConfiguration;
import fr.riege.ebsl.common.pathfinding.pathing.processing.NodeProcessor;
import fr.riege.ebsl.common.pathfinding.pathing.processing.NodeProcessorRegistry;
import fr.riege.ebsl.common.pathfinding.pathing.processing.context.EvaluationContext;
import fr.riege.ebsl.common.pathfinding.pathing.processing.impl.LayerPathProcessor;
import fr.riege.ebsl.common.pathfinding.pathing.processing.impl.QualityAwarePathProcessor;
import fr.riege.ebsl.common.pathfinding.pathing.result.PathState;
import fr.riege.ebsl.common.pathfinding.pathing.result.PathfinderResult;
import fr.riege.ebsl.common.pathfinding.wrapper.PathPosition;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

    @Test
    void earlyFallbackReturnsUsableProgressBeforeIterationCap() throws Exception {
        AStarPathfinder pathfinder = new AStarPathfinder(PathfinderConfiguration.builder()
            .async(false)
            .fallback(true)
            .maxIterations(10000)
            .earlyFallback(true)
            .earlyFallbackIterations(16)
            .earlyFallbackMinPathNodes(8)
            .earlyFallbackMinProgressRatio(0.01)
            .build());

        PathfinderResult result = pathfinder.findPath(
            new PathPosition(0, 64, 0),
            new PathPosition(1000, 64, 0),
            null
        ).toCompletableFuture().get(1, TimeUnit.SECONDS);

        assertEquals(PathState.FALLBACK, result.getPathState());
        assertEquals(16, pathfinder.getExploredCount());
        assertEquals(17, result.getPath().length());
    }

    @Test
    void timeBudgetReturnsFallbackEvenBeforeEarlyFallbackThreshold() throws Exception {
        AStarPathfinder pathfinder = new AStarPathfinder(PathfinderConfiguration.builder()
            .async(false)
            .fallback(true)
            .maxIterations(10000)
            .maxCalculationTimeMs(1)
            .processors(java.util.List.of(new SlowProcessor()))
            .build());

        PathfinderResult result = pathfinder.findPath(
            new PathPosition(0, 64, 0),
            new PathPosition(1000, 64, 0),
            null
        ).toCompletableFuture().get(1, TimeUnit.SECONDS);

        assertEquals(PathState.FALLBACK, result.getPathState());
        assertTrue(result.getPath().length() > 1);
    }

    @Test
    void processorRegistryCreatesFreshStandardProcessors() {
        assertNotSame(
            NodeProcessorRegistry.createStandardProcessors().getFirst(),
            NodeProcessorRegistry.createStandardProcessors().getFirst()
        );
        assertTrue(NodeProcessorRegistry.createStandardProcessors().stream().anyMatch(LayerPathProcessor.class::isInstance));
        assertTrue(NodeProcessorRegistry.createStandardProcessors().stream().anyMatch(QualityAwarePathProcessor.class::isInstance));
    }

    private static final class SlowProcessor implements NodeProcessor {
        @Override
        public boolean isValid(EvaluationContext context) {
            LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(2));
            return true;
        }
    }
}
