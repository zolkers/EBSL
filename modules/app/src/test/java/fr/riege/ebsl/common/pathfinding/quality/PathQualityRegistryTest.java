package fr.riege.ebsl.common.pathfinding.quality;

import fr.riege.ebsl.common.domain.world.BlockId;
import fr.riege.ebsl.common.pathfinding.Node;
import fr.riege.ebsl.common.pathfinding.movement.WalkabilityChecker;
import fr.riege.ebsl.common.pathfinding.pathing.configuration.PathfinderConfiguration;
import fr.riege.ebsl.common.pathfinding.pathing.context.EnvironmentContext;
import fr.riege.ebsl.common.pathfinding.pathing.processing.context.EvaluationContext;
import fr.riege.ebsl.common.pathfinding.pathing.processing.context.SearchContext;
import fr.riege.ebsl.common.pathfinding.pathing.processing.impl.QualityAwarePathProcessor;
import fr.riege.ebsl.common.pathfinding.pathing.result.PathState;
import fr.riege.ebsl.common.pathfinding.provider.LayerNavigationPointProvider;
import fr.riege.ebsl.common.pathfinding.provider.NavigationPointProvider;
import fr.riege.ebsl.common.pathfinding.result.PathImpl;
import fr.riege.ebsl.common.pathfinding.result.PathfinderResultImpl;
import fr.riege.ebsl.common.pathfinding.wrapper.PathPosition;
import fr.riege.ebsl.common.world.layer.IWorldLayer;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class PathQualityRegistryTest {
    @Test
    void completeStraightWalkScoresHigh() {
        List<PathPosition> positions = List.of(
            new PathPosition(0, 64, 0),
            new PathPosition(1, 64, 0),
            new PathPosition(2, 64, 0)
        );
        PathfinderResultImpl result = new PathfinderResultImpl(
            PathState.FOUND,
            new PathImpl(positions.getFirst(), positions.getLast(), positions)
        );

        PathQualityReport report = PathQualityRegistry.evaluate(PathQualityContext.of(
            result,
            PathfinderConfiguration.DEFAULT,
            positions
        ));

        assertTrue(report.score() >= 0.85, "straight complete path should be high quality");
        assertEquals(PathQualityGrade.EXCELLENT, report.grade());
    }

    @Test
    void parkourFallbackScoresLowerThanStraightWalk() {
        List<PathPosition> positions = List.of(
            new PathPosition(0, 64, 0),
            new PathPosition(2, 64, 0),
            new PathPosition(2, 64, 1)
        );
        Node start = new Node(positions.get(0));
        Node risky = new Node(positions.get(1));
        risky.setMoveType(Node.MoveType.PARKOUR);
        Node turn = new Node(positions.get(2));
        turn.setMoveType(Node.MoveType.WALK);
        PathfinderResultImpl result = new PathfinderResultImpl(
            PathState.FALLBACK,
            new PathImpl(positions.getFirst(), new PathPosition(8, 64, 0), positions)
        );

        PathQualityReport report = PathQualityRegistry.evaluate(new PathQualityContext(
            result,
            PathfinderConfiguration.DEFAULT,
            positions,
            List.of(start, risky, turn),
            List.of(),
            3.0
        ));

        assertTrue(report.score() < 0.75, "fallback risky path should not look excellent");
        assertTrue(report.contributions().stream().anyMatch(c -> c.metricId().equals("movement_risk")));
    }

    @Test
    void terrainOpportunityRewardsLoadedOpenWalkableGround() {
        List<PathPosition> positions = List.of(
            new PathPosition(0, 64, 0),
            new PathPosition(1, 64, 0),
            new PathPosition(2, 64, 0)
        );
        PathfinderResultImpl result = new PathfinderResultImpl(
            PathState.FOUND,
            new PathImpl(positions.getFirst(), positions.getLast(), positions)
        );

        PathQualityReport open = PathQualityRegistry.evaluate(new PathQualityContext(
            result,
            PathfinderConfiguration.DEFAULT,
            positions,
            List.of(),
            List.of(),
            2.0,
            new WalkabilityChecker(new TestWorld(false))
        ));
        PathQualityReport boxed = PathQualityRegistry.evaluate(new PathQualityContext(
            result,
            PathfinderConfiguration.DEFAULT,
            positions,
            List.of(),
            List.of(),
            2.0,
            new WalkabilityChecker(new TestWorld(true))
        ));

        double openTerrain = contribution(open, "terrain_opportunity");
        double boxedTerrain = contribution(boxed, "terrain_opportunity");
        assertTrue(openTerrain > boxedTerrain, "open terrain should be considered more favorable");
        assertTrue(openTerrain >= 0.85, "flat loaded ground should be very favorable");
    }

    @Test
    void qualityAwareProcessorAddsConfiguredRiskCost() {
        WalkabilityChecker checker = new WalkabilityChecker(new TestWorld(false, true));
        LayerNavigationPointProvider provider = new LayerNavigationPointProvider(checker);
        QualityAwarePathProcessor processor = new QualityAwarePathProcessor();
        PathPosition previous = new PathPosition(0, 64, 0);
        PathPosition current = new PathPosition(2, 64, 0);
        PathfinderConfiguration disabled = PathfinderConfiguration.builder()
            .provider(provider)
            .build();
        PathfinderConfiguration enabled = PathfinderConfiguration.builder()
            .provider(provider)
            .qualityRiskCostWeight(2.0)
            .build();

        assertEquals(0.0, processor.calculateCostContribution(
            new TestEvaluationContext(disabled, provider, previous, current)).value, 0.0001);
        assertEquals(MovementRiskScorer.planningPenalty(Node.MoveType.PARKOUR) * 2.0, processor.calculateCostContribution(
            new TestEvaluationContext(enabled, provider, previous, current)).value, 0.0001);
    }

    private static double contribution(PathQualityReport report, String metricId) {
        return report.contributions().stream()
            .filter(contribution -> contribution.metricId().equals(metricId))
            .findFirst()
            .orElseThrow()
            .score();
    }

    private record TestEvaluationContext(PathfinderConfiguration configuration,
                                         NavigationPointProvider provider,
                                         PathPosition previous,
                                         PathPosition current) implements EvaluationContext {
        @Override public PathPosition getCurrentPathPosition() { return current; }
        @Override public PathPosition getPreviousPathPosition() { return previous; }
        @Override public int getCurrentNodeDepth() { return 1; }
        @Override public double getCurrentNodeHeuristicValue() { return 0.0; }
        @Override public double getPathCostToPreviousPosition() { return 0.0; }
        @Override public double getBaseTransitionCost() { return 1.0; }
        @Override public PathPosition getGrandparentPathPosition() { return null; }

        @Override
        public SearchContext getSearchContext() {
            return new TestSearchContext(configuration, provider, previous, current);
        }
    }

    private record TestSearchContext(PathfinderConfiguration configuration,
                                     NavigationPointProvider provider,
                                     PathPosition start,
                                     PathPosition target) implements SearchContext {
        @Override public PathPosition getStartPathPosition() { return start; }
        @Override public PathPosition getTargetPathPosition() { return target; }
        @Override public PathfinderConfiguration getPathfinderConfiguration() { return configuration; }
        @Override public NavigationPointProvider getNavigationPointProvider() { return provider; }
        @Override public Map<String, Object> getSharedData() { return Map.of(); }
        @Override public EnvironmentContext getEnvironmentContext() { return null; }
    }

    private record TestWorld(boolean boxed, boolean gap) implements IWorldLayer {
        TestWorld(boolean boxed) {
            this(boxed, false);
        }

        @Override
        public BlockId getBlock(int x, int y, int z) {
            return isAir(x, y, z) ? BlockId.AIR : BlockId.of("minecraft:stone");
        }

        @Override
        public boolean isAir(int x, int y, int z) {
            return !isSolid(x, y, z);
        }

        @Override
        public boolean isSolid(int x, int y, int z) {
            if (gap && y == 63 && x == 1 && z == 0) {
                return false;
            }
            if (y == 63) {
                return true;
            }
            return boxed && y == 64 && (z == 1 || z == -1);
        }

        @Override public boolean isWater(int x, int y, int z) { return false; }
        @Override public boolean isLava(int x, int y, int z) { return false; }
        @Override public boolean isLoaded(int x, int y, int z) { return true; }
        @Override public int getTopSolidY(int x, int z) { return 63; }
        @Override public double getBlockHeight(int x, int y, int z) { return isSolid(x, y, z) ? 1.0 : 0.0; }
    }
}
