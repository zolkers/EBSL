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

public record LongRangeMemoryPolicy(
    int maxFailureEntries,
    int maxCorridorEntries,
    long maxEntryAgeEvents,
    double failurePenalty,
    double failureRetentionWeight,
    double corridorHitWeight,
    double corridorQualityWeight,
    double corridorRecencyWeight
) {
    public LongRangeMemoryPolicy {
        if (maxFailureEntries < 0 || maxCorridorEntries < 0) {
            throw new IllegalArgumentException("cache capacities must be non-negative");
        }
        if (maxEntryAgeEvents < 0L) {
            throw new IllegalArgumentException("maxEntryAgeEvents must be non-negative");
        }
        requireNonNegativeFinite(failurePenalty, "failurePenalty");
        requireNonNegativeFinite(failureRetentionWeight, "failureRetentionWeight");
        requireNonNegativeFinite(corridorHitWeight, "corridorHitWeight");
        requireNonNegativeFinite(corridorQualityWeight, "corridorQualityWeight");
        requireNonNegativeFinite(corridorRecencyWeight, "corridorRecencyWeight");
    }

    public static LongRangeMemoryPolicy fromSettings() {
        PathfinderSettings settings = PathfinderSettings.instance();
        return new LongRangeMemoryPolicy(
            settings.longRangeFailureCacheEntries.value(),
            settings.longRangeCorridorCacheEntries.value(),
            settings.longRangeMemoryMaxAgeEvents.value(),
            settings.longRangeFailurePenalty.value(),
            settings.longRangeFailureRetentionWeight.value(),
            settings.longRangeCorridorHitWeight.value(),
            settings.longRangeCorridorQualityWeight.value(),
            settings.longRangeCorridorRecencyWeight.value()
        );
    }

    private static void requireNonNegativeFinite(double value, String name) {
        if (!Double.isFinite(value) || value < 0.0) {
            throw new IllegalArgumentException(name + " must be finite and non-negative");
        }
    }
}
