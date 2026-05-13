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
import fr.riege.ebsl.common.world.layer.IWorldLayer;

/**
 * Defines the terrain queries required by movement planning, path quality analysis, path
 * smoothing, and movement execution.
 *
 * <p>This interface represents movement-oriented world semantics rather than raw block access.
 * Implementations translate platform-specific world data into stable questions such as whether a
 * position can be walked through, whether a block has usable support, or whether a fall destination
 * is safe enough for the pathfinder to consider.</p>
 *
 * <p>Callers should depend on this contract when they need movement terrain decisions. Concrete
 * implementations may cache block flags or adapt different world backends without changing the
 * pathfinding and execution layers.</p>
 */
public interface MovementTerrain {
    /**
     * Returns the world layer backing this terrain query object.
     *
     * @return the source world layer
     */
    IWorldLayer world();

    /**
     * Clears any cached terrain state held by the implementation.
     */
    void clearCache();

    /**
     * Returns whether the block at the given coordinates is solid.
     *
     * @param x the block x coordinate
     * @param y the block y coordinate
     * @param z the block z coordinate
     * @return {@code true} when the block is solid
     */
    boolean isSolid(int x, int y, int z);

    /**
     * Returns whether movement can pass through the block at the given coordinates.
     *
     * @param x the block x coordinate
     * @param y the block y coordinate
     * @param z the block z coordinate
     * @return {@code true} when the block can be occupied or crossed
     */
    boolean isPassable(int x, int y, int z);

    /**
     * Returns whether the block at the given coordinates is air.
     *
     * @param x the block x coordinate
     * @param y the block y coordinate
     * @param z the block z coordinate
     * @return {@code true} when the block is air
     */
    boolean isAir(int x, int y, int z);

    /**
     * Returns whether an entity can stand and move at the given feet position.
     *
     * @param x the feet x coordinate
     * @param y the feet y coordinate
     * @param z the feet z coordinate
     * @return {@code true} when the position has passable body space and usable support
     */
    boolean isWalkable(int x, int y, int z);

    /**
     * Returns whether the block at the given coordinates is dangerous for movement.
     *
     * @param x the block x coordinate
     * @param y the block y coordinate
     * @param z the block z coordinate
     * @return {@code true} when the block should be avoided by normal movement
     */
    boolean isDangerous(int x, int y, int z);

    /**
     * Returns whether the block at the given coordinates is water.
     *
     * @param x the block x coordinate
     * @param y the block y coordinate
     * @param z the block z coordinate
     * @return {@code true} when the block is water
     */
    boolean isWater(int x, int y, int z);

    /**
     * Returns whether the block at the given coordinates can be climbed.
     *
     * @param x the block x coordinate
     * @param y the block y coordinate
     * @param z the block z coordinate
     * @return {@code true} when the block supports climbing movement
     */
    boolean isClimbable(int x, int y, int z);

    /**
     * Returns whether the block at the given coordinates exposes a top surface usable as support.
     *
     * @param x the support block x coordinate
     * @param y the support block y coordinate
     * @param z the support block z coordinate
     * @return {@code true} when the block has a sufficiently high walkable top surface
     */
    boolean hasWalkableTop(int x, int y, int z);

    /**
     * Returns whether the block at the given coordinates is a low partial support at feet level.
     *
     * @param x the block x coordinate
     * @param y the block y coordinate
     * @param z the block z coordinate
     * @return {@code true} when the block can act as low support without blocking head space
     */
    boolean isLowPartialSupport(int x, int y, int z);

    /**
     * Returns whether the block should be treated as a full wall for collision decisions.
     *
     * @param x the block x coordinate
     * @param y the block y coordinate
     * @param z the block z coordinate
     * @return {@code true} when the block is effectively a full-height wall
     */
    boolean isFullWallBlock(int x, int y, int z);

    /**
     * Returns whether the block is solid enough to behave like a full wall.
     *
     * @param x the block x coordinate
     * @param y the block y coordinate
     * @param z the block z coordinate
     * @return {@code true} when the block is solid with near-full height
     */
    boolean isFullWall(int x, int y, int z);

    /**
     * Returns whether standing at the given feet position would collide with head space.
     *
     * @param x the feet x coordinate
     * @param y the feet y coordinate
     * @param z the feet z coordinate
     * @return {@code true} when the head block is solid and not passable
     */
    boolean wouldSuffocate(int x, int y, int z);

    /**
     * Returns whether falling from one height to a destination block is acceptable.
     *
     * @param fromY the starting block y coordinate
     * @param toX the destination x coordinate
     * @param toY the destination y coordinate
     * @param toZ the destination z coordinate
     * @return {@code true} when the fall is short enough or mitigated by water
     */
    boolean safeToFall(int fromY, int toX, int toY, int toZ);

    /**
     * Returns the normalized top height of the block at the given coordinates.
     *
     * @param x the block x coordinate
     * @param y the block y coordinate
     * @param z the block z coordinate
     * @return the block top height clamped to the {@code [0.0, 1.0]} range
     */
    double getTopY(int x, int y, int z);

    /**
     * Returns whether the block can occlude movement or visibility checks.
     *
     * @param x the block x coordinate
     * @param y the block y coordinate
     * @param z the block z coordinate
     * @return {@code true} when the block should be treated as occluding
     */
    boolean canOcclude(int x, int y, int z);

    /**
     * Returns the block identifier at the given coordinates.
     *
     * @param x the block x coordinate
     * @param y the block y coordinate
     * @param z the block z coordinate
     * @return the block identifier exposed by the world layer
     */
    BlockId getBlock(int x, int y, int z);

    /**
     * Returns whether the block at the given coordinates is excluded by movement policy.
     *
     * @param x the block x coordinate
     * @param y the block y coordinate
     * @param z the block z coordinate
     * @return {@code true} when the block is blacklisted for pathfinding
     */
    boolean isBlacklisted(int x, int y, int z);
}
