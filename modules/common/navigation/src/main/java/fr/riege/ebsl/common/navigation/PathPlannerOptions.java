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

import fr.riege.ebsl.common.pathfinding.quality.PathQualityPlanningMode;
import fr.riege.ebsl.common.pathfinding.settings.PathfinderSettings;

public record PathPlannerOptions(
    int maxIterations,
    int maxLength,
    int maxJumpHeight,
    boolean async,
    boolean fallback,
    boolean allowParkour,
    boolean allowJump,
    boolean allowFall,
    boolean allowWalkDiagonal,
    boolean processPath,
    int maxCalculationTimeMs,
    PathQualityPlanningMode qualityPlanningMode,
    double qualityRiskCostWeight,
    double qualityTerrainCostWeight,
    double qualityRetryMinScore,
    double qualityRetryImprovement,
    boolean iterativeDepthEnabled,
    int iterativeDepthMax,
    double iterativeDepthIterationMultiplier,
    double iterativeDepthTimeMultiplier,
    double iterativeDepthQualityMultiplier,
    double iterativeDepthMinImprovement
) {
    public static PathPlannerOptions defaults() {
        PathfinderSettings settings = PathfinderSettings.instance();
        return builder()
            .maxIterations(settings.defaultWalkMaxIterations.value())
            .maxLength(settings.defaultWalkMaxLength.value())
            .maxCalculationTimeMs(settings.defaultCalculationTimeMs.value())
            .maxJumpHeight(settings.maxJumpHeight.value())
            .qualityPlanningMode(settings.qualityPlanningMode.value())
            .qualityRiskCostWeight(settings.qualityRiskCostWeight.value())
            .qualityTerrainCostWeight(settings.qualityTerrainCostWeight.value())
            .qualityRetryMinScore(settings.qualityRetryMinScore.value())
            .qualityRetryImprovement(settings.qualityRetryImprovement.value())
            .iterativeDepthEnabled(settings.iterativeDepthEnabled.value())
            .iterativeDepthMax(settings.iterativeDepthMax.value())
            .iterativeDepthIterationMultiplier(settings.iterativeDepthIterationMultiplier.value())
            .iterativeDepthTimeMultiplier(settings.iterativeDepthTimeMultiplier.value())
            .iterativeDepthQualityMultiplier(settings.iterativeDepthQualityMultiplier.value())
            .iterativeDepthMinImprovement(settings.iterativeDepthMinImprovement.value())
            .build();
    }

    public static PathPlannerOptions instant() {
        PathfinderSettings settings = PathfinderSettings.instance();
        return defaults().toBuilder()
            .maxIterations(settings.instantWalkMaxIterations.value())
            .maxLength(settings.instantWalkMaxLength.value())
            .maxCalculationTimeMs(settings.instantCalculationTimeMs.value())
            .build();
    }

    public Builder toBuilder() {
        return builder()
            .maxIterations(maxIterations)
            .maxLength(maxLength)
            .maxJumpHeight(maxJumpHeight)
            .async(async)
            .fallback(fallback)
            .allowParkour(allowParkour)
            .allowJump(allowJump)
            .allowFall(allowFall)
            .allowWalkDiagonal(allowWalkDiagonal)
            .processPath(processPath)
            .maxCalculationTimeMs(maxCalculationTimeMs)
            .qualityPlanningMode(qualityPlanningMode)
            .qualityRiskCostWeight(qualityRiskCostWeight)
            .qualityTerrainCostWeight(qualityTerrainCostWeight)
            .qualityRetryMinScore(qualityRetryMinScore)
            .qualityRetryImprovement(qualityRetryImprovement)
            .iterativeDepthEnabled(iterativeDepthEnabled)
            .iterativeDepthMax(iterativeDepthMax)
            .iterativeDepthIterationMultiplier(iterativeDepthIterationMultiplier)
            .iterativeDepthTimeMultiplier(iterativeDepthTimeMultiplier)
            .iterativeDepthQualityMultiplier(iterativeDepthQualityMultiplier)
            .iterativeDepthMinImprovement(iterativeDepthMinImprovement);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private int maxIterations = 50000;
        private int maxLength = 5000;
        private int maxJumpHeight = 2;
        private boolean async = true;
        private boolean fallback = true;
        private boolean allowParkour = true;
        private boolean allowJump = true;
        private boolean allowFall = true;
        private boolean allowWalkDiagonal = true;
        private boolean processPath = true;
        private int maxCalculationTimeMs = 0;
        private PathQualityPlanningMode qualityPlanningMode = PathQualityPlanningMode.OFF;
        private double qualityRiskCostWeight = 0.0;
        private double qualityTerrainCostWeight = 0.0;
        private double qualityRetryMinScore = 0.0;
        private double qualityRetryImprovement = 0.0;
        private boolean iterativeDepthEnabled = false;
        private int iterativeDepthMax = 1;
        private double iterativeDepthIterationMultiplier = 1.0;
        private double iterativeDepthTimeMultiplier = 1.0;
        private double iterativeDepthQualityMultiplier = 1.0;
        private double iterativeDepthMinImprovement = 0.0;

        public Builder maxIterations(int value) {
            this.maxIterations = value;
            return this;
        }

        public Builder maxLength(int value) {
            this.maxLength = value;
            return this;
        }

        public Builder maxJumpHeight(int value) {
            this.maxJumpHeight = value;
            return this;
        }

        public Builder async(boolean value) {
            this.async = value;
            return this;
        }

        public Builder fallback(boolean value) {
            this.fallback = value;
            return this;
        }

        public Builder allowParkour(boolean value) {
            this.allowParkour = value;
            return this;
        }

        public Builder allowJump(boolean value) {
            this.allowJump = value;
            return this;
        }

        public Builder allowFall(boolean value) {
            this.allowFall = value;
            return this;
        }

        public Builder allowWalkDiagonal(boolean value) {
            this.allowWalkDiagonal = value;
            return this;
        }

        public Builder processPath(boolean value) {
            this.processPath = value;
            return this;
        }

        public Builder maxCalculationTimeMs(int value) {
            this.maxCalculationTimeMs = value;
            return this;
        }

        public Builder qualityPlanningMode(PathQualityPlanningMode value) {
            this.qualityPlanningMode = value == null ? PathQualityPlanningMode.OFF : value;
            return this;
        }

        public Builder qualityRiskCostWeight(double value) {
            this.qualityRiskCostWeight = value;
            return this;
        }

        public Builder qualityTerrainCostWeight(double value) {
            this.qualityTerrainCostWeight = value;
            return this;
        }

        public Builder qualityRetryMinScore(double value) {
            this.qualityRetryMinScore = value;
            return this;
        }

        public Builder qualityRetryImprovement(double value) {
            this.qualityRetryImprovement = value;
            return this;
        }

        public Builder iterativeDepthEnabled(boolean value) {
            this.iterativeDepthEnabled = value;
            return this;
        }

        public Builder iterativeDepthMax(int value) {
            this.iterativeDepthMax = value;
            return this;
        }

        public Builder iterativeDepthIterationMultiplier(double value) {
            this.iterativeDepthIterationMultiplier = value;
            return this;
        }

        public Builder iterativeDepthTimeMultiplier(double value) {
            this.iterativeDepthTimeMultiplier = value;
            return this;
        }

        public Builder iterativeDepthQualityMultiplier(double value) {
            this.iterativeDepthQualityMultiplier = value;
            return this;
        }

        public Builder iterativeDepthMinImprovement(double value) {
            this.iterativeDepthMinImprovement = value;
            return this;
        }

        public PathPlannerOptions build() {
            return new PathPlannerOptions(
                Math.max(1, maxIterations),
                Math.max(1, maxLength),
                Math.max(1, maxJumpHeight),
                async,
                fallback,
                allowParkour,
                allowJump,
                allowFall,
                allowWalkDiagonal,
                processPath,
                Math.max(0, maxCalculationTimeMs),
                qualityPlanningMode == null ? PathQualityPlanningMode.OFF : qualityPlanningMode,
                Math.max(0.0, qualityRiskCostWeight),
                Math.max(0.0, qualityTerrainCostWeight),
                Math.clamp(qualityRetryMinScore, 0.0, 1.0),
                Math.clamp(qualityRetryImprovement, 0.0, 1.0),
                iterativeDepthEnabled,
                Math.max(1, iterativeDepthMax),
                Math.max(1.0, iterativeDepthIterationMultiplier),
                Math.max(1.0, iterativeDepthTimeMultiplier),
                Math.max(1.0, iterativeDepthQualityMultiplier),
                Math.clamp(iterativeDepthMinImprovement, 0.0, 1.0));
        }
    }
}
