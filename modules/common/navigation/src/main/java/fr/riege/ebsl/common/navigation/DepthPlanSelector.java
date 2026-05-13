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

package fr.riege.ebsl.common.navigation;

import fr.riege.ebsl.common.pathfinding.movement.MovementTerrain;

final class DepthPlanSelector {
    private static final double MIN_WORST_SEGMENT_IMPROVEMENT = 0.08;
    private static final double MIN_WEAK_AVERAGE_IMPROVEMENT = 0.05;
    private static final double MAX_LOCAL_TAKEOVER_SCORE_REGRESSION = 0.08;

    private DepthPlanSelector() {
    }

    static PathPlan select(PathPlan primary, PathPlan candidate, double requiredImprovement) {
        return select(primary, candidate, requiredImprovement, null);
    }

    static PathPlan select(PathPlan primary, PathPlan candidate, double requiredImprovement, MovementTerrain checker) {
        if (!isUsable(candidate)) {
            return primary;
        }
        if (!isUsable(primary)) {
            return candidate;
        }
        if (primary.complete() && !candidate.complete()) {
            return primary;
        }
        if (!primary.complete() && candidate.complete()) {
            return candidate;
        }
        PathPlanQualityProfile primaryProfile = PathPlanQualityProfiler.profile(primary, checker);
        PathPlanQualityProfile candidateProfile = PathPlanQualityProfiler.profile(candidate, checker);
        return shouldTakeOver(primaryProfile, candidateProfile, requiredImprovement) ? candidate : primary;
    }

    static boolean shouldReplace(PathPlan primary, PathPlan candidate, double requiredImprovement) {
        return shouldReplace(primary, candidate, requiredImprovement, null);
    }

    static boolean shouldReplace(PathPlan primary, PathPlan candidate, double requiredImprovement, MovementTerrain checker) {
        return select(primary, candidate, requiredImprovement, checker) == candidate;
    }

    private static boolean shouldTakeOver(PathPlanQualityProfile primary,
                                          PathPlanQualityProfile candidate,
                                          double requiredImprovement) {
        double clampedRequired = Math.clamp(requiredImprovement, 0.0, 1.0);
        double scoreImprovement = candidate.overallScore() - primary.overallScore();
        if (scoreImprovement >= clampedRequired) {
            return true;
        }
        double allowedRegression = Math.max(MAX_LOCAL_TAKEOVER_SCORE_REGRESSION, clampedRequired);
        if (candidate.overallScore() + allowedRegression < primary.overallScore()) {
            return false;
        }
        double worstImprovement = primary.worstWeakness() - candidate.worstWeakness();
        if (worstImprovement >= MIN_WORST_SEGMENT_IMPROVEMENT) {
            return true;
        }
        double weakAverageImprovement = primary.weakSegmentAverage() - candidate.weakSegmentAverage();
        return weakAverageImprovement >= MIN_WEAK_AVERAGE_IMPROVEMENT
            && scoreImprovement >= -allowedRegression * 0.5;
    }

    private static boolean isUsable(PathPlan plan) {
        return plan != null && plan.usable();
    }
}
