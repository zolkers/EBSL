package fr.riege.ebsl.common.pathfinding.goal;

public record GoalNear(int x, int y, int z, double radius) implements Goal {
    public GoalNear {
        GoalValidators.requireNonNegativeFiniteRadius(radius, "GoalNear radius");
    }

    @Override
    public boolean isInGoal(int x, int y, int z) {
        double dx = (double) this.x - x;
        double dy = (double) this.y - y;
        double dz = (double) this.z - z;
        return dx * dx + dy * dy + dz * dz <= radius * radius;
    }

    @Override
    public double heuristic(int x, int y, int z) {
        double dx = (double) this.x - x;
        double dy = (double) this.y - y;
        double dz = (double) this.z - z;
        return Math.max(0.0, Math.sqrt(dx * dx + dy * dy + dz * dz) - radius);
    }

    @Override
    public String debugName() {
        return "GoalNear[" + x + "," + y + "," + z + ",r=" + radius + "]";
    }

    @Override
    public NavigationTarget resolve(int px, int py, int pz) {
        return new NavigationTarget.Block(x, y, z);
    }
}
