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
 * Coarse long-range planner inspired by hierarchical A*.
 *
 * <p>Without a loaded world graph this planner cannot evaluate chunk portals yet, but it already
 * makes the routing decision at a higher level than a single direct vector: it scores direct and
 * lateral coarse waypoints, penalizes remembered failures, and falls back to the direct planner
 * when no detour is justified.</p>
 */
public final class HierarchicalLongRangeSegmentPlanner implements LongRangeSegmentPlanner {
    private final LongRangeSegmentPlanner fallback;

    public HierarchicalLongRangeSegmentPlanner() {
        this(new DirectLongRangeSegmentPlanner());
    }

    public HierarchicalLongRangeSegmentPlanner(LongRangeSegmentPlanner fallback) {
        this.fallback = fallback;
    }

    @Override
    public LongRangePathSession.SegmentGoal plan(SegmentRequest request) {
        LongRangePathSession.SegmentGoal direct = fallback.plan(request);
        if (!direct.segmented() || request.memory() == null) {
            return direct;
        }

        Candidate best = Candidate.of(direct.x(), direct.z(), direct.distanceToFinalGoal(),
            LongRangePlanningStrategy.ROLLING_HORIZON, request);
        double dx = request.finalGoalX() + 0.5 - request.fromX();
        double dz = request.finalGoalZ() + 0.5 - request.fromZ();
        double length = Math.max(Math.sqrt(dx * dx + dz * dz), 1.0e-6);
        double perpendicularX = -dz / length;
        double perpendicularZ = dx / length;
        double lateralDistance = request.policy().maxSegmentDistance() * 0.45;

        for (int side : new int[] {-1, 1}) {
            int x = (int) Math.floor(direct.x() + perpendicularX * lateralDistance * side);
            int z = (int) Math.floor(direct.z() + perpendicularZ * lateralDistance * side);
            Candidate candidate = Candidate.of(x, z, direct.distanceToFinalGoal(),
                LongRangePlanningStrategy.HIERARCHICAL_WAYPOINT, request);
            if (candidate.score < best.score) {
                best = candidate;
            }
        }

        return new LongRangePathSession.SegmentGoal(
            best.x,
            best.z,
            true,
            direct.distanceToFinalGoal(),
            best.strategy
        );
    }

    private record Candidate(int x, int z, double score, LongRangePlanningStrategy strategy) {
        static Candidate of(int x, int z, double remainingToFinal, LongRangePlanningStrategy strategy,
                            SegmentRequest request) {
            int fromX = (int) Math.floor(request.fromX());
            int fromZ = (int) Math.floor(request.fromZ());
            double dx = request.finalGoalX() + 0.5 - (x + 0.5);
            double dz = request.finalGoalZ() + 0.5 - (z + 0.5);
            double remaining = Math.sqrt(dx * dx + dz * dz);
            double detourPenalty = strategy == LongRangePlanningStrategy.HIERARCHICAL_WAYPOINT
                ? remainingToFinal * 0.035
                : 0.0;
            double failurePenalty = request.memory().failurePenalty(fromX, fromZ, x, z);
            return new Candidate(x, z, remaining + detourPenalty + failurePenalty, strategy);
        }
    }
}
