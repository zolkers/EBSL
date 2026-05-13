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

import fr.riege.ebsl.common.navigation.runtime.headless.HeadlessWorldLayer;
import fr.riege.ebsl.common.pathfinding.quality.PathQualityPlanningMode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class PathPlanningServiceTest {
    @Test
    void configurationIgnoresQualityWeightsWhenModeIsOff() {
        PathPlanningService service = new PathPlanningService(HeadlessWorldLayer.flat(63));
        var configuration = service.configuration(PathPlannerOptions.builder()
            .qualityPlanningMode(PathQualityPlanningMode.OFF)
            .qualityRiskCostWeight(4.0)
            .qualityTerrainCostWeight(3.0)
            .build());

        assertEquals(0.0, configuration.qualityRiskCostWeight);
        assertEquals(0.0, configuration.qualityTerrainCostWeight);
    }

    @Test
    void configurationPassesQualityWeightsWhenModeIsCostAware() {
        PathPlanningService service = new PathPlanningService(HeadlessWorldLayer.flat(63));
        var configuration = service.configuration(PathPlannerOptions.builder()
            .qualityPlanningMode(PathQualityPlanningMode.BALANCED)
            .qualityRiskCostWeight(1.5)
            .qualityTerrainCostWeight(0.5)
            .build());

        assertEquals(1.5, configuration.qualityRiskCostWeight);
        assertEquals(0.5, configuration.qualityTerrainCostWeight);
    }

    @Test
    void depthOptionsScaleBudgetsAndQualityWeights() {
        PathPlannerOptions options = PathPlannerOptions.builder()
            .maxIterations(100)
            .maxLength(100)
            .maxCalculationTimeMs(20)
            .qualityRiskCostWeight(2.0)
            .qualityTerrainCostWeight(1.0)
            .iterativeDepthIterationMultiplier(2.0)
            .iterativeDepthTimeMultiplier(1.5)
            .iterativeDepthQualityMultiplier(1.25)
            .build();

        PathPlannerOptions depthThree = PathPlanningService.depthOptions(options, 3);

        assertEquals(400, depthThree.maxIterations());
        assertEquals(200, depthThree.maxLength());
        assertEquals(45, depthThree.maxCalculationTimeMs());
        assertEquals(3.125, depthThree.qualityRiskCostWeight());
        assertEquals(1.5625, depthThree.qualityTerrainCostWeight());
    }
}
