package fr.riege.ebsl.common.pathfinding;

import fr.riege.ebsl.common.pathfinding.annotation.PathingStage;
import fr.riege.ebsl.common.pathfinding.pathing.result.PathState;
import fr.riege.ebsl.common.pathfinding.pathing.result.PathfinderResult;
import fr.riege.ebsl.common.pathfinding.wrapper.PathPosition;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@PathingStage(PathingStage.Stage.RESULT_CLASSIFICATION)
public final class PathResultClassifier {
    private PathResultClassifier() {
    }

    public enum PathAvailability {
        COMPLETE,
        PARTIAL_USABLE,
        FAILED
    }

    public static PathAvailability classifyWalkResult(PathfinderResult result, Collection<PathPosition> positions,
                                               int requestedX, int requestedY, int requestedZ) {
        if (!hasUsablePath(result, positions)) {
            return PathAvailability.FAILED;
        }
        return isPartialWalkResult(result, positions, requestedX, requestedY, requestedZ)
            ? PathAvailability.PARTIAL_USABLE
            : PathAvailability.COMPLETE;
    }

    public static boolean hasUsablePath(PathfinderResult result, Collection<PathPosition> positions) {
        return result != null
            && result.getPath() != null
            && (result.successful() || result.hasFallenBack())
            && positions != null
            && !positions.isEmpty()
            && result.getPathState() != PathState.ABORTED;
    }

    public static boolean isPartialWalkResult(PathfinderResult result, Collection<PathPosition> positions,
                                       int requestedX, int requestedY, int requestedZ) {
        if (result == null || positions == null || positions.isEmpty()) {
            return true;
        }
        PathState state = result.getPathState();
        
        if (state == PathState.ABORTED || state == PathState.FAILED) {
            return true;
        }
        if (state != PathState.FOUND) {
            return true;
        }
        PathPosition last = positions instanceof List<?>
            ? ((List<PathPosition>) positions).getLast()
            : new ArrayList<>(positions).getLast();
        return last.flooredX() != requestedX
            || last.flooredY() != requestedY
            || last.flooredZ() != requestedZ;
    }
}
