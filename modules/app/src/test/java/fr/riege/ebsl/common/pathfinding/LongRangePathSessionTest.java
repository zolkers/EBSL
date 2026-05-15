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
import org.junit.jupiter.api.Test;

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
        assertEquals(DirectLongRangeSegmentPlanner.PlanningStrategy.ROLLING_HORIZON, segment.planningStrategy());
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
            0.75
        );
    }
}
