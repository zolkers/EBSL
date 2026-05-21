/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 *
 * Copyright (C) 2026 Victor Riegert and EBSL contributors
 *
 * This file is part of EBSL.
 *
 * EBSL is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * EBSL is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public
 * License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with EBSL. If not, see <https://www.gnu.org/licenses/>.
 */

package fr.riege.ebsl.common.pathfinding.pathfinder;

import fr.riege.ebsl.common.pathfinding.Node;
import fr.riege.ebsl.common.pathfinding.pathing.InspectablePathfinder;
import fr.riege.ebsl.common.pathfinding.pathing.INeighborStrategy;
import fr.riege.ebsl.common.pathfinding.pathing.configuration.PathfinderConfiguration;
import fr.riege.ebsl.common.pathfinding.pathing.heuristic.HeuristicContext;
import fr.riege.ebsl.common.pathfinding.pathing.heuristic.HeuristicWeights;
import fr.riege.ebsl.common.pathfinding.pathing.heuristic.IHeuristicStrategy;
import fr.riege.ebsl.common.pathfinding.pathfinder.heap.PrimitiveMinHeap;
import fr.riege.ebsl.common.pathfinding.pathfinder.processing.EvaluationContextImpl;
import fr.riege.ebsl.common.pathfinding.pathing.processing.Cost;
import fr.riege.ebsl.common.pathfinding.pathing.processing.NodeProcessor;
import fr.riege.ebsl.common.pathfinding.pathing.processing.NodeProcessorRegistry;
import fr.riege.ebsl.common.pathfinding.pathing.processing.context.EvaluationContext;
import fr.riege.ebsl.common.pathfinding.pathing.processing.impl.LayerPathProcessor;
import fr.riege.ebsl.common.pathfinding.pathing.processing.impl.QualityAwarePathProcessor;
import fr.riege.ebsl.common.pathfinding.pathing.result.PathState;
import fr.riege.ebsl.common.pathfinding.pathing.result.PathfinderResult;
import fr.riege.ebsl.common.pathfinding.wrapper.PathPosition;
import fr.riege.ebsl.common.pathfinding.wrapper.PathVector;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

import static org.junit.jupiter.api.Assertions.*;

class AStarPathfinderTest {
    @Test
    void startAlreadyAtTargetIsFoundImmediately() throws Exception {
        InspectablePathfinder pathfinder = Pathfinders.inspectableAStar(PathfinderConfiguration.builder()
            .async(false)
            .fallback(false)
            .build());

        PathfinderResult result = pathfinder.findPath(
            new PathPosition(4, 64, 4),
            new PathPosition(4, 64, 4),
            null
        ).toCompletableFuture().get(1, TimeUnit.SECONDS);

        assertEquals(PathState.FOUND, result.getPathState());
        assertEquals(1, result.getPath().length());
    }

    @Test
    void emptyOpenSetHasInfiniteRawCostLowerBound() {
        AStarPathfinder pathfinder = new AStarPathfinder(PathfinderConfiguration.builder().async(false).build());

        double lowerBound = pathfinder.openSetRawFCostLowerBound(new PrimitiveMinHeap(4));

        assertEquals(Double.POSITIVE_INFINITY, lowerBound);
    }

    @Test
    void invalidTransitionCostIsRejected() {
        PathPosition start = new PathPosition(0, 64, 0);
        PathPosition next = new PathPosition(1, 64, 0);
        Node parent = new Node(start, start, next, HeuristicWeights.DEFAULT_WEIGHTS, new InvalidTransitionHeuristic(), 0);
        Node child = new Node(next, start, next, HeuristicWeights.DEFAULT_WEIGHTS, new InvalidTransitionHeuristic(), 1);
        EvaluationContextImpl context = new EvaluationContextImpl(null, child, parent, new InvalidTransitionHeuristic());

        assertEquals(1, context.getCurrentNodeDepth());
        assertThrows(IllegalStateException.class, context::getBaseTransitionCost);
    }

