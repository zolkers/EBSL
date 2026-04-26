package fr.riege.ebsl.pathfinding.movement;

import fr.riege.ebsl.botting.module.BlockBlacklist;
import fr.riege.ebsl.pathfinding.util.BlockPosUtil;
import it.unimi.dsi.fastutil.longs.Long2ByteOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class WalkabilityChecker {

    private static final byte FLAG_SOLID     = 0x01;
    private static final byte FLAG_PASSABLE  = 0x02;
    private static final byte FLAG_DANGEROUS = 0x04;
    private static final byte FLAG_WATER     = 0x08;
    private static final byte FLAG_CLIMBABLE = 0x10;
    private static final byte FLAG_COMPUTED  = (byte) 0x80;

    private static final double TOP_Y_NOT_CACHED = Double.NaN;

    private final Level level;
    private final Long2ByteOpenHashMap flagCache;
    private final Long2ObjectOpenHashMap<BlockState> stateCache;
    private final Long2DoubleOpenHashMap topYCache;
    private final Long2ByteOpenHashMap fullWallCache;
    private final MutableBlockPos mutablePos = new MutableBlockPos();

    public WalkabilityChecker(Level level) {
        this.level = level;
        this.flagCache = new Long2ByteOpenHashMap(8192);
        this.flagCache.defaultReturnValue((byte) 0);
        this.stateCache = new Long2ObjectOpenHashMap<>(8192);
        this.topYCache = new Long2DoubleOpenHashMap(8192);
        this.topYCache.defaultReturnValue(TOP_Y_NOT_CACHED);
        this.fullWallCache = new Long2ByteOpenHashMap(8192);
        this.fullWallCache.defaultReturnValue((byte) 0);
    }

    public void clearCache() {
        flagCache.clear();
        stateCache.clear();
        topYCache.clear();
        fullWallCache.clear();
    }

    public Level getLevel() {
        return level;
    }

    public boolean isSolid(int x, int y, int z) {
        return (getFlags(x, y, z) & FLAG_SOLID) != 0;
    }

    public boolean isPassable(int x, int y, int z) {
        return (getFlags(x, y, z) & FLAG_PASSABLE) != 0;
    }

    public boolean isAir(int x, int y, int z) {
        return getState(x, y, z).isAir();
    }

    public boolean isWalkable(int x, int y, int z) {
        boolean lowPartialFeet = isLowPartialSupport(x, y, z);
        return (isPassable(x, y, z) || lowPartialFeet)
                && isPassable(x, y + 1, z)
                && (hasWalkableTop(x, y - 1, z) || lowPartialFeet)
                && !isDangerous(x, y, z)
                && !isDangerous(x, y + 1, z);
    }

    public boolean isDangerous(int x, int y, int z) {
        return (getFlags(x, y, z) & FLAG_DANGEROUS) != 0;
    }

    public boolean isWater(int x, int y, int z) {
        return (getFlags(x, y, z) & FLAG_WATER) != 0;
    }

    public boolean isClimbable(int x, int y, int z) {
        return (getFlags(x, y, z) & FLAG_CLIMBABLE) != 0;
    }

    public boolean hasWalkableTop(int x, int y, int z) {
        if (isBlacklisted(x, y, z)) {
            return false;
        }
        return getTopY(x, y, z) >= 0.5;
    }

    public boolean isLowPartialSupport(int x, int y, int z) {
        if (isBlacklisted(x, y, z)) {
            return false;
        }
        double topY = getTopY(x, y, z);
        return topY > 0.0 && topY <= 0.5;
    }

    public boolean isFullWallBlock(int x, int y, int z) {
        long key = BlockPosUtil.pack(x, y, z);
        byte cached = fullWallCache.get(key);
        if (cached != 0) return cached == 2;

        BlockState state = getState(x, y, z);
        if (isBlacklisted(state)) {
            fullWallCache.put(key, (byte) 2);
            return true;
        }
        boolean result = state.canOcclude() && state.isCollisionShapeFullBlock(level, mutablePos.set(x, y, z));
        fullWallCache.put(key, result ? (byte) 2 : (byte) 1);
        return result;
    }

    public boolean isFullWall(int x, int y, int z) {
        if (!isSolid(x, y, z)) return false;
        return getTopY(x, y, z) >= 0.95;
    }

    public boolean wouldSuffocate(int x, int y, int z) {
        return isSolid(x, y + 1, z) && !isPassable(x, y + 1, z);
    }

    public boolean safeToFall(int fromY, int toX, int toY, int toZ) {
        int fallDistance = fromY - toY;
        if (fallDistance <= 3) return true;
        // Water at the landing position breaks the fall safely
        if (isWater(toX, toY, toZ)) return true;
        // Check if there is water anywhere along the fall path
        for (int checkY = toY + 1; checkY < fromY; checkY++) {
            if (isWater(toX, checkY, toZ)) return true;
        }
        return false;
    }

    public double getTopY(int x, int y, int z) {
        long key = BlockPosUtil.pack(x, y, z);
        double cached = topYCache.get(key);
        if (!Double.isNaN(cached)) return cached;

        BlockState state = getState(x, y, z);
        VoxelShape shape = state.getCollisionShape(level, mutablePos.set(x, y, z));
        double topY = shape.isEmpty() ? 0.0 : shape.bounds().maxY;
        topYCache.put(key, topY);
        return topY;
    }

    public boolean canOcclude(int x, int y, int z) {
        BlockState state = getState(x, y, z);
        return isBlacklisted(state) || state.canOcclude();
    }

    public BlockState getState(int x, int y, int z) {
        long key = BlockPosUtil.pack(x, y, z);
        BlockState state = stateCache.get(key);
        if (state != null) return state;
        state = level.getBlockState(mutablePos.set(x, y, z));
        stateCache.put(key, state);
        return state;
    }

    private byte getFlags(int x, int y, int z) {
        long key = BlockPosUtil.pack(x, y, z);
        byte cached = flagCache.get(key);
        if ((cached & FLAG_COMPUTED) != 0) {
            return cached;
        }
        byte flags = computeFlags(x, y, z);
        flagCache.put(key, (byte) (flags | FLAG_COMPUTED));
        return flags;
    }

    private byte computeFlags(int x, int y, int z) {
        BlockState state = getState(x, y, z);
        byte flags = 0;
        if (isBlacklisted(state)) {
            return (byte) (FLAG_SOLID | FLAG_DANGEROUS);
        }

        double topY = getTopY(x, y, z);
        boolean hasSolidShape = topY >= 0.5;
        if (hasSolidShape && !isStatePassable(state, x, y, z)) {
            flags |= FLAG_SOLID;
        }

        if (isStatePassable(state, x, y, z)) {
            flags |= FLAG_PASSABLE;
        }

        FluidState fluidState = state.getFluidState();
        if (!fluidState.isEmpty() && fluidState.getType().isSame(net.minecraft.world.level.material.Fluids.WATER)) {
            flags |= FLAG_WATER;
        }
        if (state.getBlock() instanceof LiquidBlock) {
            Block b = state.getBlock();
            if (b == Blocks.WATER) {
                flags |= FLAG_WATER;
            }
        }

        if (state.getBlock() instanceof LadderBlock || state.getBlock() instanceof VineBlock) {
            flags |= FLAG_CLIMBABLE;
        }

        Block block = state.getBlock();
        if (block == Blocks.LAVA
                || block == Blocks.FIRE
                || block == Blocks.SOUL_FIRE
                || block == Blocks.MAGMA_BLOCK
                || block == Blocks.CACTUS
                || block == Blocks.SWEET_BERRY_BUSH) {
            flags |= FLAG_DANGEROUS;
        }

        FluidState fs = state.getFluidState();
        if (!fs.isEmpty() && fs.getType().isSame(net.minecraft.world.level.material.Fluids.LAVA)) {
            flags |= FLAG_DANGEROUS;
        }

        return flags;
    }

    private boolean isStatePassable(BlockState state, int x, int y, int z) {
        if (isBlacklisted(state)) return false;
        if (state.isAir()) return true;
        Block block = state.getBlock();
        if (block instanceof BushBlock) return true;
        if (block instanceof TallGrassBlock) return true;
        if (block instanceof FlowerBlock) return true;
        if (block instanceof DoublePlantBlock) return true;
        if (block instanceof TorchBlock) return true;
        if (block instanceof WallTorchBlock) return true;
        if (block instanceof LiquidBlock) return true;
        if (block instanceof SignBlock) return true;
        if (block instanceof BannerBlock) return true;
        if (block instanceof WallBannerBlock) return true;
        if (block instanceof CarpetBlock) return true;
        if (block instanceof BasePressurePlateBlock) return true;
        if (block instanceof BaseRailBlock) return true;
        if (block == Blocks.SNOW) return true;
        if (block instanceof ButtonBlock) return true;
        if (block instanceof VineBlock) return true;
        if (block instanceof LadderBlock) return true;

        VoxelShape shape = state.getCollisionShape(level, mutablePos.set(x, y, z));
        return shape.isEmpty();
    }

    private boolean isBlacklisted(int x, int y, int z) {
        return isBlacklisted(getState(x, y, z));
    }

    private static boolean isBlacklisted(BlockState state) {
        return BlockBlacklist.isBlacklisted(state);
    }

}
