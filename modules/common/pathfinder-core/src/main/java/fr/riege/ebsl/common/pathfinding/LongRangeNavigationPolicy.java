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

import fr.riege.ebsl.common.pathfinding.settings.PathfinderSettings;

import java.util.Objects;

/**
 * Long-range navigation thresholds and decision rules.
 *
 * <p>Keeping these rules outside the session makes long-distance navigation testable and gives
 * future planners a stable place to plug in more advanced policies such as hierarchical routing,
 * incremental repair, and risk-aware segment switching.</p>
 */
public record LongRangeNavigationPolicy(
    double maxSegmentDistance,
    double earlySegmentRecalcRatio,
    double segmentRecalcRatio,
    double prepareRemainingDistance,
    double emergencyRemainingDistance,
    double preparedSwitchRemainingDistance,
    double finalGoalXzTolerance,
    int segmentRetryCooldownMs,
    int playerStartAfterFailures,
    double playerStartRecoveryRatio
) {
    public LongRangeNavigationPolicy {
        requirePositiveFinite(maxSegmentDistance, "maxSegmentDistance");
        requireRatio(earlySegmentRecalcRatio, "earlySegmentRecalcRatio");
        requireRatio(segmentRecalcRatio, "segmentRecalcRatio");
        requireNonNegativeFinite(prepareRemainingDistance, "prepareRemainingDistance");
        requireNonNegativeFinite(emergencyRemainingDistance, "emergencyRemainingDistance");
        requireNonNegativeFinite(preparedSwitchRemainingDistance, "preparedSwitchRemainingDistance");
        requirePositiveFinite(finalGoalXzTolerance, "finalGoalXzTolerance");
        if (segmentRetryCooldownMs < 0) {
            throw new IllegalArgumentException("segmentRetryCooldownMs must be non-negative");
        }
        if (playerStartAfterFailures < 0) {
            throw new IllegalArgumentException("playerStartAfterFailures must be non-negative");
        }
        requireRatio(playerStartRecoveryRatio, "playerStartRecoveryRatio");
    }

    public static LongRangeNavigationPolicy fromSettings() {
        PathfinderSettings settings = PathfinderSettings.instance();
        return new LongRangeNavigationPolicy(
            settings.maxSegmentDistance.value(),
            settings.earlySegmentRecalcRatio.value(),
            settings.segmentRecalcRatio.value(),
            settings.prepareRemainingDistance.value(),
            settings.emergencyRemainingDistance.value(),
            settings.preparedSwitchRemainingDistance.value(),
            settings.finalGoalXzTolerance.value(),
            settings.segmentRetryCooldownMs.value(),
            settings.playerStartAfterFailures.value(),
            settings.playerStartRecoveryRatio.value()
        );
    }

    public LongRangePathSession.SegmentQueueDecision queueDecision(QueueInput input) {
        Objects.requireNonNull(input, "input");
        if (!input.active()
            || !input.currentSegmentNeedsContinuation()
            || input.segmentCalculationInFlight()
            || input.hasPreparedSegment()
            || !input.retryReady()) {
            return LongRangePathSession.SegmentQueueDecision.NONE;
        }
        if (input.immediateSegmentQueueRequested()) {
            return LongRangePathSession.SegmentQueueDecision.NORMAL;
        }
        if (input.walkExecutionDone()) {
            return LongRangePathSession.SegmentQueueDecision.EMERGENCY_FROM_PLAYER;
        }
        if (input.remainingDistance() <= emergencyRemainingDistance) {
            return LongRangePathSession.SegmentQueueDecision.NORMAL;
        }
        if (input.progressRatio() >= earlySegmentRecalcRatio
            || input.remainingDistance() <= prepareRemainingDistance) {
            return LongRangePathSession.SegmentQueueDecision.NORMAL;
        }
        return LongRangePathSession.SegmentQueueDecision.NONE;
    }

    public boolean shouldActivatePreparedSegment(double progressRatio, double remainingDistance,
                                                 boolean walkExecutionDone) {
        return walkExecutionDone
            || progressRatio >= segmentRecalcRatio
            || remainingDistance <= preparedSwitchRemainingDistance;
    }

    public boolean shouldUsePlayerRecoveryStart(int failedSegmentCalculations, double progressRatio,
                                                boolean walkExecutionDone) {
        return walkExecutionDone
            || (failedSegmentCalculations >= playerStartAfterFailures
            && progressRatio >= playerStartRecoveryRatio);
    }

    private static void requirePositiveFinite(double value, String name) {
        if (!Double.isFinite(value) || value <= 0.0) {
            throw new IllegalArgumentException(name + " must be finite and positive");
        }
    }

    private static void requireNonNegativeFinite(double value, String name) {
        if (!Double.isFinite(value) || value < 0.0) {
            throw new IllegalArgumentException(name + " must be finite and non-negative");
        }
    }

    private static void requireRatio(double value, String name) {
        if (!Double.isFinite(value) || value < 0.0 || value > 1.0) {
            throw new IllegalArgumentException(name + " must be a finite ratio between 0 and 1");
        }
    }

    public record QueueInput(
        boolean active,
        boolean currentSegmentNeedsContinuation,
        boolean segmentCalculationInFlight,
        boolean hasPreparedSegment,
        boolean retryReady,
        boolean immediateSegmentQueueRequested,
        boolean walkExecutionDone,
        double progressRatio,
        double remainingDistance
    ) {
        public QueueInput {
            requireRatio(progressRatio, "progressRatio");
            requireNonNegativeFinite(remainingDistance, "remainingDistance");
        }
    }
}
