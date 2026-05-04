package fr.riege.ebsl.pathfinding.goal;

public record GoalNear(int x, int y, int z, double radius) implements Goal {
    public GoalNear {
        GoalValidators.requireNonNegativeFiniteRadius(radius, "GoalNear radius");
    }

    @Override
    public boolean isInGoal(int x, int y, int z) {
        double dx = this.x - x;
        double dy = this.y - y;
        double dz = this.z - z;
        return Math.sqrt(dx * dx + dy * dy + dz * dz) <= radius;
    }

    @Override
    public double heuristic(int x, int y, int z) {
        double dx = this.x - x;
        double dy = this.y - y;
        double dz = this.z - z;
        return Math.max(0.0, Math.sqrt(dx * dx + dy * dy + dz * dz) - radius);
    }

    @Override
    public String debugName() {
        return "GoalNear[" + x + "," + y + "," + z + ",r=" + radius + "]";
    }
}
