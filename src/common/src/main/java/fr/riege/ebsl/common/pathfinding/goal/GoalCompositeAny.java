package fr.riege.ebsl.common.pathfinding.goal;

import java.util.List;

public record GoalCompositeAny(List<Goal> goals) implements Goal {
    public GoalCompositeAny {
        if (goals == null || goals.isEmpty()) {
            throw new IllegalArgumentException("GoalCompositeAny requires at least one goal");
        }
        if (goals.stream().anyMatch(java.util.Objects::isNull)) {
            throw new IllegalArgumentException("GoalCompositeAny cannot contain null goals");
        }
        goals = List.copyOf(goals);
    }

    @Override
    public boolean isInGoal(int x, int y, int z) {
        return goals.stream().anyMatch(goal -> goal.isInGoal(x, y, z));
    }

    @Override
    public double heuristic(int x, int y, int z) {
        return goals.stream()
            .mapToDouble(goal -> goal.heuristic(x, y, z))
            .min()
            .orElse(Double.POSITIVE_INFINITY);
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
