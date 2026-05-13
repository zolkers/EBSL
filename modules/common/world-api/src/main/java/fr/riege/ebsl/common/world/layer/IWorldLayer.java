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

package fr.riege.ebsl.common.world.layer;

import fr.riege.ebsl.common.domain.world.BlockId;
import fr.riege.ebsl.common.math.Vec3d;

/**
 * Exposes block and ray information needed by shared world logic.
 *
 * <p>Pathfinding, targeting, and rendering code query this layer for block identity, collision-like traits, support shape, and line of sight.</p>
 */
public interface IWorldLayer {
    /**
     * Returns the block identifier at the supplied world coordinates.
 *
     * @param x the block x coordinate
     * @param y the block y coordinate
     * @param z the block z coordinate
     * @return the value defined by this contract
     */
    BlockId getBlock(int x, int y, int z);
    /**
     * Returns whether the block at the supplied coordinates is air.
 *
     * @param x the block x coordinate
     * @param y the block y coordinate
     * @param z the block z coordinate
     * @return true when the condition is satisfied; false otherwise
     */
    boolean isAir(int x, int y, int z);
    /**
     * Returns whether the block at the supplied coordinates should be treated as solid.
 *
     * @param x the block x coordinate
     * @param y the block y coordinate
     * @param z the block z coordinate
     * @return true when the condition is satisfied; false otherwise
     */
    boolean isSolid(int x, int y, int z);
    /**
     * Returns whether the block at the supplied coordinates is water.
 *
     * @param x the block x coordinate
     * @param y the block y coordinate
     * @param z the block z coordinate
     * @return true when the condition is satisfied; false otherwise
     */
    boolean isWater(int x, int y, int z);
    /**
     * Returns whether the block at the supplied coordinates is lava.
 *
     * @param x the block x coordinate
     * @param y the block y coordinate
     * @param z the block z coordinate
     * @return true when the condition is satisfied; false otherwise
     */
    boolean isLava(int x, int y, int z);
    /**
     * Returns whether the block at the supplied coordinates is dangerous for navigation.
 *
     * @param x the block x coordinate
     * @param y the block y coordinate
     * @param z the block z coordinate
     * @return true when the condition is satisfied; false otherwise
     */
    default boolean isDangerous(int x, int y, int z) { return isLava(x, y, z); }
    /**
     * Returns whether the block at the supplied coordinates can be climbed.
 *
     * @param x the block x coordinate
     * @param y the block y coordinate
     * @param z the block z coordinate
     * @return true when the condition is satisfied; false otherwise
     */
    default boolean isClimbable(int x, int y, int z) { return false; }
    /**
     * Returns whether the block at the supplied coordinates is loaded and safe to query.
 *
     * @param x the block x coordinate
     * @param y the block y coordinate
     * @param z the block z coordinate
     * @return true when the condition is satisfied; false otherwise
     */
    boolean isLoaded(int x, int y, int z);
    /**
     * Returns the highest solid block y coordinate for the supplied column.
 *
     * @param x the block x coordinate
     * @param z the block z coordinate
     * @return the value defined by this contract
     */
    int getTopSolidY(int x, int z);
    /**
     * Returns the collision or support height of the block at the supplied coordinates.
 *
     * @param x the block x coordinate
     * @param y the block y coordinate
     * @param z the block z coordinate
     * @return the value defined by this contract
     */
    double getBlockHeight(int x, int y, int z);
    /**
     * Returns whether stepping onto the target block requires an explicit jump input.
 *
     * @param x the block x coordinate
     * @param y the block y coordinate
     * @param z the block z coordinate
     * @param moveDx the movement delta on the x axis
     * @param moveDz the movement delta on the z axis
     * @return true when the condition is satisfied; false otherwise
     */
    default boolean requiresJumpForStep(int x, int y, int z, int moveDx, int moveDz) { return false; }
    /**
     * Returns whether an unobstructed line of sight exists between two world positions.
 *
     * @param from the first value or starting position
     * @param to the second value or ending position
     * @return true when the condition is satisfied; false otherwise
     */
    default boolean hasLineOfSight(Vec3d from, Vec3d to) { return true; }
    /**
     * Returns whether a ray from the start position can reach the target block before hitting another solid block.
 *
     * @param from the first value or starting position
     * @param to the second value or ending position
     * @param targetX the target x value
     * @param targetY the target y value
     * @param targetZ the target z value
     * @return true when the condition is satisfied; false otherwise
     */
    default boolean canRayTraceBlock(Vec3d from, Vec3d to, int targetX, int targetY, int targetZ) {
        int steps = Math.max(1, (int) Math.ceil(from.distanceTo(to) * 8.0));
        for (int i = 1; i <= steps; i++) {
            double t = (double) i / steps;
            int x = (int) Math.floor(from.x() + (to.x() - from.x()) * t);
            int y = (int) Math.floor(from.y() + (to.y() - from.y()) * t);
            int z = (int) Math.floor(from.z() + (to.z() - from.z()) * t);
            if (x == targetX && y == targetY && z == targetZ) {
                return true;
            }
            if (isLoaded(x, y, z) && !isAir(x, y, z) && isSolid(x, y, z)) {
                return false;
            }
        }
        return false;
    }
    /**
     * Returns whether the block below the supplied cell offers partial support instead of a full block surface.
 *
     * @param x the block x coordinate
     * @param y the block y coordinate
     * @param z the block z coordinate
     * @return true when the condition is satisfied; false otherwise
     */
    default boolean isPartialSupport(int x, int y, int z) {
        double top = getBlockHeight(x, y - 1, z);
        return top > 0.0 && top < 0.95;
    }
    /**
     * Returns whether the block at the supplied coordinates behaves as slime for movement planning.
 *
     * @param x the block x coordinate
     * @param y the block y coordinate
     * @param z the block z coordinate
     * @return true when the condition is satisfied; false otherwise
     */
    default boolean isSlime(int x, int y, int z) { return false; }
    /**
     * Returns whether the supplied eye position is currently inside water.
 *
     * @param eyePosition the eye position value
     * @return true when the condition is satisfied; false otherwise
     */
    default boolean isHeadUnderWater(Vec3d eyePosition) {
        return isWater((int) Math.floor(eyePosition.x()), (int) Math.floor(eyePosition.y()), (int) Math.floor(eyePosition.z()));
    }
}
