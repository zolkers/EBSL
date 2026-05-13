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

package fr.riege.ebsl.common.pathfinding.movement.types.execution;

/**
 * Executes movement-type-specific input behavior.
 *
 * <p>Executors translate classified movement contexts into jump, water, and other control actions during path following.</p>
 */
public interface MovementExecutor {
    /**
     * Handles jump input for the supplied movement execution context.
 *
     * @param context the context describing the operation being performed
     */
    default void handleJump(MovementExecutionContext context) {
        context.releaseJump();
    }

    /**
     * Handles water movement input for the supplied water movement context.
 *
     * @param context the context describing the operation being performed
     */
    default void handleWaterMovement(WaterMovementContext context) {
    }
}
