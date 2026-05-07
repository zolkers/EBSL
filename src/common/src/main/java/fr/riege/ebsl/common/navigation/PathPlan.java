package fr.riege.ebsl.common.navigation;

import fr.riege.ebsl.common.pathfinding.Node;
import fr.riege.ebsl.common.pathfinding.ProcessedPath;
import fr.riege.ebsl.common.pathfinding.pathing.configuration.PathfinderConfiguration;
import fr.riege.ebsl.common.pathfinding.pathing.result.PathState;
import fr.riege.ebsl.common.pathfinding.pathing.result.PathfinderResult;
import fr.riege.ebsl.common.pathfinding.wrapper.PathPosition;

import java.util.Collection;
import java.util.List;

public record PathPlan(
    PathfinderResult result,
    PathfinderConfiguration configuration,
    List<PathPosition> positions,
    List<Node> rawNodes,
    List<Node> navigationNodes,
    double pathLength
) {
    public static PathPlan empty(PathfinderResult result, PathfinderConfiguration configuration) {
        return new PathPlan(result, configuration, List.of(), List.of(), List.of(), 0.0);
    }

    public static PathPlan from(PathfinderResult result,
                                PathfinderConfiguration configuration,
                                Collection<PathPosition> positions,
                                ProcessedPath processedPath) {
        List<PathPosition> positionList = positions instanceof List<PathPosition> list
            ? List.copyOf(list)
            : List.copyOf(positions);
        if (processedPath == null) {
            return new PathPlan(result, configuration, positionList, List.of(), List.of(), 0.0);
        }
        return new PathPlan(
            result,
            configuration,
            positionList,
            List.copyOf(processedPath.rawNodes()),
            List.copyOf(processedPath.navigationPath()),
            processedPath.pathLength());
    }

    public boolean successful() {
        return result != null && result.successful();
    }

    public boolean usable() {
        return result != null
            && result.getPath() != null
            && (result.successful() || result.hasFallenBack())
            && !positions.isEmpty()
            && result.getPathState() != PathState.ABORTED;
    }

    public boolean complete() {
        return result != null && result.successful() && result.getPathState() == PathState.FOUND;
    }

    public PathPosition start() {
        return positions.isEmpty() ? null : positions.getFirst();
    }

    public PathPosition end() {
        return positions.isEmpty() ? null : positions.getLast();
    }
}
