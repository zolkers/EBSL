package fr.riege.ebsl.common.pathfinding;

import java.util.List;

record ProcessedPath(List<Node> rawNodes, List<Node> navigationPath, double pathLength) {
}
