package fr.riege.ebsl.common.pathfinding.goal;

public record GoalAxisX(int x) implements Goal {
    @Override
    public boolean isInGoal(int x, int y, int z) {
        return this.x == x;
    }

    @Override
    public double heuristic(int x, int y, int z) {
        return Math.abs(this.x - x);
    }

    @Override
    public String debugName() {
        return "GoalAxisX[" + x + "]";
    }

    @Override
    public NavigationTarget resolve(int px, int py, int pz) {
        return new NavigationTarget.Column(x, pz);
    }
}
