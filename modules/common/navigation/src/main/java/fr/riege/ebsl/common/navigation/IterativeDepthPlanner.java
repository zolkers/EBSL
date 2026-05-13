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

import fr.riege.ebsl.common.pathfinding.quality.PathQualityReport;
import fr.riege.ebsl.common.pathfinding.settings.PathfinderSettings;

final class IterativeDepthPlanner {
    private IterativeDepthPlanner() {
    }

    static IterativeDepthProfile profile(PathPlannerOptions options, int depth) {
        int normalizedDepth = Math.max(1, depth);
        double iterationScale = Math.pow(options.iterativeDepthIterationMultiplier(), normalizedDepth - 1);
        double timeScale = Math.pow(options.iterativeDepthTimeMultiplier(), normalizedDepth - 1);
        double qualityScale = Math.pow(options.iterativeDepthQualityMultiplier(), normalizedDepth - 1);
        return new IterativeDepthProfile(
            normalizedDepth,
            scale(options.maxIterations(), iterationScale),
            scale(options.maxLength(), Math.sqrt(iterationScale)),
            options.maxCalculationTimeMs() <= 0 ? 0 : scale(options.maxCalculationTimeMs(), timeScale),
            qualityScale);
    }

    static IterativeDepthProfile instantProfile(PathfinderSettings settings, int depth) {
        int normalizedDepth = Math.max(1, depth);
        double iterationScale = Math.pow(settings.iterativeDepthIterationMultiplier.value(), normalizedDepth - 1);
        double timeScale = Math.pow(settings.iterativeDepthTimeMultiplier.value(), normalizedDepth - 1);
        double qualityScale = Math.pow(settings.iterativeDepthQualityMultiplier.value(), normalizedDepth - 1);
        return new IterativeDepthProfile(
            normalizedDepth,
            scale(settings.instantWalkMaxIterations.value(), iterationScale),
            scale(settings.instantWalkMaxLength.value(), Math.sqrt(iterationScale)),
            scale(settings.instantCalculationTimeMs.value(), timeScale),
            qualityScale);
    }

    static PathPlannerOptions optionsForDepth(PathPlannerOptions options, int depth) {
        if (depth <= 1) {
            return options;
        }
        IterativeDepthProfile profile = profile(options, depth);
        return options.toBuilder()
            .qualityRiskCostWeight(options.qualityRiskCostWeight() * profile.qualityScale())
            .qualityTerrainCostWeight(options.qualityTerrainCostWeight() * profile.qualityScale())
            .maxIterations(profile.maxIterations())
            .maxLength(profile.maxLength())
            .maxCalculationTimeMs(profile.maxCalculationTimeMs())
            .build();
    }

    static boolean shouldContinue(PathPlan plan, PathPlannerOptions options, int depth) {
        if (!options.iterativeDepthEnabled() || depth >= options.iterativeDepthMax()) {
            return false;
        }
        if (plan == null || !plan.usable()) {
            return true;
        }
        return true;
    }

    static boolean shouldContinue(PathQualityReport quality, PathfinderSettings settings, int depth) {
        return settings.iterativeDepthEnabled.value()
            && depth < settings.iterativeDepthMax.value();
    }

    static DepthSearchMode modeForDepth(int depth, PathPlan activePlan) {
        if (depth > 2 && activePlan != null && activePlan.usable() && depth % 2 == 1) {
            return DepthSearchMode.REPAIR_WEAKEST_WINDOW;
        }
        return DepthSearchMode.GLOBAL;
    }

    static double requiredImprovement(PathPlannerOptions options) {
        return Math.max(options.qualityRetryImprovement(), options.iterativeDepthMinImprovement());
    }

    static double requiredImprovement(PathfinderSettings settings) {
        return Math.max(settings.qualityRetryImprovement.value(), settings.iterativeDepthMinImprovement.value());
    }

    private static int scale(int value, double multiplier) {
        return (int) Math.min(Integer.MAX_VALUE, Math.round(value * multiplier));
    }
}
