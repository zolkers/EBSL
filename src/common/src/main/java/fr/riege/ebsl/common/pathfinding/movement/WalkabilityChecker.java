package fr.riege.ebsl.common.pathfinding.movement;

import fr.riege.ebsl.common.layer.IWorldLayer;
import fr.riege.ebsl.common.module.blacklist.BlockBlacklist;
import fr.riege.ebsl.common.pathfinding.util.BlockPosUtil;
import fr.riege.ebsl.common.world.BlockId;
import it.unimi.dsi.fastutil.longs.Long2ByteOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;

public final class WalkabilityChecker {
    private static final byte FLAG_SOLID = 0x01;
    private static final byte FLAG_PASSABLE = 0x02;
    private static final byte FLAG_DANGEROUS = 0x04;
    private static final byte FLAG_WATER = 0x08;
    private static final byte FLAG_CLIMBABLE = 0x10;
    private static final byte FLAG_COMPUTED = (byte) 0x80;
    private static final double TOP_Y_NOT_CACHED = Double.NaN;

    private final IWorldLayer world;
    private final Long2ByteOpenHashMap flagCache = new Long2ByteOpenHashMap(8192);
    private final Long2ObjectOpenHashMap<BlockId> blockCache = new Long2ObjectOpenHashMap<>(8192);
    private final Long2DoubleOpenHashMap topYCache = new Long2DoubleOpenHashMap(8192);

    public WalkabilityChecker(IWorldLayer world) {
        this.world = world;
        this.flagCache.defaultReturnValue((byte) 0);
        this.topYCache.defaultReturnValue(TOP_Y_NOT_CACHED);
    }

    public IWorldLayer world() {
        return world;
    }

    public void clearCache() {
        flagCache.clear();
        blockCache.clear();
        topYCache.clear();
    }

    public boolean isSolid(int x, int y, int z) {
        return (getFlags(x, y, z) & FLAG_SOLID) != 0;
    }

    public boolean isPassable(int x, int y, int z) {
        return (getFlags(x, y, z) & FLAG_PASSABLE) != 0;
    }

    public boolean isAir(int x, int y, int z) {
        return world.isAir(x, y, z);
    }

    public boolean isWalkable(int x, int y, int z) {
        if (isBlacklisted(x, y - 1, z) || isBlacklisted(x, y, z) || isBlacklisted(x, y + 1, z)) {
            return false;
        }
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
        if (isBlacklisted(x, y, z)) {
            return true;
        }
        return world.isSolid(x, y, z) && world.getBlockHeight(x, y, z) >= 0.95;
    }

    public boolean isFullWall(int x, int y, int z) {
        return isSolid(x, y, z) && getTopY(x, y, z) >= 0.95;
    }

    public boolean wouldSuffocate(int x, int y, int z) {
        return isSolid(x, y + 1, z) && !isPassable(x, y + 1, z);
    }

    public boolean safeToFall(int fromY, int toX, int toY, int toZ) {
        int fallDistance = fromY - toY;
        if (isBlacklisted(toX, toY - 1, toZ) || isBlacklisted(toX, toY, toZ)) return false;
        if (fallDistance <= 3) return true;
        if (isWater(toX, toY, toZ)) return true;
        for (int checkY = toY + 1; checkY < fromY; checkY++) {
            if (isWater(toX, checkY, toZ)) return true;
        }
        return false;
    }

    public double getTopY(int x, int y, int z) {
        long key = BlockPosUtil.pack(x, y, z);
        double cached = topYCache.get(key);
        if (!Double.isNaN(cached)) return cached;
        double topY = Math.max(0.0, Math.min(1.0, world.getBlockHeight(x, y, z)));
        topYCache.put(key, topY);
        return topY;
    }

    public boolean canOcclude(int x, int y, int z) {
        return isBlacklisted(x, y, z) || world.isSolid(x, y, z);
    }

    public BlockId getBlock(int x, int y, int z) {
        long key = BlockPosUtil.pack(x, y, z);
        BlockId block = blockCache.get(key);
        if (block != null) return block;
        block = world.getBlock(x, y, z);
        blockCache.put(key, block);
        return block;
    }

    public boolean isBlacklisted(int x, int y, int z) {
        return BlockBlacklist.isBlacklisted(getBlock(x, y, z));
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
        byte flags = 0;
        if (world.isSolid(x, y, z)) flags |= FLAG_SOLID;
        if (world.isAir(x, y, z) || world.isWater(x, y, z) || !world.isSolid(x, y, z)) flags |= FLAG_PASSABLE;
        if (world.isWater(x, y, z)) flags |= FLAG_WATER;
        if (world.isLava(x, y, z) || world.isDangerous(x, y, z)) flags |= FLAG_DANGEROUS;
        if (world.isClimbable(x, y, z)) flags |= FLAG_CLIMBABLE;
        return flags;
    }
}
