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

package fr.riege.ebsl.common.api.navigation;

import fr.riege.ebsl.common.navigation.NavigationStatus;
import fr.riege.ebsl.common.pathfinding.Node;
import fr.riege.ebsl.common.platform.service.NavigationService;

public record NavigationSnapshot(
    NavigationStatus status,
    boolean navigating,
    Node.MoveType currentMoveType,
    boolean walkSneakLatched,
    int pathNodeCount
) {
    public static NavigationSnapshot capture(NavigationService nav) {
        return new NavigationSnapshot(
            nav.pathStatus(),
            nav.isNavigating(),
            nav.currentMoveType(),
            nav.isWalkSneakLatched(),
            nav.lastPathNodeCount());
    }

    public String navigationStateLabel() {
        return status == null ? "unknown" : status.name().toLowerCase();
    }
}
