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

package fr.riege.ebsl.common.platform.layer;

/**
 * Provides the platform ImGui frame integration.
 *
 * <p>The layer registers panel drawing callbacks and reports the active viewport dimensions for layout code.</p>
 */
public interface IImGuiLayer {
    /**
     * Registers the callback invoked for each ImGui frame.
 *
     * @param drawPanels the callback that draws registered panels
     */
    void registerFrame(Runnable drawPanels);
    /**
     * Returns the current ImGui viewport width in pixels.
 *
     * @return the value defined by this contract
     */
    int getViewportWidth();
    /**
     * Returns the current ImGui viewport height in pixels.
 *
     * @return the value defined by this contract
     */
    int getViewportHeight();
}
