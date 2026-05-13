/*
 * This file is part of EBSL.
 *
 * EBSL is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * EBSL is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package fr.riege.ebsl.common.pathfinding.execution;

import fr.riege.ebsl.common.pathfinding.Node;

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
