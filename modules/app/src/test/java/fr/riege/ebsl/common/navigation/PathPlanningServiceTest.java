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
import fr.riege.ebsl.common.pathfinding.Node;
import fr.riege.ebsl.common.pathfinding.pathing.configuration.PathfinderConfiguration;
import fr.riege.ebsl.common.pathfinding.pathing.result.PathState;
import fr.riege.ebsl.common.pathfinding.pathing.result.PathfinderResult;
import fr.riege.ebsl.common.pathfinding.pathing.result.PathfinderResults;
import fr.riege.ebsl.common.pathfinding.pathing.result.Paths;
import fr.riege.ebsl.common.pathfinding.quality.PathQualityGrade;
import fr.riege.ebsl.common.pathfinding.quality.PathQualityPlanningMode;
import fr.riege.ebsl.common.pathfinding.quality.PathQualityReport;
import fr.riege.ebsl.common.pathfinding.wrapper.PathPosition;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

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

    @Test
    void iterativeDepthProfileKeepsUnlimitedTimeUnlimited() {
        PathPlannerOptions options = PathPlannerOptions.builder()
            .maxIterations(120)
            .maxLength(81)
            .maxCalculationTimeMs(0)
            .iterativeDepthIterationMultiplier(3.0)
            .iterativeDepthTimeMultiplier(2.0)
            .iterativeDepthQualityMultiplier(1.5)
            .build();

        IterativeDepthProfile profile = IterativeDepthPlanner.profile(options, 2);

        assertEquals(360, profile.maxIterations());
        assertEquals(140, profile.maxLength());
        assertEquals(0, profile.maxCalculationTimeMs());
        assertEquals(1.5, profile.qualityScale());
    }

    @Test
    void depthSelectorRequiresMeaningfulQualityImprovement() {
        PathPlan primary = plan(PathState.FOUND, 0.55);
        PathPlan tinyImprovement = plan(PathState.FOUND, 0.56);
        PathPlan meaningfulImprovement = plan(PathState.FOUND, 0.63);

        assertSame(primary, DepthPlanSelector.select(primary, tinyImprovement, 0.04));
        assertSame(meaningfulImprovement, DepthPlanSelector.select(primary, meaningfulImprovement, 0.04));
    }

    @Test
    void depthSelectorPrefersCompletePathOverFallback() {
        PathPlan fallback = plan(PathState.FALLBACK, 0.80);
        PathPlan complete = plan(PathState.FOUND, 0.58);

        assertSame(complete, DepthPlanSelector.select(fallback, complete, 0.30));
        assertSame(complete, DepthPlanSelector.select(complete, fallback, 0.01));
    }

    private static PathPlan plan(PathState state, double score) {
        List<PathPosition> positions = List.of(
            new PathPosition(0, 64, 0),
            new PathPosition(1, 64, 0)
        );
        PathfinderResult result = PathfinderResults.of(state, Paths.of(positions.getFirst(), positions.getLast(), positions));
        List<Node> nodes = positions.stream().map(Node::new).toList();
        return new PathPlan(
            result,
            PathfinderConfiguration.DEFAULT,
            positions,
            nodes,
            nodes,
            1.0,
            new PathQualityReport(score, PathQualityGrade.GOOD, List.of()));
    }
}
