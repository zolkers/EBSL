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

import fr.riege.ebsl.common.pathfinding.wrapper.PathPosition;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Small long-range navigation memory used by planners and repair policies.
 *
 * <p>This is the cache layer future hierarchical and incremental planners need: it records segment failures,
 * reusable corridor hints, and successful handoffs without coupling the pathfinder to a world
 * implementation.</p>
 */
public final class LongRangePathMemory {
    private static final int MAX_ENTRIES = 256;

    private final Map<SegmentKey, Integer> failures = new LinkedHashMap<>();
    private final Map<SegmentKey, LongRangePathPlan> successfulCorridors = new LinkedHashMap<>();

    public void clear() {
        failures.clear();
        successfulCorridors.clear();
    }

    public void recordFailure(int fromX, int fromZ, int toX, int toZ) {
        trim(failures);
        SegmentKey key = SegmentKey.of(fromX, fromZ, toX, toZ);
        failures.merge(key, 1, Integer::sum);
    }

    public void recordFailure(PathPosition from, int toX, int toZ) {
        recordFailure(from.flooredX(), from.flooredZ(), toX, toZ);
    }

    public int failureCount(int fromX, int fromZ, int toX, int toZ) {
        return failures.getOrDefault(SegmentKey.of(fromX, fromZ, toX, toZ), 0);
    }

    public double failurePenalty(int fromX, int fromZ, int toX, int toZ) {
        return failureCount(fromX, fromZ, toX, toZ) * 1000.0;
    }

    public void rememberCorridor(LongRangePathPlan plan) {
        if (plan == null || plan.positions().isEmpty()) {
            return;
        }
        trim(successfulCorridors);
        PathPosition start = plan.positions().getFirst();
        PathPosition end = plan.positions().getLast();
        successfulCorridors.put(SegmentKey.of(start.flooredX(), start.flooredZ(), end.flooredX(), end.flooredZ()), plan);
    }

    public LongRangePathPlan corridor(int fromX, int fromZ, int toX, int toZ) {
        return successfulCorridors.get(SegmentKey.of(fromX, fromZ, toX, toZ));
    }

    public int rememberedFailureEntries() {
        return failures.size();
    }

    public int rememberedCorridors() {
        return successfulCorridors.size();
    }

    private static <T> void trim(Map<SegmentKey, T> map) {
        while (map.size() >= MAX_ENTRIES) {
            SegmentKey first = map.keySet().iterator().next();
            map.remove(first);
        }
    }

    private record SegmentKey(int fromX, int fromZ, int toX, int toZ) {
        static SegmentKey of(int fromX, int fromZ, int toX, int toZ) {
            return new SegmentKey(fromX, fromZ, toX, toZ);
        }
    }
}
