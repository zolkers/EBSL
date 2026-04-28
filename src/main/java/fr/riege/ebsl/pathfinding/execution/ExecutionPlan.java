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
    Runnable onFinished,
    ExecutionOptions options,
    boolean finishAtPathEnd
) {
    public ExecutionPlan(List<Node> path, int goalX, int goalY, int goalZ,
                         boolean precise, Runnable onFinished) {
        this(path, goalX, goalY, goalZ, precise, onFinished, ExecutionOptions.defaults(), true);
    }

    public ExecutionPlan(List<Node> path, int goalX, int goalY, int goalZ,
                         boolean precise, Runnable onFinished, ExecutionOptions options) {
        this(path, goalX, goalY, goalZ, precise, onFinished, options, true);
    }

    public ExecutionPlan {
        path = path == null ? Collections.emptyList() : List.copyOf(path);
        if (options == null) options = ExecutionOptions.defaults();
    }

    public boolean hasPath() {
        return !path.isEmpty();
    }

    public ExecutionPlan withPath(List<Node> newPath) {
        return new ExecutionPlan(newPath, goalX, goalY, goalZ, precise, onFinished, options, finishAtPathEnd);
    }
}
