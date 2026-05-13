package fr.riege.ebsl.common.pathfinding.pathing;

import fr.riege.ebsl.common.pathfinding.wrapper.PathVector;
import java.util.ArrayList;
import java.util.List;

public final class NeighborStrategies {
    private NeighborStrategies() {}

    private static final List<PathVector> VERTICAL_AND_HORIZONTAL_OFFSETS = List.of(
        new PathVector(1, 0, 0),
        new PathVector(-1, 0, 0),
        new PathVector(0, 0, 1),
        new PathVector(0, 0, -1),
        new PathVector(0, 1, 0),
        new PathVector(0,-1, 0)
    );

    private static final List<PathVector> HORIZONTAL_DIAGONAL_AND_VERTICAL_OFFSETS = List.of(
        new PathVector(1, 0, 0),
        new PathVector(-1, 0, 0),
        new PathVector(0, 0, 1),
        new PathVector(0, 0, -1),
        new PathVector(1,-1, 0),
        new PathVector(-1,-1, 0),
        new PathVector(0,-1, 1),
        new PathVector(0,-1, -1),
        new PathVector(0, 1, 0),
        new PathVector(0,-1, 0),
        new PathVector(1, 1, 0),
        new PathVector(-1, 1, 0),
        new PathVector(0, 1, 1),
        new PathVector(0, 1, -1),
        new PathVector(1, 0, 1),
        new PathVector(1, 0, -1),
        new PathVector(-1, 0, 1),
        new PathVector(-1, 0, -1)
    );

    public static final INeighborStrategy VERTICAL_AND_HORIZONTAL =
        () -> VERTICAL_AND_HORIZONTAL_OFFSETS;

    public static final INeighborStrategy HORIZONTAL_DIAGONAL_AND_VERTICAL =
        () -> HORIZONTAL_DIAGONAL_AND_VERTICAL_OFFSETS;

    public static INeighborStrategy horizontalDiagonalAndVertical(int maxJumpHeight) {
        return horizontalDiagonalAndVertical(maxJumpHeight, true, true, true, true);
    }

    public static INeighborStrategy horizontalDiagonalAndVertical(int maxJumpHeight, boolean allowParkour) {
        return horizontalDiagonalAndVertical(maxJumpHeight, allowParkour, true, true, true);
    }

    public static INeighborStrategy horizontalDiagonalAndVertical(int maxJumpHeight, boolean allowParkour,
                                                                  boolean allowJump, boolean allowFall,
                                                                  boolean allowWalkDiagonal) {
        List<PathVector> offsets = new ArrayList<>();

        offsets.add(new PathVector(1, 0, 0));
        offsets.add(new PathVector(-1, 0, 0));
        offsets.add(new PathVector(0, 0, 1));
        offsets.add(new PathVector(0, 0, -1));

        if (allowFall) {
            offsets.add(new PathVector(1,-1, 0));
            offsets.add(new PathVector(-1,-1, 0));
            offsets.add(new PathVector(0,-1, 1));
            offsets.add(new PathVector(0,-1, -1));
            offsets.add(new PathVector(0,-1, 0));
        }

        offsets.add(new PathVector(0, 1, 0));
        offsets.add(new PathVector(1, 1, 0));
        offsets.add(new PathVector(-1, 1, 0));
        offsets.add(new PathVector(0, 1, 1));
        offsets.add(new PathVector(0, 1, -1));

        if (allowWalkDiagonal) {
            offsets.add(new PathVector(1, 0, 1));
            offsets.add(new PathVector(1, 0, -1));
            offsets.add(new PathVector(-1, 0, 1));
            offsets.add(new PathVector(-1, 0, -1));
        }

        if (allowJump) {
            int cappedJumpHeight = Math.max(1, maxJumpHeight);
            for (int height = 2; height <= cappedJumpHeight; height++) {
                offsets.add(new PathVector(0, height, 0));
            }
        }

        if (allowParkour) {
            addParkourOffsets(offsets);
        }

        List<PathVector> immutableOffsets = List.copyOf(offsets);
        return () -> immutableOffsets;
    }

    private static void addParkourOffsets(List<PathVector> offsets) {
        for (int distance = 2; distance <= 4; distance++) {
            addCardinalParkourOffset(offsets, distance, 0);
            addCardinalParkourOffset(offsets, distance, 1);
            for (int dy = -1; dy >= -5; dy--) {
                addCardinalParkourOffset(offsets, distance, dy);
            }
        }

        for (int longAxis = 2; longAxis <= 3; longAxis++) {
            for (int shortAxis = 1; shortAxis <= longAxis; shortAxis++) {
                addDiagonalParkourOffset(offsets, longAxis, shortAxis, 0);
                addDiagonalParkourOffset(offsets, longAxis, shortAxis, 1);
                for (int dy = -1; dy >= -5; dy--) {
                    addDiagonalParkourOffset(offsets, longAxis, shortAxis, dy);
                }
            }
        }
    }

    private static void addCardinalParkourOffset(List<PathVector> offsets, int distance, int dy) {
        offsets.add(new PathVector(distance, dy, 0));
        offsets.add(new PathVector(-distance, dy, 0));
        offsets.add(new PathVector(0, dy, distance));
        offsets.add(new PathVector(0, dy, -distance));
    }

    private static void addDiagonalParkourOffset(List<PathVector> offsets, int x, int z, int dy) {
        offsets.add(new PathVector(x, dy, z));
        offsets.add(new PathVector(x, dy, -z));
        offsets.add(new PathVector(-x, dy, z));
        offsets.add(new PathVector(-x, dy, -z));
        if (x == z) {
            return;
        }
        offsets.add(new PathVector(z, dy, x));
        offsets.add(new PathVector(z, dy, -x));
        offsets.add(new PathVector(-z, dy, x));
        offsets.add(new PathVector(-z, dy, -x));
    }
}
