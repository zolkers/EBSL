package fr.riege.ebsl.common.pathfinding.goal;

public record GoalXZ(int x, int z) implements Goal {
    @Override
    public boolean isInGoal(int x, int y, int z) {
        return this.x == x && this.z == z;
    }

    @Override
    public double heuristic(int x, int y, int z) {
        double dx = (double) this.x - x;
        double dz = (double) this.z - z;
        return Math.sqrt(dx * dx + dz * dz);
    }

    @Override
    public String debugName() {
        return "GoalXZ[" + x + "," + z + "]";
    }

    @Override
    public NavigationTarget resolve(int px, int py, int pz) {
        return new NavigationTarget.Column(x, z);
    }
}
