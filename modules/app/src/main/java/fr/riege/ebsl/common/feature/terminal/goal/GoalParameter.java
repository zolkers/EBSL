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
package fr.riege.ebsl.common.feature.terminal.goal;

import fr.riege.ebsl.common.world.layer.IPlayerLayer;

public record GoalParameter(String id, String label, DefaultProvider defaultProvider) {
    public int defaultValue(IPlayerLayer player) {
        return defaultProvider.value(player);
    }

    /**
     * Defines the default provider contract.

     *

     * <p>Implementations provide the stable boundary used by EBSL components that depend on default provider behavior.</p>

     */
    @FunctionalInterface
    public interface DefaultProvider {
        /**
         * Returns the default value for the current player state.
 *
         * @param player the player abstraction used for the calculation
         * @return the value defined by this contract
         */
        int value(IPlayerLayer player);
    }

    public static GoalParameter constant(String id, String label, int value) {
        return new GoalParameter(id, label, player -> value);
    }

    public static GoalParameter currentX() {
        return new GoalParameter("x", "X", p -> (int) Math.floor(p.position().x()));
    }

    public static GoalParameter currentY() {
        return new GoalParameter("y", "Y", p -> (int) Math.floor(p.position().y()));
    }

    public static GoalParameter currentZ() {
        return new GoalParameter("z", "Z", p -> (int) Math.floor(p.position().z()));
    }
}
