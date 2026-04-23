package fr.riege.ebsl.pathfinding.goal;

public record GoalColumn(int x, int z, double radius) implements Goal {
    @Override
    public boolean isInGoal(int x, int y, int z) {
        double dx = this.x - x;
        double dz = this.z - z;
        return Math.sqrt(dx * dx + dz * dz) <= radius;
    }

    @Override
    public double heuristic(int x, int y, int z) {
        double dx = this.x - x;
        double dz = this.z - z;
        return Math.max(0.0, Math.sqrt(dx * dx + dz * dz) - radius);
    }

    @Override
    public String debugName() {
        return "GoalColumn[" + x + "," + z + ",r=" + radius + "]";
    }
}
