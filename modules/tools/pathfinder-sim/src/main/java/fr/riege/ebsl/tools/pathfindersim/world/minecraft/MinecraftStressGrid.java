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

package fr.riege.ebsl.tools.pathfindersim.world.minecraft;

public record MinecraftStressGrid(
    int radiusX,
    int radiusY,
    int radiusZ,
    int step
) {
    public MinecraftStressGrid {
        radiusX = Math.max(0, radiusX);
        radiusY = Math.max(0, radiusY);
        radiusZ = Math.max(0, radiusZ);
        step = Math.max(1, step);
    }

    public int scenarioCount() {
        return countFor(radiusX) * countFor(radiusY) * countFor(radiusZ);
    }

    private int countFor(int radius) {
        return radius * 2 / step + 1;
    }
}
