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
 * Chooses the next long-range segment target.
 *
 * <p>This interface is intentionally small: the default implementation keeps the current direct
 * rolling-horizon behavior, while advanced implementations can later use chunk graphs,
 * hierarchical A*, landmarks, or cached corridors without changing session orchestration.</p>
 */
public interface LongRangeSegmentPlanner {
    LongRangePathSession.SegmentGoal plan(SegmentRequest request);

    record SegmentRequest(
        double fromX,
        double fromZ,
        int finalGoalX,
        int finalGoalZ,
        LongRangeNavigationPolicy policy,
        LongRangePathMemory memory
    ) {
        public SegmentRequest {
            if (!Double.isFinite(fromX) || !Double.isFinite(fromZ)) {
                throw new IllegalArgumentException("segment start coordinates must be finite");
            }
            if (policy == null) {
                throw new IllegalArgumentException("policy cannot be null");
            }
        }
    }
}
