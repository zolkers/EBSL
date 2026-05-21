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

package fr.riege.ebsl.common.platform.service;

/**
 * Controls the visibility of the in-game EBSL user interface.
 *
 * <p>Platform adapters implement this boundary so commands and modules can toggle UI state without depending on loader details.</p>
 */
public interface UiService {
    /**
     * Toggles UI visibility and returns the new visible state.
 *
     * @return true when the condition is satisfied; false otherwise
     */
    boolean toggle();

    /**
     * Returns whether visible is true for the current state.
 *
     * @return true when the condition is satisfied; false otherwise
     */
    boolean isVisible();
}
