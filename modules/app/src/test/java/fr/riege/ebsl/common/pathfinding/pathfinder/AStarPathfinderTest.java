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
import fr.riege.ebsl.common.pathfinding.pathing.action.MovementAction;
import fr.riege.ebsl.common.pathfinding.pathing.configuration.PathfinderConfiguration;
import fr.riege.ebsl.common.pathfinding.pathing.goal.PathGoals;
import fr.riege.ebsl.common.pathfinding.pathing.processing.Cost;
import fr.riege.ebsl.common.pathfinding.pathing.processing.NodeProcessor;
import fr.riege.ebsl.common.pathfinding.pathing.processing.NodeProcessorRegistry;
import fr.riege.ebsl.common.pathfinding.pathing.processing.context.EvaluationContext;
import fr.riege.ebsl.common.pathfinding.pathing.processing.impl.LayerPathProcessor;
import fr.riege.ebsl.common.pathfinding.pathing.processing.impl.QualityAwarePathProcessor;
import fr.riege.ebsl.common.pathfinding.pathing.result.PathState;
import fr.riege.ebsl.common.pathfinding.pathing.result.PathfinderResult;
import fr.riege.ebsl.common.pathfinding.pathing.state.SearchState;
import fr.riege.ebsl.common.pathfinding.wrapper.PathPosition;
import fr.riege.ebsl.common.pathfinding.wrapper.PathVector;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

import static org.junit.jupiter.api.Assertions.*;

class AStarPathfinderTest {
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
    void canStopAtAbstractNearGoal() throws Exception {
        InspectablePathfinder pathfinder = Pathfinders.inspectableAStar(PathfinderConfiguration.builder()
            .async(false)
            .fallback(false)
            .goalRefinement(false)
            .build());

        PathPosition target = new PathPosition(10, 64, 0);
        PathfinderResult result = pathfinder.findPath(
            new PathPosition(0, 64, 0),
            PathGoals.near(target, 2),
            null
        ).toCompletableFuture().get(1, TimeUnit.SECONDS);

        PathPosition reached = new ArrayList<>(result.getPath().collect()).getLast();
        assertEquals(PathState.FOUND, result.getPathState());
        assertTrue(Math.abs(reached.flooredX() - target.flooredX()) <= 2);
        assertNotEquals(target, reached);
    }

    @Test
    void movementActionsCanBiasSearchCost() throws Exception {
        InspectablePathfinder pathfinder = Pathfinders.inspectableAStar(PathfinderConfiguration.builder()
            .async(false)
            .fallback(false)
            .maxIterations(128)
            .goalRefinement(true)
            .goalRefinementMinIterations(0)
            .goalRefinementMaxIterations(128)
            .goalRefinementMaxTimeMs(0)
            .neighborStrategy(new ExpensiveDirectEastStrategy())
            .build());

        PathPosition start = new PathPosition(0, 64, 0);
        PathPosition target = new PathPosition(1, 64, 0);
        PathfinderResult result = pathfinder.findPath(start, target, null)
            .toCompletableFuture()
            .get(1, TimeUnit.SECONDS);

        List<PathPosition> positions = new ArrayList<>(result.getPath().collect());
        assertEquals(PathState.FOUND, result.getPathState());
        assertEquals(start, positions.getFirst());
        assertEquals(target, positions.getLast());
        assertTrue(positions.size() > 2);
    }

    @Test
    void searchKeepsDistinctArrivalStatesForSamePosition() throws Exception {
        InspectablePathfinder pathfinder = Pathfinders.inspectableAStar(PathfinderConfiguration.builder()
            .async(false)
            .fallback(false)
            .maxIterations(128)
            .goalRefinement(true)
            .goalRefinementMinIterations(0)
            .goalRefinementMaxIterations(128)
            .goalRefinementMaxTimeMs(0)
            .neighborStrategy(new ArrivalStateStrategy())
            .build());

        PathPosition start = new PathPosition(0, 64, 0);
        PathPosition target = new PathPosition(2, 64, 0);
        PathfinderResult result = pathfinder.findPath(start, target, null)
            .toCompletableFuture()
            .get(1, TimeUnit.SECONDS);

        List<PathPosition> positions = new ArrayList<>(result.getPath().collect());
        assertEquals(PathState.FOUND, result.getPathState());
        assertEquals(List.of(
            start,
            new PathPosition(0, 64, 1),
            new PathPosition(1, 64, 0),
            target
        ), positions);
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

    private static final class ExpensiveDirectEastStrategy implements INeighborStrategy {
        @Override
        public Iterable<PathVector> getOffsets() {
            return List.of(
                new PathVector(1, 0, 0),
                new PathVector(-1, 0, 0),
                new PathVector(0, 0, 1),
                new PathVector(0, 0, -1)
            );
        }

        @Override
        public Iterable<MovementAction> getActions(PathPosition currentPosition) {
            double eastMultiplier = currentPosition.flooredX() == 0 && currentPosition.flooredZ() == 0 ? 100.0 : 1.0;
            return List.of(
                MovementAction.offset(new PathVector(1, 0, 0)).withCost(eastMultiplier, 0.0),
                MovementAction.offset(new PathVector(-1, 0, 0)),
                MovementAction.offset(new PathVector(0, 0, 1)),
                MovementAction.offset(new PathVector(0, 0, -1))
            );
        }
    }

    private static final class ArrivalStateStrategy implements INeighborStrategy {
        @Override
        public Iterable<PathVector> getOffsets() {
            return List.of();
        }

        @Override
        public Iterable<MovementAction> getActions(SearchState currentState) {
            PathPosition position = currentState.position();
            if (position.equals(new PathPosition(0, 64, 0))) {
                return List.of(
                    MovementAction.offset(new PathVector(1, 0, 0)).withMoveTypeHint(Node.MoveType.WALK),
                    MovementAction.offset(new PathVector(0, 0, 1)).withMoveTypeHint(Node.MoveType.WALK)
                );
            }
            if (position.equals(new PathPosition(0, 64, 1))) {
                return List.of(
                    MovementAction.offset(new PathVector(1, 0, -1)).withMoveTypeHint(Node.MoveType.FLY)
                );
            }
            if (position.equals(new PathPosition(1, 64, 0))) {
                double multiplier = currentState.arrivalMoveType() == Node.MoveType.FLY ? 1.0 : 100.0;
                return List.of(
                    MovementAction.offset(new PathVector(1, 0, 0))
                        .withMoveTypeHint(Node.MoveType.WALK)
                        .withCost(multiplier, 0.0)
                );
            }
            return List.of();
        }
    }
}
