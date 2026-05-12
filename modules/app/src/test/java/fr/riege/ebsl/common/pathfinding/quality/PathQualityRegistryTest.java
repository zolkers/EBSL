package fr.riege.ebsl.common.pathfinding.quality;

import fr.riege.ebsl.common.pathfinding.Node;
import fr.riege.ebsl.common.pathfinding.pathing.configuration.PathfinderConfiguration;
import fr.riege.ebsl.common.pathfinding.pathing.result.PathState;
import fr.riege.ebsl.common.pathfinding.result.PathImpl;
import fr.riege.ebsl.common.pathfinding.result.PathfinderResultImpl;
import fr.riege.ebsl.common.pathfinding.wrapper.PathPosition;
import org.junit.jupiter.api.Test;

import java.util.List;

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
        risky.moveType = Node.MoveType.PARKOUR;
        Node turn = new Node(positions.get(2));
        turn.moveType = Node.MoveType.WALK;
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
}
