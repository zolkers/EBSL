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

package fr.riege.ebsl.common.pathfinding.rotation;

import fr.riege.ebsl.common.world.layer.IPlayerLayer;

/**
 * Computes camera rotation updates toward a target orientation.
 *
 * <p>Strategies may keep internal interpolation state between frames and receive explicit start/stop lifecycle callbacks.</p>
 */
public interface IRotationStrategy {
    /**
     * Handles the rotate lifecycle callback.
 *
     * @param player the player abstraction used for the calculation
     * @param targetYaw the yaw angle to rotate toward
     * @param targetPitch the pitch angle to rotate toward
     * @return the value defined by this contract
     */
    Rotation onRotate(IPlayerLayer player, float targetYaw, float targetPitch);

    /**
     * Initializes strategy state when a rotation sequence starts.
 *
     * @param player the player abstraction used for the calculation
     */
    default void onStart(IPlayerLayer player) {
    }

    /**
     * Clears strategy state when a rotation sequence stops.
     */
    default void onStop() {
    }
}
