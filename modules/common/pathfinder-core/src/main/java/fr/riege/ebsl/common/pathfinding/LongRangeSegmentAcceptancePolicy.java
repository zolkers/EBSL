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

import java.util.Objects;

/**
 * Accepts or rejects prepared long-range segments using semantic plan quality.
 */
public final class LongRangeSegmentAcceptancePolicy {
    private final double maxRiskScore;
    private final int minPositions;

    public LongRangeSegmentAcceptancePolicy() {
        this(64.0, 2);
    }

    public LongRangeSegmentAcceptancePolicy(double maxRiskScore, int minPositions) {
        if (!Double.isFinite(maxRiskScore) || maxRiskScore < 0.0) {
            throw new IllegalArgumentException("maxRiskScore must be finite and non-negative");
        }
        if (minPositions < 1) {
            throw new IllegalArgumentException("minPositions must be positive");
        }
        this.maxRiskScore = maxRiskScore;
        this.minPositions = minPositions;
    }

    public SegmentAcceptanceDecision evaluate(LongRangePathPlan candidate) {
        Objects.requireNonNull(candidate, "candidate");
        if (candidate.positions().size() < minPositions) {
            return SegmentAcceptanceDecision.reject("too_short");
        }
        if (candidate.riskScore() > maxRiskScore) {
            return SegmentAcceptanceDecision.reject("too_risky");
        }
        if (!Double.isFinite(candidate.qualityScore())) {
            return SegmentAcceptanceDecision.reject("invalid_quality");
        }
        return SegmentAcceptanceDecision.accept("quality_ok");
    }

    public record SegmentAcceptanceDecision(boolean accepted, String reason) {
        static SegmentAcceptanceDecision accept(String reason) {
            return new SegmentAcceptanceDecision(true, reason);
        }

        static SegmentAcceptanceDecision reject(String reason) {
            return new SegmentAcceptanceDecision(false, reason);
        }
    }
}
