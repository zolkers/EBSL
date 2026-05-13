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

package fr.riege.ebsl.common.pathfinding.provider;

import fr.riege.ebsl.common.domain.world.BlockId;
import fr.riege.ebsl.common.pathfinding.movement.WalkabilityChecker;
import fr.riege.ebsl.common.pathfinding.wrapper.PathPosition;
import fr.riege.ebsl.common.world.layer.IWorldLayer;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LayerNavigationPointProviderTest {
    @Test
    void blocksPointWhenFeetAreSolid() {
        FakeWorld world = new FakeWorld();
        world.solid(0, 63, 0);
        world.solid(0, 64, 0);
        WorldNavigationPointProvider provider = NavigationPointProviders.worldBacked(new WalkabilityChecker(world));

        NavigationPoint point = provider.getNavigationPoint(new PathPosition(0, 64, 0), null);

        assertFalse(point.isTraversable());
    }

    @Test
    void blocksPointWhenHeadIsSolid() {
        FakeWorld world = new FakeWorld();
        world.solid(0, 63, 0);
        world.solid(0, 65, 0);
        WorldNavigationPointProvider provider = NavigationPointProviders.worldBacked(new WalkabilityChecker(world));

        NavigationPoint point = provider.getNavigationPoint(new PathPosition(0, 64, 0), null);

        assertFalse(point.isTraversable());
    }

    @Test
    void blocksPointWhenHeadOrSupportIsUnloaded() {
        FakeWorld world = new FakeWorld();
        world.solid(0, 63, 0);
        world.unloaded(0, 65, 0);
        WorldNavigationPointProvider provider = NavigationPointProviders.worldBacked(new WalkabilityChecker(world));

        NavigationPoint point = provider.getNavigationPoint(new PathPosition(0, 64, 0), null);

        assertFalse(point.isTraversable());
    }

    @Test
    void allowsClearSupportedPoint() {
        FakeWorld world = new FakeWorld();
        world.solid(0, 63, 0);
        WorldNavigationPointProvider provider = NavigationPointProviders.worldBacked(new WalkabilityChecker(world));

        NavigationPoint point = provider.getNavigationPoint(new PathPosition(0, 64, 0), null);

        assertTrue(point.isTraversable());
    }

    private static final class FakeWorld implements IWorldLayer {
        private final Set<String> solids = new HashSet<>();
        private final Set<String> unloaded = new HashSet<>();

        void solid(int x, int y, int z) {
            solids.add(key(x, y, z));
        }

        void unloaded(int x, int y, int z) {
            unloaded.add(key(x, y, z));
        }

        @Override public BlockId getBlock(int x, int y, int z) {
            return isSolid(x, y, z) ? new BlockId("test", "solid") : BlockId.AIR;
        }

        @Override public boolean isAir(int x, int y, int z) {
            return !isSolid(x, y, z);
        }

        @Override public boolean isSolid(int x, int y, int z) {
            return solids.contains(key(x, y, z));
        }

        @Override public boolean isWater(int x, int y, int z) {
            return false;
        }

        @Override public boolean isLava(int x, int y, int z) {
            return false;
        }

        @Override public boolean isLoaded(int x, int y, int z) {
            return !unloaded.contains(key(x, y, z));
        }

        @Override public int getTopSolidY(int x, int z) {
            return 63;
        }

        @Override public double getBlockHeight(int x, int y, int z) {
            return isSolid(x, y, z) ? 1.0 : 0.0;
        }

        private static String key(int x, int y, int z) {
            return x + "," + y + "," + z;
        }
    }
}
