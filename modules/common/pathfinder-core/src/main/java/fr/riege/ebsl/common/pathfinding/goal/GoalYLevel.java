package fr.riege.ebsl.common.pathfinding.goal;

public record GoalYLevel(int y) implements Goal {
    @Override
    public boolean isInGoal(int x, int y, int z) {
        return this.y == y;
    }

    @Override
    public double heuristic(int x, int y, int z) {
        return Math.abs(this.y - y);
    }

    @Override
    public String debugName() {
        return "GoalYLevel[" + y + "]";
    }

    @Override
    public NavigationTarget resolve(int px, int py, int pz) {
        return new NavigationTarget.Block(px, y, pz);
    }
}