    @Test
    void openRawCostAccountingIgnoresMissingAndNonFiniteCosts() throws Exception {
        Class<?> sessionType = Class.forName(AStarPathfinder.class.getName() + "$PathfindingSession");
        Constructor<?> constructor = sessionType.getDeclaredConstructor();
        constructor.setAccessible(true);
        Object session = constructor.newInstance();

        Method addOpenRawFCost = sessionType.getDeclaredMethod("addOpenRawFCost", double.class);
        Method removeOpenRawFCost = sessionType.getDeclaredMethod("removeOpenRawFCost", double.class);
        Method minOpenRawFCost = sessionType.getDeclaredMethod("minOpenRawFCost");
        addOpenRawFCost.setAccessible(true);
        removeOpenRawFCost.setAccessible(true);
        minOpenRawFCost.setAccessible(true);

        assertEquals(Double.POSITIVE_INFINITY, (double) minOpenRawFCost.invoke(session));

        removeOpenRawFCost.invoke(session, Double.POSITIVE_INFINITY);
        removeOpenRawFCost.invoke(session, 12.0);
        addOpenRawFCost.invoke(session, 3.0);
        addOpenRawFCost.invoke(session, 3.0);

        assertEquals(3.0, (double) minOpenRawFCost.invoke(session));

        removeOpenRawFCost.invoke(session, 3.0);
        assertEquals(3.0, (double) minOpenRawFCost.invoke(session));

        removeOpenRawFCost.invoke(session, 3.0);
        assertEquals(Double.POSITIVE_INFINITY, (double) minOpenRawFCost.invoke(session));
    }

