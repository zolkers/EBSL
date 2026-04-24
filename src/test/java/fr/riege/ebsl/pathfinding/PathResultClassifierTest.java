package fr.riege.ebsl.pathfinding;

import fr.riege.ebsl.pathfinding.pathing.result.Path;
import fr.riege.ebsl.pathfinding.pathing.result.PathState;
import fr.riege.ebsl.pathfinding.pathing.result.PathfinderResult;
import fr.riege.ebsl.pathfinding.result.PathImpl;
import fr.riege.ebsl.pathfinding.result.PathfinderResultImpl;
import fr.riege.ebsl.pathfinding.wrapper.PathPosition;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PathResultClassifierTest {
    @Test
    void usablePathRequiresResultPathPositionsAndNonAbortedState() {
        List<PathPosition> positions = positions(new PathPosition(0, 64, 0), new PathPosition(1, 64, 0));
        PathfinderResult found = result(PathState.FOUND, positions);
        PathfinderResult fallback = result(PathState.FALLBACK, positions);
        PathfinderResult aborted = result(PathState.ABORTED, positions);

        assertTrue(PathResultClassifier.hasUsablePath(found, positions));
        assertTrue(PathResultClassifier.hasUsablePath(fallback, positions));
        assertFalse(PathResultClassifier.hasUsablePath(null, positions));
        assertFalse(PathResultClassifier.hasUsablePath(new PathfinderResultImpl(PathState.FOUND, null), positions));
        assertFalse(PathResultClassifier.hasUsablePath(aborted, positions));
        assertFalse(PathResultClassifier.hasUsablePath(found, List.of()));
    }

    @Test
    void partialWalkResultDependsOnFinalPositionToleranceAndResultState() {
        assertFalse(PathResultClassifier.isPartialWalkResult(
            result(PathState.FOUND, positions(new PathPosition(10, 64, 10))),
            positions(new PathPosition(10, 64, 10)),
            10, 64, 10));
        assertFalse(PathResultClassifier.isPartialWalkResult(
            result(PathState.FOUND, positions(new PathPosition(11, 66, 11))),
            positions(new PathPosition(11, 66, 11)),
            10, 64, 10));
        assertTrue(PathResultClassifier.isPartialWalkResult(
            result(PathState.FOUND, positions(new PathPosition(12, 64, 10))),
            positions(new PathPosition(12, 64, 10)),
            10, 64, 10));
        assertTrue(PathResultClassifier.isPartialWalkResult(
            result(PathState.FALLBACK, positions(new PathPosition(10, 64, 10))),
            positions(new PathPosition(10, 64, 10)),
            10, 64, 10));
    }

    private static PathfinderResult result(PathState state, List<PathPosition> positions) {
        Path path = new PathImpl(positions.getFirst(), positions.getLast(), positions);
        return new PathfinderResultImpl(state, path);
    }

    private static List<PathPosition> positions(PathPosition... positions) {
        return List.of(positions);
    }
}
