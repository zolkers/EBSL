package fr.riege.ebsl.pathfinding;

import fr.riege.ebsl.pathfinding.annotation.PathingStage;
import fr.riege.ebsl.pathfinding.pathing.result.PathState;
import fr.riege.ebsl.pathfinding.pathing.result.PathfinderResult;
import fr.riege.ebsl.pathfinding.wrapper.PathPosition;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@PathingStage(PathingStage.Stage.RESULT_CLASSIFICATION)
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
        if (result == null || positions == null || positions.isEmpty()) {
            return true;
        }
        PathState state = result.getPathState();
        // Hard failures are always partial (no usable end position)
        if (state == PathState.ABORTED || state == PathState.FAILED) {
            return true;
        }
        // For FOUND and all fallback states, check whether the path end is close enough to the target.
        // A FALLBACK path that ends within 1 block of the target is effectively complete.
        PathPosition last = positions instanceof List<?>
            ? ((List<PathPosition>) positions).getLast()
            : new ArrayList<>(positions).getLast();
        int dx = Math.abs(last.flooredX() - requestedX);
        int dz = Math.abs(last.flooredZ() - requestedZ);
        int dy = Math.abs(last.flooredY() - requestedY);
        return dx > 1 || dz > 1 || dy > 2;
    }
}
