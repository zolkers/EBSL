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
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class PathPlannerOptionsTest {
    @Test
    void builderClampsQualityAndLimitValues() {
        PathPlannerOptions options = PathPlannerOptions.builder()
            .maxIterations(-4)
            .maxLength(0)
            .maxJumpHeight(0)
            .maxCalculationTimeMs(-12)
            .qualityPlanningMode(null)
            .qualityRiskCostWeight(-2.0)
            .qualityTerrainCostWeight(-3.0)
            .qualityRetryMinScore(2.0)
            .qualityRetryImprovement(-1.0)
            .iterativeDepthMax(0)
            .iterativeDepthIterationMultiplier(0.5)
            .iterativeDepthTimeMultiplier(0.25)
            .iterativeDepthQualityMultiplier(0.1)
            .iterativeDepthMinImprovement(2.0)
            .build();

        assertEquals(1, options.maxIterations());
        assertEquals(1, options.maxLength());
        assertEquals(1, options.maxJumpHeight());
        assertEquals(0, options.maxCalculationTimeMs());
        assertEquals(PathQualityPlanningMode.OFF, options.qualityPlanningMode());
        assertEquals(0.0, options.qualityRiskCostWeight());
        assertEquals(0.0, options.qualityTerrainCostWeight());
        assertEquals(1.0, options.qualityRetryMinScore());
        assertEquals(0.0, options.qualityRetryImprovement());
        assertEquals(1, options.iterativeDepthMax());
        assertEquals(1.0, options.iterativeDepthIterationMultiplier());
        assertEquals(1.0, options.iterativeDepthTimeMultiplier());
        assertEquals(1.0, options.iterativeDepthQualityMultiplier());
        assertEquals(1.0, options.iterativeDepthMinImprovement());
    }

    @Test
    void toBuilderPreservesQualityPlanningFields() {
        PathPlannerOptions original = PathPlannerOptions.builder()
            .qualityPlanningMode(PathQualityPlanningMode.CAUTIOUS)
            .qualityRiskCostWeight(1.25)
            .qualityTerrainCostWeight(0.75)
            .qualityRetryMinScore(0.66)
            .qualityRetryImprovement(0.08)
            .iterativeDepthEnabled(true)
            .iterativeDepthMax(5)
            .iterativeDepthIterationMultiplier(1.8)
            .iterativeDepthTimeMultiplier(1.4)
            .iterativeDepthQualityMultiplier(1.2)
            .iterativeDepthMinImprovement(0.03)
            .build();

        PathPlannerOptions copy = original.toBuilder().build();

        assertEquals(PathQualityPlanningMode.CAUTIOUS, copy.qualityPlanningMode());
        assertEquals(1.25, copy.qualityRiskCostWeight());
        assertEquals(0.75, copy.qualityTerrainCostWeight());
        assertEquals(0.66, copy.qualityRetryMinScore());
        assertEquals(0.08, copy.qualityRetryImprovement());
        assertTrue(copy.iterativeDepthEnabled());
        assertEquals(5, copy.iterativeDepthMax());
        assertEquals(1.8, copy.iterativeDepthIterationMultiplier());
        assertEquals(1.4, copy.iterativeDepthTimeMultiplier());
        assertEquals(1.2, copy.iterativeDepthQualityMultiplier());
        assertEquals(0.03, copy.iterativeDepthMinImprovement());
    }
}
