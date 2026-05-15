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

package fr.riege.ebsl.common.pathfinding;

import fr.riege.ebsl.common.math.Vec3d;
import fr.riege.ebsl.common.pathfinding.wrapper.PathPosition;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LongRangePathSessionTest {
    @Test
    void plannerUsesRollingHorizonWhenGoalExceedsSegmentDistance() {
        LongRangePathSession session = new LongRangePathSession(testPolicy(), new DirectLongRangeSegmentPlanner());
        session.start(100, 0);

        LongRangePathSession.SegmentGoal segment = session.planSegmentGoal(0.5, 0.5);

        assertTrue(segment.segmented());
        assertEquals(50, segment.x());
        assertEquals(0, segment.z());
        assertEquals(LongRangePlanningStrategy.ROLLING_HORIZON, segment.planningStrategy());
        assertEquals(segment, session.diagnostics().lastPlannedSegment());
    }

    @Test
    void policyQueuesBeforeSegmentEndAndRecordsDecision() {
        LongRangePathSession session = new LongRangePathSession(testPolicy(), new DirectLongRangeSegmentPlanner());
        session.start(100, 0);
        session.onSegmentStarted(50, 0, true);

        LongRangePathSession.SegmentQueueDecision decision = session.queueDecision(
            0.60,
            80.0,
            false,
            1000L
        );

        assertEquals(LongRangePathSession.SegmentQueueDecision.NORMAL, decision);
        assertEquals(decision, session.diagnostics().lastQueueDecision());
        assertEquals("queue_decision", session.diagnostics().lastEvent());
    }

    @Test
    void failedSegmentsTriggerPlayerRecoveryAfterPolicyThreshold() {
        LongRangePathSession session = new LongRangePathSession(testPolicy(), new DirectLongRangeSegmentPlanner());
        session.start(100, 0);
        session.onSegmentStarted(50, 0, true);
        session.markSegmentCalculationStarted(null);
        session.markSegmentCalculationFailed(1000L);
        session.markSegmentCalculationStarted(null);
        session.markSegmentCalculationFailed(2000L);

        assertTrue(session.shouldUsePlayerRecoveryStart(0.80, false));
        assertEquals(2, session.diagnostics().failedSegmentCalculations());
        assertEquals("segment_calculation_failed", session.diagnostics().lastEvent());
    }

    @Test
    void finalGoalToleranceComesFromPolicy() {
        LongRangePathSession session = new LongRangePathSession(testPolicy(), new DirectLongRangeSegmentPlanner());
        session.startBlockGoal(10, 64, 10);

        assertTrue(session.isFinalGoalReached(new Vec3d(10.9, 65.0, 10.9)));
        assertFalse(session.isFinalGoalReached(new Vec3d(10.9, 69.0, 10.9)));
        assertFalse(session.isFinalGoalReached(new Vec3d(14.0, 65.0, 10.0)));
    }

    @Test
    void hierarchicalPlannerAvoidsRememberedFailedSegment() {
        LongRangePathMemory memory = new LongRangePathMemory();
        LongRangePathSession session = new LongRangePathSession(
            testPolicy(),
            new HierarchicalLongRangeSegmentPlanner(),
            new LongRangeSegmentAcceptancePolicy(),
            memory);
        session.start(100, 0);
        LongRangePathSession.SegmentGoal direct = new DirectLongRangeSegmentPlanner().plan(
            new LongRangeSegmentPlanner.SegmentRequest(0.5, 0.5, 100, 0, testPolicy(), memory));
        memory.recordFailure(0, 0, direct.x(), direct.z());

        LongRangePathSession.SegmentGoal segment = session.planSegmentGoal(0.5, 0.5);

        assertEquals(LongRangePlanningStrategy.HIERARCHICAL_WAYPOINT, segment.planningStrategy());
        assertNotEquals(direct.z(), segment.z());
    }

    @Test
    void acceptedPreparedSegmentsAreRememberedAsCorridors() {
        LongRangePathSession session = new LongRangePathSession(testPolicy(), new DirectLongRangeSegmentPlanner());
        session.start(100, 0);
        session.onSegmentStarted(50, 0, true);

        session.setPreparedSegment(new LongRangePathSession.PendingSegment(
            simpleNodes(Node.MoveType.WALK),
            2,
            64,
            0,
            true,
            false,
            5,
            LongRangePathSession.SegmentAttachment.MERGE_WITH_CURRENT,
            true,
            LongRangePathPlan.fromNodes(simpleNodes(Node.MoveType.WALK), false, LongRangePlanningStrategy.ROLLING_HORIZON)));

        assertTrue(session.hasPreparedSegment());
        assertEquals(1, session.diagnostics().rememberedCorridors());
        assertEquals("quality_ok", session.diagnostics().lastAcceptanceReason());
    }

    @Test
    void riskyPreparedSegmentsAreRejectedByAcceptancePolicy() {
        LongRangePathSession session = new LongRangePathSession(
            testPolicy(),
            new DirectLongRangeSegmentPlanner(),
            new LongRangeSegmentAcceptancePolicy(1.0, 2),
            new LongRangePathMemory());
        session.start(100, 0);
        session.onSegmentStarted(50, 0, true);

        List<Node> risky = simpleNodes(Node.MoveType.PARKOUR);
        session.setPreparedSegment(new LongRangePathSession.PendingSegment(
            risky,
            2,
            64,
            0,
            true,
            false,
            5,
            LongRangePathSession.SegmentAttachment.MERGE_WITH_CURRENT,
            true,
            LongRangePathPlan.fromNodes(risky, false, LongRangePlanningStrategy.ROLLING_HORIZON)));

        assertFalse(session.hasPreparedSegment());
        assertEquals("too_risky", session.diagnostics().lastAcceptanceReason());
        assertEquals("segment_calculation_failed", session.diagnostics().lastEvent());
    }

    @Test
    void failureCacheEvictsLowestScoredEntries() {
        LongRangePathMemory memory = new LongRangePathMemory(new LongRangeMemoryPolicy(
            2, 2, 0, 10.0, 100.0, 1.0, 1.0, 1.0));

        memory.recordFailure(0, 0, 10, 0);
        memory.recordFailure(0, 0, 10, 0);
        memory.recordFailure(0, 0, 20, 0);
        memory.recordFailure(0, 0, 30, 0);

        assertEquals(2, memory.rememberedFailureEntries());
        assertEquals(2, memory.failureCount(0, 0, 10, 0));
        assertEquals(0, memory.failureCount(0, 0, 20, 0));
    }

    @Test
    void corridorCacheKeepsReusedCorridorsDuringEviction() {
        LongRangePathMemory memory = new LongRangePathMemory(new LongRangeMemoryPolicy(
            2, 2, 0, 10.0, 1.0, 100.0, 1.0, 0.0));
        LongRangePathPlan first = LongRangePathPlan.fromNodes(simpleNodes(Node.MoveType.WALK), false,
            LongRangePlanningStrategy.ROLLING_HORIZON);
        LongRangePathPlan second = LongRangePathPlan.fromNodes(nodesTo(4, Node.MoveType.WALK), false,
            LongRangePlanningStrategy.ROLLING_HORIZON);
        LongRangePathPlan third = LongRangePathPlan.fromNodes(nodesTo(6, Node.MoveType.WALK), false,
            LongRangePlanningStrategy.ROLLING_HORIZON);

        memory.rememberCorridor(first);
        assertNotNull(memory.corridor(0, 0, 2, 0));
        memory.rememberCorridor(second);
        memory.rememberCorridor(third);

        assertEquals(2, memory.rememberedCorridors());
        assertNotNull(memory.corridor(0, 0, 2, 0));
        assertNull(memory.corridor(0, 0, 6, 0));
    }

    private static List<Node> simpleNodes(Node.MoveType moveType) {
        return nodesTo(2, moveType);
    }

    private static List<Node> nodesTo(int x, Node.MoveType moveType) {
        Node start = new Node(new PathPosition(0, 64, 0));
        Node end = new Node(new PathPosition(x, 64, 0));
        end.setParent(start);
        end.setMoveType(moveType);
        end.setGCost(x);
        return List.of(start, end);
    }

    private static LongRangeNavigationPolicy testPolicy() {
        return new LongRangeNavigationPolicy(
            50.0,
            0.50,
            0.70,
            45.0,
            14.0,
            24.0,
            2.0,
            1500,
            2,
            0.75,
            true,
            0.45,
            64.0,
            2
        );
    }
}
