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

package fr.riege.ebsl.common.pathfinding.provider;

import fr.riege.ebsl.common.pathfinding.movement.MovementTerrain;

import java.util.Objects;

/**
 * Creates navigation point providers behind provider contracts.
 */
public final class NavigationPointProviders {
    private NavigationPointProviders() {
    }

    /**
     * Creates a world-backed provider that resolves points through a walkability checker.
     *
     * @param checker the walkability checker to query
     * @return a world-backed navigation point provider
     */
    public static WorldNavigationPointProvider worldBacked(MovementTerrain checker) {
        return new LayerNavigationPointProvider(Objects.requireNonNull(checker, "checker"));
    }
}
