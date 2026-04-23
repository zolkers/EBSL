package fr.riege.ebsl.pathfinding.goal;

import java.util.List;

public record GoalCompositeAny(List<Goal> goals) implements Goal {
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
}
