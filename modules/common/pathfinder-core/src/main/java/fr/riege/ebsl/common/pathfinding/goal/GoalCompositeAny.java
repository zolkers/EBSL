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
package fr.riege.ebsl.common.pathfinding.goal;

import java.util.List;
import java.util.Objects;

public record GoalCompositeAny(List<Goal> goals) implements Goal {
    public GoalCompositeAny {
        if (goals == null || goals.isEmpty()) {
            throw new IllegalArgumentException("GoalCompositeAny requires at least one goal");
        }
        if (goals.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException("GoalCompositeAny cannot contain null goals");
        }
        goals = List.copyOf(goals);
    }

    @Override
    public boolean isInGoal(int x, int y, int z) {
        for (Goal goal : goals) {
            if (goal.isInGoal(x, y, z)) return true;
        }
        return false;
    }

    @Override
    public double heuristic(int x, int y, int z) {
        double best = Double.POSITIVE_INFINITY;
        for (Goal goal : goals) {
            best = Math.min(best, goal.heuristic(x, y, z));
        }
        return best;
    }

    @Override
    public String debugName() {
        return "GoalCompositeAny[" + goals.size() + "]";
    }

    @Override
    public NavigationTarget resolve(int px, int py, int pz) {
        return new NavigationTarget.Block(px, py, pz);
    }
}
