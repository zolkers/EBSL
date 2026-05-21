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

package fr.riege.ebsl.common.feature.terminal;

import fr.riege.ebsl.common.world.layer.IPlayerLayer;

public record CommandParameter(String id, String label, DefaultProvider defaultProvider) {
    public CommandParameter {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("parameter id required");
        }
        label = label == null || label.isBlank() ? id : label;
        defaultProvider = defaultProvider != null ? defaultProvider : player -> 0;
    }

    public int defaultValue(IPlayerLayer player) {
        return defaultProvider.defaultValue(player);
    }

    public static CommandParameter constant(String id, String label, int value) {
        return new CommandParameter(id, label, player -> value);
    }

    public static CommandParameter currentX() {
        return new CommandParameter("x", "X", player -> (int) Math.floor(player.position().x()));
    }

    public static CommandParameter currentY() {
        return new CommandParameter("y", "Y", player -> (int) Math.floor(player.position().y()));
    }

    public static CommandParameter currentZ() {
        return new CommandParameter("z", "Z", player -> (int) Math.floor(player.position().z()));
    }

    @FunctionalInterface
    public interface DefaultProvider {
        int defaultValue(IPlayerLayer player);
    }
}
