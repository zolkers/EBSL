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
