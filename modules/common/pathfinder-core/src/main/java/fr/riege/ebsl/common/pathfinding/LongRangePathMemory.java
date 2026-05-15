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
import java.util.Objects;

/**
 * Small long-range navigation memory used by planners and repair policies.
 *
 * <p>This is the cache layer future hierarchical and incremental planners need: it records segment failures,
 * reusable corridor hints, and successful handoffs without coupling the pathfinder to a world
 * implementation.</p>
 */
public final class LongRangePathMemory {
    private final LongRangeMemoryPolicy policy;

    private final Map<SegmentKey, FailureEntry> failures = new LinkedHashMap<>();
    private final Map<SegmentKey, CorridorEntry> successfulCorridors = new LinkedHashMap<>();
    private long eventCounter;

    public LongRangePathMemory() {
        this(LongRangeMemoryPolicy.fromSettings());
    }

    public LongRangePathMemory(LongRangeMemoryPolicy policy) {
        this.policy = Objects.requireNonNull(policy, "policy");
    }

    public void clear() {
        failures.clear();
        successfulCorridors.clear();
        eventCounter = 0L;
    }

    public void recordFailure(int fromX, int fromZ, int toX, int toZ) {
        long event = nextEvent();
        SegmentKey key = SegmentKey.of(fromX, fromZ, toX, toZ);
        failures.compute(key, (ignored, entry) -> entry == null
            ? new FailureEntry(1, event, event)
            : entry.recordedAgain(event));
        evictFailures();
    }

    public void recordFailure(PathPosition from, int toX, int toZ) {
        recordFailure(from.flooredX(), from.flooredZ(), toX, toZ);
    }

    public int failureCount(int fromX, int fromZ, int toX, int toZ) {
        FailureEntry entry = failures.get(SegmentKey.of(fromX, fromZ, toX, toZ));
        return entry == null ? 0 : entry.count();
    }

    public double failurePenalty(int fromX, int fromZ, int toX, int toZ) {
        return failureCount(fromX, fromZ, toX, toZ) * policy.failurePenalty();
    }

    public void rememberCorridor(LongRangePathPlan plan) {
        if (plan == null || plan.positions().isEmpty()) {
            return;
        }
        long event = nextEvent();
        PathPosition start = plan.positions().getFirst();
        PathPosition end = plan.positions().getLast();
        successfulCorridors.put(
            SegmentKey.of(start.flooredX(), start.flooredZ(), end.flooredX(), end.flooredZ()),
            new CorridorEntry(plan, 0, event, event));
        evictCorridors();
    }

    public LongRangePathPlan corridor(int fromX, int fromZ, int toX, int toZ) {
        SegmentKey key = SegmentKey.of(fromX, fromZ, toX, toZ);
        CorridorEntry entry = successfulCorridors.get(key);
        if (entry == null) {
            return null;
        }
        long event = nextEvent();
        successfulCorridors.put(key, entry.hit(event));
        return entry.plan();
    }

    public int rememberedFailureEntries() {
        return failures.size();
    }

    public int rememberedCorridors() {
        return successfulCorridors.size();
    }

    private long nextEvent() {
        eventCounter++;
        return eventCounter;
    }

    private void evictFailures() {
        failures.entrySet().removeIf(entry -> isExpired(entry.getValue().lastSeenEvent()));
        while (failures.size() > policy.maxFailureEntries()) {
            removeLowestFailureScore();
        }
    }

    private void evictCorridors() {
        successfulCorridors.entrySet().removeIf(entry -> isExpired(entry.getValue().lastSeenEvent()));
        while (successfulCorridors.size() > policy.maxCorridorEntries()) {
            removeLowestCorridorScore();
        }
    }

    private boolean isExpired(long lastSeenEvent) {
        return policy.maxEntryAgeEvents() > 0L && eventCounter - lastSeenEvent > policy.maxEntryAgeEvents();
    }

    private void removeLowestFailureScore() {
        SegmentKey victim = null;
        double victimScore = Double.POSITIVE_INFINITY;
        for (Map.Entry<SegmentKey, FailureEntry> entry : failures.entrySet()) {
            double age = Math.max(0L, eventCounter - entry.getValue().lastSeenEvent());
            double score = entry.getValue().count() * policy.failureRetentionWeight() - age;
            if (score < victimScore) {
                victim = entry.getKey();
                victimScore = score;
            }
        }
        if (victim != null) {
            failures.remove(victim);
        }
    }

    private void removeLowestCorridorScore() {
        SegmentKey victim = null;
        double victimScore = Double.POSITIVE_INFINITY;
        for (Map.Entry<SegmentKey, CorridorEntry> entry : successfulCorridors.entrySet()) {
            CorridorEntry corridor = entry.getValue();
            double age = Math.max(0L, eventCounter - corridor.lastSeenEvent());
            double quality = 1.0 / Math.max(1.0, corridor.plan().qualityScore());
            double score = corridor.hits() * policy.corridorHitWeight()
                + quality * policy.corridorQualityWeight()
                - age * policy.corridorRecencyWeight();
            if (score < victimScore) {
                victim = entry.getKey();
                victimScore = score;
            }
        }
        if (victim != null) {
            successfulCorridors.remove(victim);
        }
    }

    private record FailureEntry(int count, long firstSeenEvent, long lastSeenEvent) {
        FailureEntry recordedAgain(long event) {
            return new FailureEntry(count + 1, firstSeenEvent, event);
        }
    }

    private record CorridorEntry(LongRangePathPlan plan, int hits, long createdEvent, long lastSeenEvent) {
        CorridorEntry hit(long event) {
            return new CorridorEntry(plan, hits + 1, createdEvent, event);
        }
    }

    private record SegmentKey(int fromX, int fromZ, int toX, int toZ) {
        static SegmentKey of(int fromX, int fromZ, int toX, int toZ) {
            return new SegmentKey(fromX, fromZ, toX, toZ);
        }
    }
}
