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
import fr.riege.ebsl.common.pathfinding.pathing.InspectablePathfinder;

import java.util.List;
import java.util.Objects;

public final class LongRangePathSession {
    private final LongRangeNavigationPolicy policy;
    private final LongRangeSegmentPlanner segmentPlanner;

    private boolean active;
    private int finalGoalX;
    private int finalGoalY;
    private int finalGoalZ;
    private GoalContract goalContract = GoalContract.XZ;
    private int currentSegmentGoalX;
    private int currentSegmentGoalY;
    private int currentSegmentGoalZ;
    private boolean currentSegmentNeedsContinuation;
    private int currentSegmentId;
    private int calculationSegmentId = -1;
    private boolean segmentCalculationInFlight;
    private InspectablePathfinder backgroundPathfinder;
    private PendingSegment preparedSegment;
    private long nextRetryAfterMs;
    private int failedSegmentCalculations;
    private boolean immediateSegmentQueueRequested;
    private LongRangeNavigationDiagnostics diagnostics = LongRangeNavigationDiagnostics.empty();

    public LongRangePathSession() {
        this(LongRangeNavigationPolicy.fromSettings(), new DirectLongRangeSegmentPlanner());
    }

    public LongRangePathSession(LongRangeNavigationPolicy policy, LongRangeSegmentPlanner segmentPlanner) {
        this.policy = Objects.requireNonNull(policy, "policy");
        this.segmentPlanner = Objects.requireNonNull(segmentPlanner, "segmentPlanner");
    }

    public void start(int finalGoalX, int finalGoalZ) {
        start(finalGoalX, 0, finalGoalZ, GoalContract.XZ);
    }

    public void startBlockGoal(int finalGoalX, int finalGoalY, int finalGoalZ) {
        start(finalGoalX, finalGoalY, finalGoalZ, GoalContract.XYZ);
    }

    private void start(int finalGoalX, int finalGoalY, int finalGoalZ, GoalContract goalContract) {
        clear();
        active = true;
        this.finalGoalX = finalGoalX;
        this.finalGoalY = finalGoalY;
        this.finalGoalZ = finalGoalZ;
        this.goalContract = goalContract;
    }

    public void clear() {
        active = false;
        finalGoalY = 0;
        goalContract = GoalContract.XZ;
        currentSegmentGoalX = 0;
        currentSegmentGoalY = 0;
        currentSegmentGoalZ = 0;
        currentSegmentNeedsContinuation = false;
        currentSegmentId = 0;
        calculationSegmentId = -1;
        segmentCalculationInFlight = false;
        preparedSegment = null;
        nextRetryAfterMs = 0;
        failedSegmentCalculations = 0;
        immediateSegmentQueueRequested = false;
        if (backgroundPathfinder != null) {
            backgroundPathfinder.abort();
            backgroundPathfinder = null;
        }
        updateDiagnostics("cleared");
    }

    public boolean isActive() {
        return active;
    }

    public boolean shouldKeepNavigationAlive() {
        return active && (currentSegmentNeedsContinuation || segmentCalculationInFlight || preparedSegment != null);
    }

    public int finalGoalX() { return finalGoalX; }
    public int finalGoalY() { return finalGoalY; }
    public int finalGoalZ() { return finalGoalZ; }
    public boolean requiresExactY() { return goalContract == GoalContract.XYZ; }
    public int currentSegmentGoalX() { return currentSegmentGoalX; }
    public int currentSegmentGoalY() { return currentSegmentGoalY; }
    public int currentSegmentGoalZ() { return currentSegmentGoalZ; }

    public SegmentGoal planSegmentGoal(double fromX, double fromZ) {
        SegmentGoal segmentGoal = segmentPlanner.plan(new LongRangeSegmentPlanner.SegmentRequest(
            fromX, fromZ, finalGoalX, finalGoalZ, policy));
        diagnostics = diagnostics.withPlannedSegment(segmentGoal).withEvent("planned_segment");
        return segmentGoal;
    }

    public void onSegmentStarted(int goalX, int goalZ, boolean needsContinuation) {
        onSegmentStarted(goalX, 0, goalZ, needsContinuation, false);
    }

    public void onSegmentStarted(int goalX, int goalZ, boolean needsContinuation, boolean partial) {
        onSegmentStarted(goalX, 0, goalZ, needsContinuation, partial);
    }

    public void onSegmentStarted(int goalX, int goalY, int goalZ, boolean needsContinuation, boolean partial) {
        currentSegmentGoalX = goalX;
        currentSegmentGoalY = goalY;
        currentSegmentGoalZ = goalZ;
        currentSegmentNeedsContinuation = needsContinuation;
        currentSegmentId++;
        calculationSegmentId = -1;
        preparedSegment = null;
        nextRetryAfterMs = 0;
        failedSegmentCalculations = 0;
        immediateSegmentQueueRequested = partial;
        updateDiagnostics(partial ? "partial_segment_started" : "segment_started");
    }

    public void markCompleted() {
        active = false;
        currentSegmentNeedsContinuation = false;
        calculationSegmentId = -1;
        segmentCalculationInFlight = false;
        preparedSegment = null;
        immediateSegmentQueueRequested = false;
        if (backgroundPathfinder != null) {
            backgroundPathfinder.abort();
            backgroundPathfinder = null;
        }
        updateDiagnostics("completed");
    }

