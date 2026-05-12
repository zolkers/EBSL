package fr.riege.ebsl.common.navigation.runtime.headless;

import fr.riege.ebsl.common.world.layer.IWorldLayer;
import fr.riege.ebsl.common.math.Vec3d;
import fr.riege.ebsl.common.pathfinding.util.BlockPosUtil;
import fr.riege.ebsl.common.domain.world.BlockId;

import java.util.HashMap;
import java.util.Map;

public final class HeadlessWorldLayer implements IWorldLayer {
    private final Map<Long, HeadlessBlockState> blocks = new HashMap<>();
    private HeadlessBlockState defaultState = HeadlessBlockState.AIR;
    private int minY = -64;
    private int maxY = 320;

    public static HeadlessWorldLayer flat(int y) {
        HeadlessWorldLayer world = new HeadlessWorldLayer();
        world.fill(-32, y, -32, 32, y, 32, HeadlessBlockState.STONE);
        return world;
    }

    public HeadlessWorldLayer defaultState(HeadlessBlockState defaultState) {
        this.defaultState = defaultState == null ? HeadlessBlockState.AIR : defaultState;
        return this;
    }

    public HeadlessWorldLayer heightRange(int minY, int maxY) {
        this.minY = Math.min(minY, maxY);
        this.maxY = Math.max(minY, maxY);
        return this;
    }

    public HeadlessWorldLayer set(int x, int y, int z, HeadlessBlockState state) {
        long key = BlockPosUtil.pack(x, y, z);
        if (state == null || state.isAir()) {
            blocks.remove(key);
        } else {
            blocks.put(key, state);
        }
        return this;
    }

    public HeadlessWorldLayer fill(int minX, int minY, int minZ, int maxX, int maxY, int maxZ, HeadlessBlockState state) {
        for (int x = Math.min(minX, maxX); x <= Math.max(minX, maxX); x++) {
            for (int y = Math.min(minY, maxY); y <= Math.max(minY, maxY); y++) {
                for (int z = Math.min(minZ, maxZ); z <= Math.max(minZ, maxZ); z++) {
                    set(x, y, z, state);
                }
            }
        }
        return this;
    }

    public HeadlessBlockState stateAt(int x, int y, int z) {
        return blocks.getOrDefault(BlockPosUtil.pack(x, y, z), defaultState);
    }

    @Override public BlockId getBlock(int x, int y, int z) {
        return stateAt(x, y, z).id();
    }

    @Override public boolean isAir(int x, int y, int z) {
        return stateAt(x, y, z).isAir();
    }

    @Override public boolean isSolid(int x, int y, int z) {
        return stateAt(x, y, z).solid();
    }

    @Override public boolean isWater(int x, int y, int z) {
        return stateAt(x, y, z).water();
    }

    @Override public boolean isLava(int x, int y, int z) {
        return stateAt(x, y, z).lava();
    }

    @Override public boolean isDangerous(int x, int y, int z) {
        return stateAt(x, y, z).dangerous();
    }

    @Override public boolean isClimbable(int x, int y, int z) {
        return stateAt(x, y, z).climbable();
    }

    @Override public boolean isLoaded(int x, int y, int z) {
        return y >= minY && y <= maxY;
    }

    @Override public int getTopSolidY(int x, int z) {
        for (int y = maxY; y >= minY; y--) {
            if (isSolid(x, y, z)) {
                return y;
            }
        }
        return minY;
    }

    @Override public double getBlockHeight(int x, int y, int z) {
        return stateAt(x, y, z).height();
    }

    @Override public boolean hasLineOfSight(Vec3d from, Vec3d to) {
        int steps = Math.max(1, (int) Math.ceil(from.distanceTo(to) * 2.0));
        for (int i = 1; i < steps; i++) {
            double t = (double) i / steps;
            int x = (int) Math.floor(from.x() + (to.x() - from.x()) * t);
            int y = (int) Math.floor(from.y() + (to.y() - from.y()) * t);
            int z = (int) Math.floor(from.z() + (to.z() - from.z()) * t);
            if (isSolid(x, y, z)) {
                return false;
            }
        }
        return true;
    }
}
