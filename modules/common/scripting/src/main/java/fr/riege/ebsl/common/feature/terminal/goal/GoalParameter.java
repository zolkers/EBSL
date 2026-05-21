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

package fr.riege.ebsl.common.feature.terminal.goal;

import fr.riege.ebsl.common.pathfinding.goal.GoalContext;
import fr.riege.ebsl.common.pathfinding.goal.GoalParameterSpec;
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

    public static GoalParameter from(GoalParameterSpec spec) {
        return new GoalParameter(spec.id(), spec.label(), player -> spec.defaultValue(context(player)));
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

    private static GoalContext context(IPlayerLayer player) {
        if (player == null) {
            return new GoalContext(0, 0, 0);
        }
        return new GoalContext(
            (int) Math.floor(player.position().x()),
            (int) Math.floor(player.position().y()),
            (int) Math.floor(player.position().z()));
    }
}
