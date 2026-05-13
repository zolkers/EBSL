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
import fr.riege.ebsl.common.domain.world.TargetedBlock;
import fr.riege.ebsl.common.math.Vec3d;

/**
 * Exposes the local player state needed by navigation, targeting, and rendering code.
 *
 * <p>The layer reports position, rotation, environment state, health, and selected targets without leaking platform entity classes.</p>
 */
public interface IPlayerLayer {
    /**
     * Returns the current world position.
 *
     * @return the value defined by this contract
     */
    Vec3d position();
    /**
     * Returns the current velocity vector.
 *
     * @return the value defined by this contract
     */
    default Vec3d velocity() { return new Vec3d(0.0, 0.0, 0.0); }
    /**
     * Returns the current eye position used for ray casts and aiming.
 *
     * @return the value defined by this contract
     */
    default Vec3d eyePosition() {
        Vec3d position = position();
        return new Vec3d(position.x(), position.y() + 1.62, position.z());
    }
    /**
     * Returns the current yaw angle in degrees.
 *
     * @return the value defined by this contract
     */
    default float yaw() { return 0.0f; }
    /**
     * Returns the current pitch angle in degrees.
 *
     * @return the value defined by this contract
     */
    default float pitch() { return 0.0f; }
    /**
     * Handles the ground lifecycle callback.
 *
     * @return true when the condition is satisfied; false otherwise
     */
    default boolean onGround() { return true; }
    /**
     * Returns whether the player is currently flying.
 *
     * @return true when the condition is satisfied; false otherwise
     */
    default boolean isFlying() { return false; }
    /**
     * Returns whether in water is true for the current state.
 *
     * @return true when the condition is satisfied; false otherwise
     */
    boolean isInWater();
    /**
     * Returns whether in lava is true for the current state.
 *
     * @return true when the condition is satisfied; false otherwise
     */
    boolean isInLava();
    /**
     * Returns whether the player is currently sprinting.
 *
     * @return true when the condition is satisfied; false otherwise
     */
    boolean isSprinting();
    /**
     * Returns whether the player or actor is alive.
 *
     * @return true when the condition is satisfied; false otherwise
     */
    boolean isAlive();
    /**
     * Returns the current health value.
 *
     * @return the value defined by this contract
     */
    float getHealth();
    /**
     * Returns the currently targeted block hit, including hit-position metadata when available.
 *
     * @return the value defined by this contract
     */
    default TargetedBlock targetedBlockHit() { return null; }
    /**
     * Returns the identifier of the currently targeted block, if any.
 *
     * @return the value defined by this contract
     */
    default BlockId targetedBlock() {
        TargetedBlock target = targetedBlockHit();
        return target == null ? null : target.block();
    }
    /**
     * Returns the platform entity identifier when one is available.
 *
     * @return the value defined by this contract
     */
    default Integer entityId() { return null; }
}
