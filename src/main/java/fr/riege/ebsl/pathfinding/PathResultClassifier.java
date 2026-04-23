package fr.riege.ebsl.pathfinding;

import fr.riege.ebsl.pathfinding.pathing.result.PathState;
import fr.riege.ebsl.pathfinding.pathing.result.PathfinderResult;
import fr.riege.ebsl.pathfinding.wrapper.PathPosition;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

final class PathResultClassifier {
    private PathResultClassifier() {
    }

    static boolean hasUsablePath(PathfinderResult result, Collection<PathPosition> positions) {
        return result != null
            && result.getPath() != null
            && (result.successful() || result.hasFallenBack())
            && positions != null
            && !positions.isEmpty()
            && result.getPathState() != PathState.ABORTED;
    }

    static boolean isPartialWalkResult(PathfinderResult result, Collection<PathPosition> positions,
                                       int requestedX, int requestedY, int requestedZ) {
        if (result == null || result.getPathState() != PathState.FOUND) {
            return true;
        }
        if (positions == null || positions.isEmpty()) {
            return true;
        }

        PathPosition last = positions instanceof List<?>
            ? ((List<PathPosition>) positions).getLast()
            : new ArrayList<>(positions).getLast();
        int dx = Math.abs(last.flooredX() - requestedX);
        int dz = Math.abs(last.flooredZ() - requestedZ);
        int dy = Math.abs(last.flooredY() - requestedY);
        return dx > 1 || dz > 1 || dy > 2;
    }
}
