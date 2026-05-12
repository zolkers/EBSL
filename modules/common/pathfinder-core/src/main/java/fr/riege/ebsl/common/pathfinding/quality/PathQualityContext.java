package fr.riege.ebsl.common.pathfinding.quality;

import fr.riege.ebsl.common.pathfinding.Node;
import fr.riege.ebsl.common.pathfinding.pathing.configuration.PathfinderConfiguration;
import fr.riege.ebsl.common.pathfinding.pathing.result.PathfinderResult;
import fr.riege.ebsl.common.pathfinding.wrapper.PathPosition;

import java.util.Collection;
import java.util.List;

public record PathQualityContext(
    PathfinderResult result,
    PathfinderConfiguration configuration,
    List<PathPosition> positions,
    List<Node> rawNodes,
    List<Node> navigationNodes,
    double pathLength
) {
    public PathQualityContext {
        positions = positions == null ? List.of() : List.copyOf(positions);
        rawNodes = rawNodes == null ? List.of() : List.copyOf(rawNodes);
        navigationNodes = navigationNodes == null ? List.of() : List.copyOf(navigationNodes);
        pathLength = pathLength > 0.0 ? pathLength : directPathLength(positions);
    }

    public static PathQualityContext of(PathfinderResult result, PathfinderConfiguration configuration,
                                        Collection<PathPosition> positions) {
        List<PathPosition> positionList = positions == null ? List.of() : List.copyOf(positions);
        return new PathQualityContext(result, configuration, positionList, List.of(), List.of(), directPathLength(positionList));
    }

    public PathPosition start() {
        return positions.isEmpty() ? null : positions.getFirst();
    }

    public PathPosition end() {
        return positions.isEmpty() ? null : positions.getLast();
    }

    private static double directPathLength(List<PathPosition> positions) {
        if (positions == null || positions.size() < 2) {
            return 0.0;
        }
        double total = 0.0;
        for (int i = 1; i < positions.size(); i++) {
            total += positions.get(i - 1).distance(positions.get(i));
        }
        return total;
    }
}
