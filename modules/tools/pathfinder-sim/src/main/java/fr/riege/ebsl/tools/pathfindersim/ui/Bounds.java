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

package fr.riege.ebsl.tools.pathfindersim.ui;

import fr.riege.ebsl.tools.pathfindersim.replay.ReplayBlock;
import fr.riege.ebsl.tools.pathfindersim.replay.SimulationTick;

import java.util.List;

record Bounds(double minX, double maxX, double minZ, double maxZ) {
    static Bounds of(List<SimulationTick> ticks, List<ReplayBlock> terrain) {
        double minX = Double.POSITIVE_INFINITY;
        double maxX = Double.NEGATIVE_INFINITY;
        double minZ = Double.POSITIVE_INFINITY;
        double maxZ = Double.NEGATIVE_INFINITY;
        for (SimulationTick tick : ticks) {
            minX = Math.min(minX, tick.position().x());
            maxX = Math.max(maxX, tick.position().x());
            minZ = Math.min(minZ, tick.position().z());
            maxZ = Math.max(maxZ, tick.position().z());
        }
        for (ReplayBlock block : terrain) {
            minX = Math.min(minX, block.x());
            maxX = Math.max(maxX, block.x() + 1.0);
            minZ = Math.min(minZ, block.z());
            maxZ = Math.max(maxZ, block.z() + 1.0);
        }
        if (!Double.isFinite(minX)) {
            return new Bounds(0.0, 1.0, 0.0, 1.0);
        }
        return new Bounds(minX - 1.0, maxX + 1.0, minZ - 1.0, maxZ + 1.0);
    }
}
