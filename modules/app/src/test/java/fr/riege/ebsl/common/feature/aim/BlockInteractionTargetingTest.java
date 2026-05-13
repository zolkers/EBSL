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

package fr.riege.ebsl.common.feature.aim;

import fr.riege.ebsl.common.domain.world.BlockId;
import fr.riege.ebsl.common.math.Vec3d;
import fr.riege.ebsl.common.pathfinding.wrapper.PathPosition;
import fr.riege.ebsl.common.world.layer.IWorldLayer;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class BlockInteractionTargetingTest {
    @Test
    void prefersReachableSideBelowBlockOverStandingOnTop() {
        FakeWorld world = new FakeWorld();
        world.block(0, 65, 0, BlockId.of("minecraft:oak_leaves"));
        world.block(0, 63, 0, BlockId.of("minecraft:stone"));
        world.block(1, 63, 0, BlockId.of("minecraft:stone"));

        PathPosition target = BlockInteractionTargeting.bestStandingPosition(
            world,
            new Vec3d(2.5, 64.0, 0.5),
            0,
            65,
            0,
            4
        );

        assertNotNull(target);
        assertEquals(new PathPosition(1, 64, 0), target);
    }

    private static final class FakeWorld implements IWorldLayer {
        private final Map<String, BlockId> blocks = new HashMap<>();

        void block(int x, int y, int z, BlockId id) {
            blocks.put(key(x, y, z), id);
        }

        @Override
        public BlockId getBlock(int x, int y, int z) {
            return blocks.getOrDefault(key(x, y, z), BlockId.AIR);
        }

        @Override
        public boolean isAir(int x, int y, int z) {
            return getBlock(x, y, z).equals(BlockId.AIR);
        }

        @Override
        public boolean isSolid(int x, int y, int z) {
            return !isAir(x, y, z);
        }

        @Override
        public boolean isWater(int x, int y, int z) {
            return false;
        }

        @Override
        public boolean isLava(int x, int y, int z) {
            return false;
        }

        @Override
        public boolean isLoaded(int x, int y, int z) {
            return true;
        }

        @Override
        public int getTopSolidY(int x, int z) {
            return 64;
        }

        @Override
        public double getBlockHeight(int x, int y, int z) {
            return isSolid(x, y, z) ? 1.0 : 0.0;
        }

        private static String key(int x, int y, int z) {
            return x + "," + y + "," + z;
        }
    }
}
