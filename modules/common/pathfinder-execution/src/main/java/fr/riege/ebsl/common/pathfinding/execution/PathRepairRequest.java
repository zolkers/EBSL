package fr.riege.ebsl.common.pathfinding.execution;

import fr.riege.ebsl.common.pathfinding.Node;

import java.util.List;

public record PathRepairRequest(Node joinNode, List<Node> remainingPath, String reason,
                                int goalX, int goalY, int goalZ) {
    public PathRepairRequest {
        remainingPath = remainingPath == null ? List.of() : List.copyOf(remainingPath);
    }
}