    @Test
    void canReturnFirstReachWhenGoalRefinementIsDisabled() throws Exception {
        InspectablePathfinder pathfinder = Pathfinders.inspectableAStar(PathfinderConfiguration.builder()
            .async(false)
            .fallback(false)
            .goalRefinement(false)
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
    void refinesPastFirstReachWhenCheaperGoalPathExists() throws Exception {
        InspectablePathfinder pathfinder = Pathfinders.inspectableAStar(PathfinderConfiguration.builder()
            .async(false)
            .fallback(false)
            .maxIterations(128)
            .goalRefinement(true)
            .goalRefinementMinIterations(0)
            .goalRefinementMaxIterations(128)
            .goalRefinementMaxTimeMs(0)
            .processors(List.of(new ExpensiveDirectGoalStep()))
            .build());

        PathPosition start = new PathPosition(0, 64, 0);
        PathPosition target = new PathPosition(1, 64, 0);
        PathfinderResult result = pathfinder.findPath(start, target, null)
            .toCompletableFuture()
            .get(1, TimeUnit.SECONDS);

        List<PathPosition> positions = new ArrayList<>(result.getPath().collect());
        assertEquals(PathState.FOUND, result.getPathState());
        assertTrue(pathfinder.getExploredCount() > 1);
        assertTrue(positions.size() > 2);
        assertEquals(start, positions.getFirst());
        assertEquals(target, positions.getLast());
    }

    @Test
    void reopensClosedNodeWhenBetterGCostIsDiscovered() throws Exception {
        PathPosition start = new PathPosition(0, 64, 0);
        PathPosition highCostFirstHop = new PathPosition(1, 64, 0);
        PathPosition cheaperDetour = new PathPosition(0, 64, 1);
        PathPosition target = new PathPosition(2, 64, 0);

        InspectablePathfinder pathfinder = Pathfinders.inspectableAStar(PathfinderConfiguration.builder()
            .async(false)
            .fallback(false)
            .maxIterations(64)
            .goalRefinement(true)
            .goalRefinementMinIterations(10)
            .goalRefinementMaxIterations(64)
            .goalRefinementMaxTimeMs(0)
            .neighborStrategy(new DirectedReopenGraph(start, highCostFirstHop, cheaperDetour, target))
            .heuristicStrategy(new InconsistentReopenHeuristic(cheaperDetour))
            .processors(List.of(new ExpensiveRootHop(highCostFirstHop)))
            .build());

        PathfinderResult result = pathfinder.findPath(start, target, null)
            .toCompletableFuture()
            .get(1, TimeUnit.SECONDS);

        List<PathPosition> positions = new ArrayList<>(result.getPath().collect());
        assertEquals(PathState.FOUND, result.getPathState());
        assertEquals(List.of(start, cheaperDetour, highCostFirstHop, target), positions);
    }

    @Test
    void defaultHeuristicWeightsStayOctileOnlyForOptimalSearchAssumption() {
        HeuristicWeights weights = HeuristicWeights.DEFAULT_WEIGHTS;

        assertEquals(0.0, weights.manhattanWeight);
        assertEquals(1.0, weights.octileWeight);
        assertEquals(0.0, weights.perpendicularWeight);
        assertEquals(0.0, weights.heightWeight);
    }

    @Test
    void fallbackStillReturnsForwardProgressWhenSearchIsCapped() throws Exception {
        InspectablePathfinder pathfinder = Pathfinders.inspectableAStar(PathfinderConfiguration.builder()
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
        InspectablePathfinder pathfinder = Pathfinders.inspectableAStar(PathfinderConfiguration.builder()
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
        InspectablePathfinder pathfinder = Pathfinders.inspectableAStar(PathfinderConfiguration.builder()
            .async(false)
            .fallback(true)
            .maxIterations(10000)
            .maxCalculationTimeMs(1)
            .processors(List.of(new SlowProcessor()))
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

    private static final class ExpensiveDirectGoalStep implements NodeProcessor {
        @Override
        public Cost calculateCostContribution(EvaluationContext context) {
            boolean startsAtRoot = context.getPreviousPathPosition().equals(context.getStartPathPosition());
            boolean reachesTarget = context.getCurrentPathPosition().equals(context.getTargetPathPosition());
            return startsAtRoot && reachesTarget ? Cost.of(100.0) : Cost.ZERO;
        }
    }

    private static final class ExpensiveRootHop implements NodeProcessor {
        private final PathPosition expensiveHop;

        private ExpensiveRootHop(PathPosition expensiveHop) {
            this.expensiveHop = expensiveHop;
        }

        @Override
        public Cost calculateCostContribution(EvaluationContext context) {
            boolean startsAtRoot = context.getPreviousPathPosition().equals(context.getStartPathPosition());
            boolean takesExpensiveHop = context.getCurrentPathPosition().equals(expensiveHop);
            return startsAtRoot && takesExpensiveHop ? Cost.of(4.0) : Cost.ZERO;
        }
    }

    private static final class DirectedReopenGraph implements INeighborStrategy {
        private final PathPosition start;
        private final PathPosition highCostFirstHop;
        private final PathPosition cheaperDetour;
        private final PathPosition target;

        private DirectedReopenGraph(PathPosition start, PathPosition highCostFirstHop,
                                    PathPosition cheaperDetour, PathPosition target) {
            this.start = start;
            this.highCostFirstHop = highCostFirstHop;
            this.cheaperDetour = cheaperDetour;
            this.target = target;
        }

        @Override
        public Iterable<PathVector> getOffsets() {
            return List.of();
        }

        @Override
        public Iterable<PathVector> getOffsets(PathPosition currentPosition) {
            if (currentPosition.equals(start)) {
                return List.of(offset(start, highCostFirstHop), offset(start, cheaperDetour));
            }
            if (currentPosition.equals(highCostFirstHop)) {
                return List.of(offset(highCostFirstHop, target));
            }
            if (currentPosition.equals(cheaperDetour)) {
                return List.of(offset(cheaperDetour, highCostFirstHop));
            }
            return List.of();
        }

        private static PathVector offset(PathPosition from, PathPosition to) {
            return new PathVector(to.x - from.x, to.y - from.y, to.z - from.z);
        }
    }

    private static final class InconsistentReopenHeuristic implements IHeuristicStrategy {
        private final PathPosition delayedDetour;

        private InconsistentReopenHeuristic(PathPosition delayedDetour) {
            this.delayedDetour = delayedDetour;
        }

        @Override
        public double calculate(HeuristicContext context) {
            return context.pathfindingProgress.current.equals(delayedDetour) ? 10.0 : 0.0;
        }

        @Override
        public double calculateTransitionCost(PathPosition from, PathPosition to) {
            return from.distance(to);
        }
    }

    private static final class InvalidTransitionHeuristic implements IHeuristicStrategy {
        @Override
        public double calculate(HeuristicContext context) {
            return 0.0;
        }

        @Override
        public double calculateTransitionCost(PathPosition from, PathPosition to) {
            return Double.NaN;
        }
    }
}
