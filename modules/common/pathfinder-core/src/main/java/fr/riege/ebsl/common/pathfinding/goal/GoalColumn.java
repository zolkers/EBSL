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

public record GoalColumn(int x, int z, double radius) implements Goal {
    public GoalColumn {
        GoalValidators.requireNonNegativeFiniteRadius(radius, "GoalColumn radius");
    }

    @Override
    public boolean isInGoal(int x, int y, int z) {
        double dx = (double) this.x - x;
        double dz = (double) this.z - z;
        return dx * dx + dz * dz <= radius * radius;
    }

    @Override
    public double heuristic(int x, int y, int z) {
        double dx = (double) this.x - x;
        double dz = (double) this.z - z;
        return Math.max(0.0, Math.sqrt(dx * dx + dz * dz) - radius);
    }

    @Override
    public String debugName() {
        return "GoalColumn[" + x + "," + z + ",r=" + radius + "]";
    }

    @Override
    public NavigationTarget resolve(int px, int py, int pz) {
        return new NavigationTarget.Column(x, z);
    }
}
