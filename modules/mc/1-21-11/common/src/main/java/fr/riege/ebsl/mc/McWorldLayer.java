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

package fr.riege.ebsl.mc;

import fr.riege.ebsl.common.domain.world.BlockId;
import fr.riege.ebsl.common.math.Vec3d;
import fr.riege.ebsl.common.world.layer.IWorldLayer;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

final class McWorldLayer implements IWorldLayer {
    private final Minecraft client;
    McWorldLayer(Minecraft client) { this.client = client; }

    @Override public BlockId getBlock(int x, int y, int z) {
        Level level = level();
        if (level == null) return BlockId.AIR;
        Identifier id = BuiltInRegistries.BLOCK.getKey(state(level, x, y, z).getBlock());
        return new BlockId(id.getNamespace(), id.getPath());
    }

    @Override public boolean isAir(int x, int y, int z) {
        Level level = level();
        return level == null || state(level, x, y, z).isAir();
    }

    @Override public boolean isSolid(int x, int y, int z) {
        Level level = level();
        return level != null && getBlockHeight(level, x, y, z) >= 0.5 && !isPassable(level, x, y, z);
    }

    @Override public boolean isWater(int x, int y, int z) {
        Level level = level();
        if (level == null) return false;
        FluidState fluid = state(level, x, y, z).getFluidState();
        return !fluid.isEmpty() && fluid.is(FluidTags.WATER);
    }

    @Override public boolean isLava(int x, int y, int z) {
        Level level = level();
        if (level == null) return false;
        FluidState fluid = state(level, x, y, z).getFluidState();
        return !fluid.isEmpty() && fluid.is(FluidTags.LAVA);
    }

    @Override public boolean isDangerous(int x, int y, int z) {
        Level level = level();
        if (level == null) return false;
        Block block = state(level, x, y, z).getBlock();
        return block == Blocks.LAVA
            || block == Blocks.FIRE
            || block == Blocks.SOUL_FIRE
            || block == Blocks.MAGMA_BLOCK
            || block == Blocks.CACTUS
            || block == Blocks.SWEET_BERRY_BUSH
            || block == Blocks.WITHER_ROSE
            || isLava(x, y, z);
    }

    @Override public boolean isClimbable(int x, int y, int z) {
        Level level = level();
        if (level == null) return false;
        Block block = state(level, x, y, z).getBlock();
        return block instanceof LadderBlock || block instanceof VineBlock;
    }

    @Override public boolean isLoaded(int x, int y, int z) {
        Level level = level();
        return level != null && inBuildHeight(level, y) && level.isLoaded(new BlockPos(x, y, z));
    }

    @Override public int getTopSolidY(int x, int z) {
        Level level = level();
        return level == null ? 0 : level.getHeight(Heightmap.Types.MOTION_BLOCKING, x, z);
    }

    @Override public double getBlockHeight(int x, int y, int z) {
        Level level = level();
        return level == null ? 0.0 : getBlockHeight(level, x, y, z);
    }

    @Override public boolean requiresJumpForStep(int x, int y, int z, int moveDx, int moveDz) {
        Level level = level();
        if (level == null) return false;
        BlockState support = state(level, x, y - 1, z);
        return support.getBlock() instanceof StairBlock;
    }

    @Override public boolean hasLineOfSight(Vec3d from, Vec3d to) {
        Level level = level();
        if (level == null) return false;
        var hit = level.clip(new ClipContext(
            new Vec3(from.x(), from.y(), from.z()),
            new Vec3(to.x(), to.y(), to.z()),
            ClipContext.Block.COLLIDER,
            ClipContext.Fluid.NONE,
            CollisionContext.empty()));
        return hit.getType() == HitResult.Type.MISS;
    }

    @Override public boolean canRayTraceBlock(Vec3d from, Vec3d to, int targetX, int targetY, int targetZ) {
        Level level = level();
        if (level == null) return false;
        var hit = level.clip(new ClipContext(
            new Vec3(from.x(), from.y(), from.z()),
            new Vec3(to.x(), to.y(), to.z()),
            ClipContext.Block.OUTLINE,
            ClipContext.Fluid.NONE,
            CollisionContext.empty()));
        if (hit instanceof BlockHitResult blockHit && hit.getType() == HitResult.Type.BLOCK) {
            BlockPos pos = blockHit.getBlockPos();
            return pos.getX() == targetX && pos.getY() == targetY && pos.getZ() == targetZ;
        }
        return IWorldLayer.super.canRayTraceBlock(from, to, targetX, targetY, targetZ);
    }

    @Override public boolean isPartialSupport(int x, int y, int z) {
        Level level = level();
        if (level == null) return false;
        BlockPos support = new BlockPos(x, y - 1, z);
        BlockState supportState = state(level, support.getX(), support.getY(), support.getZ());
        VoxelShape shape = supportState.getCollisionShape(level, support);
        if (shape.isEmpty()) return false;
        return !supportState.isCollisionShapeFullBlock(level, support);
    }

    @Override public boolean isSlime(int x, int y, int z) {
        Level level = level();
        return level != null && state(level, x, y, z).is(Blocks.SLIME_BLOCK);
    }

    private Level level() {
        return client.level;
    }

    private static boolean inBuildHeight(LevelHeightAccessor level, int y) {
        return y >= level.getMinY() && y < level.getMaxY();
    }

    private static BlockState state(Level level, int x, int y, int z) {
        if (!inBuildHeight(level, y)) return Blocks.AIR.defaultBlockState();
        return level.getBlockState(new BlockPos(x, y, z));
    }

    private static double getBlockHeight(Level level, int x, int y, int z) {
        VoxelShape shape = state(level, x, y, z).getCollisionShape(level, new BlockPos(x, y, z), CollisionContext.empty());
        return shape.isEmpty() ? 0.0 : shape.max(Direction.Axis.Y);
    }

    private static boolean isPassable(Level level, int x, int y, int z) {
        BlockState state = state(level, x, y, z);
        if (state.isAir()) {
            return true;
        }
        Block block = state.getBlock();
        if (isKnownPassableBlock(block) || block == Blocks.SNOW || state.is(BlockTags.TRAPDOORS)) {
            return true;
        }

        VoxelShape shape = state.getCollisionShape(level, new BlockPos(x, y, z), CollisionContext.empty());
        return shape.isEmpty();
    }

    private static boolean isKnownPassableBlock(Block block) {
        return block instanceof BushBlock
            || block instanceof TallGrassBlock
            || block instanceof FlowerBlock
            || block instanceof DoublePlantBlock
            || block instanceof TorchBlock
            || block instanceof WallTorchBlock
            || block instanceof LiquidBlock
            || block instanceof SignBlock
            || block instanceof BannerBlock
            || block instanceof WallBannerBlock
            || block instanceof CarpetBlock
            || block instanceof BasePressurePlateBlock
            || block instanceof BaseRailBlock
            || block instanceof ButtonBlock
            || block instanceof VineBlock
            || block instanceof LadderBlock;
    }
}
