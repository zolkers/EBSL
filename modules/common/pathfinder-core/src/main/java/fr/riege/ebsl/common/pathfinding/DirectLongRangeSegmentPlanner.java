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
 * Rolling-horizon segment planner using the direct vector toward the final goal.
 *
 * <p>This is not the final word on long-distance routing; it is the stable baseline planner that
 * preserves existing behavior while giving the navigation stack a replaceable planning boundary.</p>
 */
public final class DirectLongRangeSegmentPlanner implements LongRangeSegmentPlanner {
    @Override
    public LongRangePathSession.SegmentGoal plan(SegmentRequest request) {
        double dx = request.finalGoalX() + 0.5 - request.fromX();
        double dz = request.finalGoalZ() + 0.5 - request.fromZ();
        double maxSegmentDistance = request.policy().maxSegmentDistance();
        double distanceSquared = dx * dx + dz * dz;
        if (distanceSquared <= maxSegmentDistance * maxSegmentDistance) {
            return new LongRangePathSession.SegmentGoal(
                request.finalGoalX(),
                request.finalGoalZ(),
                false,
                Math.sqrt(distanceSquared),
                LongRangePlanningStrategy.DIRECT_TO_GOAL
            );
        }

        double distance = Math.sqrt(distanceSquared);
        double scale = maxSegmentDistance / Math.max(distance, 1.0e-6);
        int segmentX = (int) Math.floor(request.fromX() + dx * scale);
        int segmentZ = (int) Math.floor(request.fromZ() + dz * scale);
        if (segmentX == request.finalGoalX() && segmentZ == request.finalGoalZ()) {
            return new LongRangePathSession.SegmentGoal(
                request.finalGoalX(),
                request.finalGoalZ(),
                false,
                distance,
                LongRangePlanningStrategy.DIRECT_TO_GOAL
            );
        }
        return new LongRangePathSession.SegmentGoal(
            segmentX,
            segmentZ,
            true,
            distance,
            LongRangePlanningStrategy.ROLLING_HORIZON
        );
    }
}
