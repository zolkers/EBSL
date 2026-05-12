package fr.riege.ebsl.common.pathfinding.goal;

public record GoalBlock(int x, int y, int z) implements Goal {
    @Override
    public boolean isInGoal(int x, int y, int z) {
        return this.x == x && this.y == y && this.z == z;
    }

    @Override
    public double heuristic(int x, int y, int z) {
        double dx = (double) this.x - x;
        double dy = (double) this.y - y;
        double dz = (double) this.z - z;
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    @Override
    public String debugName() {
        return "GoalBlock[" + x + "," + y + "," + z + "]";
    }

    @Override
    public NavigationTarget resolve(int px, int py, int pz) {
        return new NavigationTarget.Block(x, y, z);
    }
}
