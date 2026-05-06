package fr.riege.ebsl.common.pathfinding.goal;

public record GoalChunk(int chunkX, int chunkZ) implements Goal {
    @Override
    public boolean isInGoal(int x, int y, int z) {
        return (x >> 4) == chunkX && (z >> 4) == chunkZ;
    }

    @Override
    public double heuristic(int x, int y, int z) {
        return asRectangle().heuristic(x, y, z);
    }

    @Override
    public String debugName() {
        return "GoalChunk[" + chunkX + "," + chunkZ + "]";
    }

    public GoalRectangleXZ asRectangle() {
        int minX = chunkX << 4;
        int minZ = chunkZ << 4;
        return new GoalRectangleXZ(minX, minZ, minX + 15, minZ + 15);
    }
}
