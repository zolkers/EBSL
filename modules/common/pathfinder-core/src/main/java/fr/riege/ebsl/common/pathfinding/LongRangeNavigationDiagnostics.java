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

/**
 * Compact state snapshot for long-range navigation debugging.
 */
public record LongRangeNavigationDiagnostics(
    String lastEvent,
    LongRangePathSession.SegmentGoal lastPlannedSegment,
    LongRangePathSession.SegmentQueueDecision lastQueueDecision,
    int failedSegmentCalculations,
    boolean segmentCalculationInFlight,
    boolean hasPreparedSegment,
    long nextRetryAfterMs,
    int rememberedFailures,
    int rememberedCorridors,
    String lastAcceptanceReason
) {
    public static LongRangeNavigationDiagnostics empty() {
        return new LongRangeNavigationDiagnostics(
            "idle",
            null,
            LongRangePathSession.SegmentQueueDecision.NONE,
            0,
            false,
            false,
            0L,
            0,
            0,
            "none"
        );
    }

    LongRangeNavigationDiagnostics withEvent(String event) {
        return new LongRangeNavigationDiagnostics(
            event,
            lastPlannedSegment,
            lastQueueDecision,
            failedSegmentCalculations,
            segmentCalculationInFlight,
            hasPreparedSegment,
            nextRetryAfterMs,
            rememberedFailures,
            rememberedCorridors,
            lastAcceptanceReason
        );
    }

    LongRangeNavigationDiagnostics withPlannedSegment(LongRangePathSession.SegmentGoal segment) {
        return new LongRangeNavigationDiagnostics(
            lastEvent,
            segment,
            lastQueueDecision,
            failedSegmentCalculations,
            segmentCalculationInFlight,
            hasPreparedSegment,
            nextRetryAfterMs,
            rememberedFailures,
            rememberedCorridors,
            lastAcceptanceReason
        );
    }

    LongRangeNavigationDiagnostics withQueueDecision(LongRangePathSession.SegmentQueueDecision decision) {
        return new LongRangeNavigationDiagnostics(
            lastEvent,
            lastPlannedSegment,
            decision,
            failedSegmentCalculations,
            segmentCalculationInFlight,
            hasPreparedSegment,
            nextRetryAfterMs,
            rememberedFailures,
            rememberedCorridors,
            lastAcceptanceReason
        );
    }

    LongRangeNavigationDiagnostics withSessionState(int failures, boolean inFlight,
                                                    boolean prepared, long retryAfterMs,
                                                    LongRangePathMemory memory) {
        return new LongRangeNavigationDiagnostics(
            lastEvent,
            lastPlannedSegment,
            lastQueueDecision,
            failures,
            inFlight,
            prepared,
            retryAfterMs,
            memory == null ? rememberedFailures : memory.rememberedFailureEntries(),
            memory == null ? rememberedCorridors : memory.rememberedCorridors(),
            lastAcceptanceReason
        );
    }

    LongRangeNavigationDiagnostics withAcceptanceReason(String reason) {
        return new LongRangeNavigationDiagnostics(
            lastEvent,
            lastPlannedSegment,
            lastQueueDecision,
            failedSegmentCalculations,
            segmentCalculationInFlight,
            hasPreparedSegment,
            nextRetryAfterMs,
            rememberedFailures,
            rememberedCorridors,
            reason == null ? "none" : reason
        );
    }
}
