package fr.riege.ebsl.common.pathfinding.goal;

public record GoalAxisZ(int z) implements Goal {
    @Override
    public boolean isInGoal(int x, int y, int z) {
        return this.z == z;
    }

    @Override
    public double heuristic(int x, int y, int z) {
        return Math.abs(this.z - z);
    }

    @Override
    public String debugName() {
        return "GoalAxisZ[" + z + "]";
    }

    @Override
    public NavigationTarget resolve(int px, int py, int pz) {
        return new NavigationTarget.Column(px, z);
    }
}
