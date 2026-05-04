package fr.riege.ebsl.pathfinding.goal;

public record GoalBlock(int x, int y, int z) implements Goal {
    @Override
    public boolean isInGoal(int x, int y, int z) {
        return this.x == x && this.y == y && this.z == z;
    }

    @Override
    public double heuristic(int x, int y, int z) {
        double dx = this.x - x;
        double dy = this.y - y;
        double dz = this.z - z;
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    @Override
    public String debugName() {
        return "GoalBlock[" + x + "," + y + "," + z + "]";
    }
}
