package fr.riege.ebsl.common.pathfinding.goal;

public record GoalGetToBlock(int x, int y, int z) implements Goal {
    @Override
    public boolean isInGoal(int x, int y, int z) {
        int dx = Math.abs(this.x - x);
        int dy = Math.abs((this.y + 1) - y);
        int dz = Math.abs(this.z - z);
        return dx <= 1 && dz <= 1 && dy <= 1;
    }

    @Override
    public double heuristic(int x, int y, int z) {
        double dx = Math.max(0, Math.abs(this.x - x) - 1);
        double dy = Math.abs((this.y + 1) - y);
        double dz = Math.max(0, Math.abs(this.z - z) - 1);
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    @Override
    public String debugName() {
        return "GoalGetToBlock[" + x + "," + y + "," + z + "]";
    }
}
