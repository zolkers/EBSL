package fr.riege.ebsl.pathfinding.pathing;

import fr.riege.ebsl.pathfinding.wrapper.PathVector;
import java.util.ArrayList;
import java.util.List;

public final class NeighborStrategies {
    private NeighborStrategies() {}

    private static final List<PathVector> VERTICAL_AND_HORIZONTAL_OFFSETS = List.of(
        new PathVector( 1, 0,  0),
        new PathVector(-1, 0,  0),
        new PathVector( 0, 0,  1),
        new PathVector( 0, 0, -1),
        new PathVector( 0, 1,  0),
        new PathVector( 0,-1,  0)
    );

    private static final List<PathVector> HORIZONTAL_DIAGONAL_AND_VERTICAL_OFFSETS = List.of(
        new PathVector( 1, 0,  0),
        new PathVector(-1, 0,  0),
        new PathVector( 0, 0,  1),
        new PathVector( 0, 0, -1),
        new PathVector( 0, 1,  0),
        new PathVector( 0,-1,  0),
        new PathVector( 1, 1,  0),
        new PathVector(-1, 1,  0),
        new PathVector( 0, 1,  1),
        new PathVector( 0, 1, -1),
        new PathVector( 1, 0,  1),
        new PathVector( 1, 0, -1),
        new PathVector(-1, 0,  1),
        new PathVector(-1, 0, -1)
    );

    public static final INeighborStrategy VERTICAL_AND_HORIZONTAL =
        () -> VERTICAL_AND_HORIZONTAL_OFFSETS;

    public static final INeighborStrategy HORIZONTAL_DIAGONAL_AND_VERTICAL =
        () -> HORIZONTAL_DIAGONAL_AND_VERTICAL_OFFSETS;

    public static INeighborStrategy horizontalDiagonalAndVertical(int maxJumpHeight) {
        int cappedJumpHeight = Math.max(1, maxJumpHeight);
        List<PathVector> offsets = new ArrayList<>(HORIZONTAL_DIAGONAL_AND_VERTICAL_OFFSETS);
        addParkourOffsets(offsets);
        for (int height = 2; height <= cappedJumpHeight; height++) {
            offsets.add(new PathVector(0, height, 0));
        }

        List<PathVector> immutableOffsets = List.copyOf(offsets);
        return () -> immutableOffsets;
    }

    private static void addParkourOffsets(List<PathVector> offsets) {
        for (int distance = 2; distance <= 4; distance++) {
            offsets.add(new PathVector( distance, 0,  0));
            offsets.add(new PathVector(-distance, 0,  0));
            offsets.add(new PathVector( 0, 0,  distance));
            offsets.add(new PathVector( 0, 0, -distance));
        }
    }
}
