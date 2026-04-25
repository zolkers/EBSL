package fr.riege.ebsl.pathfinding.pathing;

import fr.riege.ebsl.pathfinding.wrapper.PathVector;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class NeighborStrategiesTest {
    @Test
    void walkStrategyIncludesCardinalAscends() {
        List<PathVector> offsets = new ArrayList<>();
        NeighborStrategies.horizontalDiagonalAndVertical(1).getOffsets()
            .forEach(offsets::add);

        assertTrue(hasOffset(offsets, 1, 1, 0));
        assertTrue(hasOffset(offsets, -1, 1, 0));
        assertTrue(hasOffset(offsets, 0, 1, 1));
        assertTrue(hasOffset(offsets, 0, 1, -1));
    }

    private static boolean hasOffset(List<PathVector> offsets, double x, double y, double z) {
        return offsets.stream().anyMatch(offset ->
            Double.compare(offset.x, x) == 0
                && Double.compare(offset.y, y) == 0
                && Double.compare(offset.z, z) == 0);
    }
}
