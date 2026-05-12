package fr.riege.ebsl.common.navigation;

import fr.riege.ebsl.common.pathfinding.quality.PathQualityPlanningMode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
    }

    @Test
    void toBuilderPreservesQualityPlanningFields() {
        PathPlannerOptions original = PathPlannerOptions.builder()
            .qualityPlanningMode(PathQualityPlanningMode.CAUTIOUS)
            .qualityRiskCostWeight(1.25)
            .qualityTerrainCostWeight(0.75)
            .qualityRetryMinScore(0.66)
            .qualityRetryImprovement(0.08)
            .build();

        PathPlannerOptions copy = original.toBuilder().build();

        assertEquals(PathQualityPlanningMode.CAUTIOUS, copy.qualityPlanningMode());
        assertEquals(1.25, copy.qualityRiskCostWeight());
        assertEquals(0.75, copy.qualityTerrainCostWeight());
        assertEquals(0.66, copy.qualityRetryMinScore());
        assertEquals(0.08, copy.qualityRetryImprovement());
    }
}
