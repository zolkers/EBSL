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

package fr.riege.ebsl.common.pathfinding.parkour;

public record ParkourJumpPlan(
    boolean feasible,
    int approachBlocks,
    int distanceBlocks,
    double requiredReach,
    double estimatedReach,
    double verticalDelta,
    String reason,
    String detail
) {
    public static ParkourJumpPlan rejected(String reason) {
        return rejected(reason, "");
    }

    public static ParkourJumpPlan rejected(String reason, String detail) {
        return new ParkourJumpPlan(false, 0, 0, 0.0, 0.0, 0.0, reason, detail);
    }
}
