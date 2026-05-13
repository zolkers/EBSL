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

package fr.riege.ebsl.common.pathfinding.quality;

import fr.riege.ebsl.common.pathfinding.Node;

/**
 * Defines risk and planning penalties for movement types.
 *
 * <p>Quality metrics and processors use this model to prefer human-like routes over fast but fragile obstacle-heavy paths.</p>
 */
public interface MovementCostModel {
    /**
     * Returns the quality risk assigned to the supplied movement type.
 *
     * @param type the movement or event type being evaluated
     * @return the value defined by this contract
     */
    double risk(Node.MoveType type);

    /**
     * Returns the planning penalty assigned to the supplied movement type.
 *
     * @param type the movement or event type being evaluated
     * @return the value defined by this contract
     */
    double planningPenalty(Node.MoveType type);
}
