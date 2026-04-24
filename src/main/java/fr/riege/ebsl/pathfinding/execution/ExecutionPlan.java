package fr.riege.ebsl.pathfinding.execution;

import fr.riege.ebsl.pathfinding.Node;

import java.util.Collections;
import java.util.List;

public record ExecutionPlan(
    List<Node> path,
    int goalX,
    int goalY,
    int goalZ,
    boolean precise,
    Runnable onFinished
) {
    public ExecutionPlan {
        path = path == null ? Collections.emptyList() : List.copyOf(path);
    }

    public boolean hasPath() {
        return !path.isEmpty();
    }

    public ExecutionPlan withPath(List<Node> path) {
        return new ExecutionPlan(path, goalX, goalY, goalZ, precise, onFinished);
    }
}
