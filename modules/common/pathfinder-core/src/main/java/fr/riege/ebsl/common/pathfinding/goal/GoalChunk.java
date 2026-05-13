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

package fr.riege.ebsl.common.pathfinding.goal;

public record GoalChunk(int chunkX, int chunkZ) implements Goal {
    @Override
    public boolean isInGoal(int x, int y, int z) {
        return (x >> 4) == chunkX && (z >> 4) == chunkZ;
    }

    @Override
    public double heuristic(int x, int y, int z) {
        return asRectangle().heuristic(x, y, z);
    }

    @Override
    public String debugName() {
        return "GoalChunk[" + chunkX + "," + chunkZ + "]";
    }

    public GoalRectangleXZ asRectangle() {
        int minX = chunkX << 4;
        int minZ = chunkZ << 4;
        return new GoalRectangleXZ(minX, minZ, minX + 15, minZ + 15);
    }

    @Override
    public NavigationTarget resolve(int px, int py, int pz) {
        return new NavigationTarget.Column(chunkX * 16 + 8, chunkZ * 16 + 8);
    }
}
