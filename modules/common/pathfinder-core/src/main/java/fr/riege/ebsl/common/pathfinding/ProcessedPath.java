package fr.riege.ebsl.common.pathfinding;

import java.util.List;

public record ProcessedPath(List<Node> rawNodes, List<Node> navigationPath, double pathLength) {
}
