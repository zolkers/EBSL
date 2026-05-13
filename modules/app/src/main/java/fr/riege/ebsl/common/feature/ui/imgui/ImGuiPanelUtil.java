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

package fr.riege.ebsl.common.feature.ui.imgui;

import fr.riege.ebsl.common.feature.ui.layout.UiRect;
import imgui.ImDrawList;
import imgui.ImGui;
import imgui.flag.ImGuiCond;
import imgui.flag.ImGuiWindowFlags;

public final class ImGuiPanelUtil {
    public static final int FIXED_PANEL_FLAGS =
        ImGuiWindowFlags.NoMove
            | ImGuiWindowFlags.NoResize
            | ImGuiWindowFlags.NoCollapse
            | ImGuiWindowFlags.NoSavedSettings
            | ImGuiWindowFlags.NoTitleBar;

    private ImGuiPanelUtil() {}

    public static void nextFixedWindow(UiRect rect) {
        ImGui.setNextWindowPos(rect.x(), rect.y(), ImGuiCond.Always);
        ImGui.setNextWindowSize(rect.width(), rect.height(), ImGuiCond.Always);
    }

    public static void drawRectBorder(UiRect rect, int color, float thickness) {
        ImDrawList dl = ImGui.getForegroundDrawList();
        dl.addRect(rect.x(), rect.y(), rect.right(), rect.bottom(), color, 0.0f, 0, thickness);
    }

    public static void drawLine(float x1, float y1, float x2, float y2, int color, float thickness) {
        ImGui.getForegroundDrawList().addLine(x1, y1, x2, y2, color, thickness);
    }
}
