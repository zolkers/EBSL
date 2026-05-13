/*
 * This file is part of EBSL.
 *
 * EBSL is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * EBSL is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package fr.riege.ebsl.common.pathfinding.provider;

import fr.riege.ebsl.common.pathfinding.movement.MovementTerrain;
import fr.riege.ebsl.common.pathfinding.pathing.context.EnvironmentContext;
import fr.riege.ebsl.common.pathfinding.util.BlockPosUtil;
import fr.riege.ebsl.common.pathfinding.wrapper.PathPosition;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;

final class LayerNavigationPointProvider implements WorldNavigationPointProvider {
    private static final NavigationPoint BLOCKED = NavigationPoints.blocked();

    private final MovementTerrain checker;
    private final Long2ObjectOpenHashMap<NavigationPoint> navPointCache = new Long2ObjectOpenHashMap<>(512);

    LayerNavigationPointProvider(MovementTerrain checker) {
        this.checker = checker;
    }

    @Override
    public MovementTerrain checker() {
        return checker;
    }

    @Override
    public void clearCache() {
        navPointCache.clear();
    }

    @Override
    public NavigationPoint getNavigationPoint(PathPosition position, EnvironmentContext environmentContext) {
        int x = position.flooredX();
        int y = position.flooredY();
        int z = position.flooredZ();
        long key = BlockPosUtil.pack(x, y, z);
        NavigationPoint cached = navPointCache.get(key);
        if (cached != null) {
            return cached;
        }

        if (!checker.world().isLoaded(x, y, z)) {
            return BLOCKED;
        }
        if (checker.isBlacklisted(x, y - 1, z) || checker.isBlacklisted(x, y, z) || checker.isBlacklisted(x, y + 1, z)) {
            navPointCache.put(key, BLOCKED);
            return BLOCKED;
        }

        boolean lowPartialFeet = checker.isLowPartialSupport(x, y, z);
        boolean canPassFeet = lowPartialFeet || checker.isPassable(x, y, z);
        boolean canPassHead = checker.isPassable(x, y + 1, z);
        boolean liquid = checker.isWater(x, y, z);
        boolean climbable = checker.isClimbable(x, y, z);
        boolean floor = lowPartialFeet || liquid || checker.hasWalkableTop(x, y - 1, z);
        boolean dangerous = checker.isDangerous(x, y, z) || checker.isDangerous(x, y + 1, z);
        double floorLevel = floorLevel(x, y, z, liquid, lowPartialFeet);

        NavigationPoint point = NavigationPoints.of(canPassFeet && canPassHead && !dangerous, floor, floorLevel, climbable, liquid);
        navPointCache.put(key, point);
        return point;
    }

    private double floorLevel(int x, int y, int z, boolean liquid, boolean lowPartialFeet) {
        if (liquid) return y + 0.5;
        if (lowPartialFeet) return y + checker.getTopY(x, y, z);
        double belowTop = checker.getTopY(x, y - 1, z);
        return belowTop <= 0.0 ? y - 1.0 : y - 1.0 + belowTop;
    }

}
