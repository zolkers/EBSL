package fr.riege.ebsl.common.pathfinding.goal;

public record GoalColumn(int x, int z, double radius) implements Goal {
    public GoalColumn {
        GoalValidators.requireNonNegativeFiniteRadius(radius, "GoalColumn radius");
    }

    @Override
    public boolean isInGoal(int x, int y, int z) {
        double dx = (double) this.x - x;
        double dz = (double) this.z - z;
        return Math.sqrt(dx * dx + dz * dz) <= radius;
    }

    @Override
    public double heuristic(int x, int y, int z) {
        double dx = (double) this.x - x;
        double dz = (double) this.z - z;
        return Math.max(0.0, Math.sqrt(dx * dx + dz * dz) - radius);
    }

    @Override
    public String debugName() {
        return "GoalColumn[" + x + "," + z + ",r=" + radius + "]";
    }

    @Override
    public NavigationTarget resolve(int px, int py, int pz) {
        return new NavigationTarget.Column(x, z);
    }
}