    public SegmentQueueDecision queueDecision(double progressRatio, double remainingDistance, boolean walkExecutionDone, long now) {
        SegmentQueueDecision decision = policy.queueDecision(new LongRangeNavigationPolicy.QueueInput(
            active,
            currentSegmentNeedsContinuation,
            segmentCalculationInFlight,
            preparedSegment != null,
            now >= nextRetryAfterMs,
            immediateSegmentQueueRequested,
            walkExecutionDone,
            progressRatio,
            remainingDistance
        ));
        if (immediateSegmentQueueRequested) {
            immediateSegmentQueueRequested = false;
        }
        diagnostics = diagnostics.withQueueDecision(decision).withEvent("queue_decision");
        return decision;
    }

    public boolean shouldActivatePreparedSegment(double progressRatio, double remainingDistance, boolean walkExecutionDone) {
        return policy.shouldActivatePreparedSegment(progressRatio, remainingDistance, walkExecutionDone);
    }

    public boolean shouldUsePlayerRecoveryStart(double progressRatio, boolean walkExecutionDone) {
        return policy.shouldUsePlayerRecoveryStart(failedSegmentCalculations, progressRatio, walkExecutionDone);
    }

    public int markSegmentCalculationStarted(InspectablePathfinder pathfinder) {
        segmentCalculationInFlight = true;
        calculationSegmentId = currentSegmentId;
        backgroundPathfinder = pathfinder;
        updateDiagnostics("segment_calculation_started");
        return calculationSegmentId;
    }

    public void setPreparedSegment(PendingSegment preparedSegment) {
        this.preparedSegment = preparedSegment;
        this.segmentCalculationInFlight = false;
        this.backgroundPathfinder = null;
        this.nextRetryAfterMs = 0;
        this.failedSegmentCalculations = 0;
        updateDiagnostics("prepared_segment_ready");
    }

    public void markSegmentCalculationFailed(long now) {
        segmentCalculationInFlight = false;
        calculationSegmentId = -1;
        backgroundPathfinder = null;
        preparedSegment = null;
        nextRetryAfterMs = now + policy.segmentRetryCooldownMs();
        failedSegmentCalculations++;
        updateDiagnostics("segment_calculation_failed");
    }

    public void forceNextCalculationFromPlayer() {
        if (backgroundPathfinder != null) {
            backgroundPathfinder.abort();
        }
        segmentCalculationInFlight = false;
        calculationSegmentId = -1;
        backgroundPathfinder = null;
        preparedSegment = null;
        nextRetryAfterMs = 0;
        failedSegmentCalculations = policy.playerStartAfterFailures();
        immediateSegmentQueueRequested = true;
        updateDiagnostics("forced_player_recovery");
    }

    public boolean isCurrentCalculation(int segmentId) {
        return segmentCalculationInFlight && calculationSegmentId == segmentId;
    }

    public boolean hasPreparedSegment() { return preparedSegment != null; }
    public boolean hasSegmentCalculationInFlight() { return segmentCalculationInFlight; }
    public PendingSegment preparedSegment() { return preparedSegment; }
    public LongRangeNavigationDiagnostics diagnostics() { return diagnostics; }

    public boolean isFinalGoalReached(Vec3d playerPos) {
        double dx = (finalGoalX + 0.5) - playerPos.x();
        double dz = (finalGoalZ + 0.5) - playerPos.z();
        if (Math.sqrt(dx * dx + dz * dz) > policy.finalGoalXzTolerance()) {
            return false;
        }
        return !requiresExactY() || Math.abs(finalGoalY - playerPos.y()) <= 2.0;
    }

    public boolean isFinalSegmentGoal(int x, int y, int z) {
        return x == finalGoalX && z == finalGoalZ && (!requiresExactY() || y == finalGoalY);
    }

    private void updateDiagnostics(String event) {
        diagnostics = diagnostics
            .withEvent(event)
            .withSessionState(
                failedSegmentCalculations,
                segmentCalculationInFlight,
                preparedSegment != null,
                nextRetryAfterMs);
    }

    public record SegmentGoal(
        int x,
        int z,
        boolean segmented,
        double distanceToFinalGoal,
        DirectLongRangeSegmentPlanner.PlanningStrategy planningStrategy
    ) {
        public SegmentGoal(int x, int z, boolean segmented) {
            this(x, z, segmented, 0.0, DirectLongRangeSegmentPlanner.PlanningStrategy.DIRECT_TO_GOAL);
        }
    }

    public enum SegmentAttachment {
        MERGE_WITH_CURRENT,
        REPLACE_FROM_PLAYER
    }

    public enum SegmentQueueDecision {
        NONE,
        NORMAL,
        EMERGENCY_FROM_PLAYER
    }

    public enum GoalContract {
        XZ,
        XYZ
    }

    public record PendingSegment(
        List<Node> path,
        int goalX,
        int goalY,
        int goalZ,
        boolean needsContinuation,
        boolean partial,
        long exploredCount,
        SegmentAttachment attachment,
        boolean rollingHorizon
    ) {
    }
}
