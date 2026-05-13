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

package fr.riege.ebsl.common.navigation.runtime.entity;

import fr.riege.ebsl.common.math.Vec3d;

/**
 * Exposes the physical state of an entity controlled by navigation runtime code.
 *
 * <p>Actor snapshots let headless and Minecraft-backed runtimes share follower logic without sharing entity implementations.</p>
 */
public interface NavigationActor {
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
    default Vec3d velocity() {
        return new Vec3d(0.0, 0.0, 0.0);
    }

    /**
     * Handles the ground lifecycle callback.
 *
     * @return true when the condition is satisfied; false otherwise
     */
    default boolean onGround() {
        return true;
    }

    /**
     * Returns whether in water is true for the current state.
 *
     * @return true when the condition is satisfied; false otherwise
     */
    default boolean isInWater() {
        return false;
    }

    /**
     * Returns whether in lava is true for the current state.
 *
     * @return true when the condition is satisfied; false otherwise
     */
    default boolean isInLava() {
        return false;
    }

    /**
     * Returns whether the player or actor is alive.
 *
     * @return true when the condition is satisfied; false otherwise
     */
    default boolean isAlive() {
        return true;
    }

    /**
     * Returns the actor collision width.
 *
     * @return the value defined by this contract
     */
    default double width() {
        return 0.6;
    }

    /**
     * Returns the actor collision height.
 *
     * @return the value defined by this contract
     */
    default double height() {
        return 1.8;
    }
}
