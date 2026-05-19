/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 *
 * Copyright (C) 2026 Victor Riegert and EBSL contributors
 *
 * This file is part of EBSL.
 *
 * EBSL is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * EBSL is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public
 * License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with EBSL. If not, see <https://www.gnu.org/licenses/>.
 */

package fr.riege.ebsl.common.pathfinding.movement;

import fr.riege.ebsl.common.domain.world.BlockId;
import fr.riege.ebsl.common.pathfinding.block.BlockBlacklist;
import fr.riege.ebsl.common.pathfinding.util.BlockPosUtil;
import fr.riege.ebsl.common.world.layer.IWorldLayer;
import it.unimi.dsi.fastutil.longs.Long2ByteOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;

public final class WalkabilityChecker implements MovementTerrain {
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
    private final Object cacheLock = new Object();

    public WalkabilityChecker(IWorldLayer world) {
        this.world = world;
        this.flagCache.defaultReturnValue((byte) 0);
        this.topYCache.defaultReturnValue(TOP_Y_NOT_CACHED);
    }

    @Override
    public IWorldLayer world() {
        return world;
    }

    @Override
    public void clearCache() {
        synchronized (cacheLock) {
            flagCache.clear();
            blockCache.clear();
            topYCache.clear();
        }
    }

    @Override
    public boolean isSolid(int x, int y, int z) {
        return (getFlags(x, y, z) & FLAG_SOLID) != 0;
    }

    @Override
    public boolean isPassable(int x, int y, int z) {
        return (getFlags(x, y, z) & FLAG_PASSABLE) != 0;
    }

    @Override
    public boolean isAir(int x, int y, int z) {
        return world.isAir(x, y, z);
    }

    @Override
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

    @Override
    public boolean isDangerous(int x, int y, int z) {
        return (getFlags(x, y, z) & FLAG_DANGEROUS) != 0;
    }

    @Override
    public boolean isWater(int x, int y, int z) {
        return (getFlags(x, y, z) & FLAG_WATER) != 0;
    }

    @Override
    public boolean isClimbable(int x, int y, int z) {
        return (getFlags(x, y, z) & FLAG_CLIMBABLE) != 0;
    }

    @Override
    public boolean hasWalkableTop(int x, int y, int z) {
        if (isBlacklisted(x, y, z)) {
            return false;
        }
        return getTopY(x, y, z) >= 0.5;
    }

    @Override
    public boolean isLowPartialSupport(int x, int y, int z) {
        if (isBlacklisted(x, y, z)) {
            return false;
        }
        double topY = getTopY(x, y, z);
        return topY > 0.0 && topY <= 0.5;
    }

    @Override
    public boolean isFullWallBlock(int x, int y, int z) {
        if (isBlacklisted(x, y, z)) {
            return true;
        }
        return world.isSolid(x, y, z) && world.getBlockHeight(x, y, z) >= 0.95;
    }

    @Override
    public boolean isFullWall(int x, int y, int z) {
        return isSolid(x, y, z) && getTopY(x, y, z) >= 0.95;
    }

    @Override
    public boolean wouldSuffocate(int x, int y, int z) {
        return isSolid(x, y + 1, z) && !isPassable(x, y + 1, z);
    }

    @Override
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

    @Override
    public double getTopY(int x, int y, int z) {
        long key = BlockPosUtil.pack(x, y, z);
        double cached;
        synchronized (cacheLock) {
            cached = topYCache.get(key);
        }
        if (!Double.isNaN(cached)) return cached;
        double topY = Math.clamp(world.getBlockHeight(x, y, z), 0.0, 1.0);
        synchronized (cacheLock) {
            topYCache.put(key, topY);
        }
        return topY;
    }

    @Override
    public boolean canOcclude(int x, int y, int z) {
        return isBlacklisted(x, y, z) || world.isSolid(x, y, z);
    }

    @Override
    public BlockId getBlock(int x, int y, int z) {
        long key = BlockPosUtil.pack(x, y, z);
        BlockId block;
        synchronized (cacheLock) {
            block = blockCache.get(key);
        }
        if (block != null) return block;
        block = world.getBlock(x, y, z);
        synchronized (cacheLock) {
            blockCache.put(key, block);
        }
        return block;
    }

    @Override
    public boolean isBlacklisted(int x, int y, int z) {
        return BlockBlacklist.isBlacklisted(getBlock(x, y, z));
    }

    private byte getFlags(int x, int y, int z) {
        long key = BlockPosUtil.pack(x, y, z);
        byte cached;
        synchronized (cacheLock) {
            cached = flagCache.get(key);
        }
        if ((cached & FLAG_COMPUTED) != 0) {
            return cached;
        }
        byte flags = computeFlags(x, y, z);
        synchronized (cacheLock) {
            flagCache.put(key, (byte) (flags | FLAG_COMPUTED));
        }
        return flags;
    }

    private byte computeFlags(int x, int y, int z) {
        byte flags = 0;
        boolean solid = world.isSolid(x, y, z);
        boolean water = world.isWater(x, y, z);
        boolean air = world.isAir(x, y, z);
        if (solid) flags |= FLAG_SOLID;
        if (air || water || !solid) flags |= FLAG_PASSABLE;
        if (water) flags |= FLAG_WATER;
        if (world.isLava(x, y, z) || world.isDangerous(x, y, z)) flags |= FLAG_DANGEROUS;
        if (world.isClimbable(x, y, z)) flags |= FLAG_CLIMBABLE;
        return flags;
    }
}
