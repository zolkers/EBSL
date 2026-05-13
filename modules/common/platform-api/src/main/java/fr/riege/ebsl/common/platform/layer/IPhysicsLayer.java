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

package fr.riege.ebsl.common.platform.layer;

/**
 * Applies low-level player physics adjustments exposed by the platform.
 *
 * <p>Shared code uses this layer for rotation control and mouse-step quantization without depending on Minecraft internals.</p>
 */
public interface IPhysicsLayer {
    /**
     * Applies the supplied yaw and pitch to the controlled player or camera.
 *
     * @param yaw the yaw value
     * @param pitch the pitch value
     */
    default void setRotation(float yaw, float pitch) {
    }

    /**
     * Returns the platform rotation quantum used to mimic valid mouse movement increments.
 *
     * @return the value defined by this contract
     */
    default double rotationGcd() {
        double sensitivity = 0.5;
        double f = sensitivity * 0.6 + 0.2;
        return f * f * f * 1.2;
    }
}
