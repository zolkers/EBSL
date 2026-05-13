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

package fr.riege.ebsl.common.feature.aim;

import fr.riege.ebsl.common.domain.world.BlockId;
import fr.riege.ebsl.common.math.Vec3d;
import fr.riege.ebsl.common.world.layer.IWorldLayer;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BlockAimTargetingTest {
    @Test
    void matchesBlockGroups() {
        assertTrue(BlockAimTargeting.matches(BlockId.of("minecraft:oak_leaves"), "leaf"));
        assertTrue(BlockAimTargeting.matches(BlockId.of("minecraft:birch_log"), "wood"));
        assertTrue(BlockAimTargeting.matches(BlockId.of("minecraft:crimson_stem"), "wood"));
        assertTrue(BlockAimTargeting.matches(BlockId.of("minecraft:tall_grass"), "grass"));
        assertTrue(BlockAimTargeting.matches(BlockId.of("minecraft:birch_log"), "leaf|wood"));
        assertFalse(BlockAimTargeting.matches(BlockId.of("minecraft:crimson_stem"), "wood&!crimson_stem"));
        assertFalse(BlockAimTargeting.matches(BlockId.of("minecraft:stone"), "wood"));
    }

    @Test
    void keepsExactAndSuffixMatching() {
        assertTrue(BlockAimTargeting.matches(BlockId.of("minecraft:oak_leaves"), "minecraft:oak_leaves"));
        assertTrue(BlockAimTargeting.matches(BlockId.of("minecraft:oak_leaves"), "leaves"));
        assertFalse(BlockAimTargeting.matches(BlockId.of("minecraft:oak_leaves"), "minecraft:leaves"));
    }

    @Test
    void rayTraceRejectsBlocksHiddenBehindAnotherSolidBlock() {
        FakeWorld world = new FakeWorld();
        world.block(1, 64, 0, BlockId.of("minecraft:oak_log"));
        world.block(2, 64, 0, BlockId.of("minecraft:oak_leaves"));

        assertFalse(world.canRayTraceBlock(new Vec3d(0.5, 64.5, 0.5), new Vec3d(2.5, 64.5, 0.5), 2, 64, 0));
        assertTrue(world.canRayTraceBlock(new Vec3d(0.5, 64.5, 0.5), new Vec3d(1.5, 64.5, 0.5), 1, 64, 0));
    }

    private static final class FakeWorld implements IWorldLayer {
        private final Map<String, BlockId> blocks = new HashMap<>();

        void block(int x, int y, int z, BlockId id) {
            blocks.put(key(x, y, z), id);
        }

        @Override public BlockId getBlock(int x, int y, int z) {
            return blocks.getOrDefault(key(x, y, z), BlockId.AIR);
        }

        @Override public boolean isAir(int x, int y, int z) {
            return getBlock(x, y, z).equals(BlockId.AIR);
        }

        @Override public boolean isSolid(int x, int y, int z) {
            return !isAir(x, y, z);
        }

        @Override public boolean isWater(int x, int y, int z) {
            return false;
        }

        @Override public boolean isLava(int x, int y, int z) {
            return false;
        }

        @Override public boolean isLoaded(int x, int y, int z) {
            return true;
        }

        @Override public int getTopSolidY(int x, int z) {
            return 64;
        }

        @Override public double getBlockHeight(int x, int y, int z) {
            return isSolid(x, y, z) ? 1.0 : 0.0;
        }

        private static String key(int x, int y, int z) {
            return x + "," + y + "," + z;
        }
    }
}
