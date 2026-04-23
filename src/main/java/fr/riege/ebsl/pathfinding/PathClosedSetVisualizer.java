package fr.riege.ebsl.pathfinding;

import fr.riege.ebsl.pathfinding.debug.PathVisualizer;
import fr.riege.ebsl.pathfinding.pathfinder.AStarPathfinder;

final class PathClosedSetVisualizer {
    private static final long MASK_Y = 0xFFFL;
    private static final long MASK_XZ = 0x3FFFFFFL;
    private static final int SHIFT_Z = 12;
    private static final int SHIFT_X = 38;

    private PathClosedSetVisualizer() {
    }

    static void pushExploredNodes(AStarPathfinder pathfinder) {
        if (pathfinder.getClosedSet() == null) {
            return;
        }
        for (long key : pathfinder.getClosedSet()) {
            PathVisualizer.addExplored(unpackX(key), unpackY(key), unpackZ(key));
        }
    }

    private static int unpackX(long key) {
        long raw = (key >> SHIFT_X) & MASK_XZ;
        return (raw & (1L << 25)) != 0 ? (int) (raw | ~MASK_XZ) : (int) raw;
    }

    private static int unpackY(long key) {
        long raw = key & MASK_Y;
        return (raw & (1L << 11)) != 0 ? (int) (raw | ~MASK_Y) : (int) raw;
    }

    private static int unpackZ(long key) {
        long raw = (key >> SHIFT_Z) & MASK_XZ;
        return (raw & (1L << 25)) != 0 ? (int) (raw | ~MASK_XZ) : (int) raw;
    }
}
