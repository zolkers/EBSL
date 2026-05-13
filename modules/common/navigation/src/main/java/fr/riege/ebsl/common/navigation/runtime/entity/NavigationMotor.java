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

package fr.riege.ebsl.common.navigation.runtime.entity;

/**
 * Applies movement intents produced by navigation runtime code.
 *
 * <p>Motors translate high-level intent into platform-specific key, velocity, or simulation commands.</p>
 */
public interface NavigationMotor {
    /**
     * Applies this operation to the supplied value or context.
 *
     * @param intent the intent value
     */
    void apply(MovementIntent intent);

    /**
     * Stops the active operation and releases active controls when appropriate.
     */
    default void stop() {
        apply(MovementIntent.stop());
    }
}
