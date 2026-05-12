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
}
