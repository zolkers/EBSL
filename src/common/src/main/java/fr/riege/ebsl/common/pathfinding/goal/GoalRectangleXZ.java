package fr.riege.ebsl.common.pathfinding.goal;

public record GoalRectangleXZ(int minX, int minZ, int maxX, int maxZ) implements Goal {
    public GoalRectangleXZ {
        if (minX > maxX || minZ > maxZ) {
            throw new IllegalArgumentException("Invalid rectangle bounds");
        }
    }

    @Override
    public boolean isInGoal(int x, int y, int z) {
        return x >= minX && x <= maxX && z >= minZ && z <= maxZ;
    }

    @Override
    public double heuristic(int x, int y, int z) {
        double dx = axisDistance(x, minX, maxX);
        double dz = axisDistance(z, minZ, maxZ);
        return Math.sqrt(dx * dx + dz * dz);
    }

    @Override
    public String debugName() {
        return "GoalRectangleXZ[" + minX + "," + minZ + " -> " + maxX + "," + maxZ + "]";
    }

    private static double axisDistance(int value, int min, int max) {
        if (value < min) {
            return min - value;
        }
        if (value > max) {
            return value - max;
        }
        return 0.0;
    }
}
