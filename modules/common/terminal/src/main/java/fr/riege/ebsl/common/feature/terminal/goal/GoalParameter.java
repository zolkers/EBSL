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

import fr.riege.ebsl.common.feature.terminal.CommandParameter;
import fr.riege.ebsl.common.pathfinding.goal.GoalContext;
import fr.riege.ebsl.common.pathfinding.goal.GoalParameterSpec;
import fr.riege.ebsl.common.world.layer.IPlayerLayer;

public record GoalParameter(CommandParameter parameter) {
    public GoalParameter {
        if (parameter == null) {
            throw new IllegalArgumentException("command parameter required");
        }
    }

    public String id() {
        return parameter.id();
    }

    public String label() {
        return parameter.label();
    }

    public int defaultValue(IPlayerLayer player) {
        return parameter.defaultValue(player);
    }

    public CommandParameter commandParameter() {
        return parameter;
    }

    public static GoalParameter constant(String id, String label, int value) {
        return new GoalParameter(CommandParameter.constant(id, label, value));
    }

    public static GoalParameter from(GoalParameterSpec spec) {
        return new GoalParameter(new CommandParameter(spec.id(), spec.label(), player -> spec.defaultValue(context(player))));
    }

    public static GoalParameter currentX() {
        return new GoalParameter(CommandParameter.currentX());
    }

    public static GoalParameter currentY() {
        return new GoalParameter(CommandParameter.currentY());
    }

    public static GoalParameter currentZ() {
        return new GoalParameter(CommandParameter.currentZ());
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
