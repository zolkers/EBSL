/*
 * This file is part of EBSL.
 *
 * EBSL is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * EBSL is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package fr.riege.ebsl.common.feature.ui.imgui.panel;

import fr.riege.ebsl.common.feature.ui.layout.UiRect;
import fr.riege.ebsl.common.feature.ui.layout.UiTheme;
import imgui.ImDrawList;
import imgui.ImGui;

final class ImGuiPanelChrome {
    private static final float HEADER_HEIGHT = 34.0f;
    private static final float CONTENT_PAD = 14.0f;

    private ImGuiPanelChrome() {
    }

    static void begin(UiRect viewport, String childId) {
        ImDrawList dl = ImGui.getWindowDrawList();
        dl.addRectFilled(viewport.x(), viewport.y(), viewport.right(), viewport.bottom(), UiTheme.BG_PANEL_DARK);
        dl.addRectFilled(viewport.x(), viewport.y(), viewport.right(), viewport.y() + HEADER_HEIGHT, UiTheme.BG_PANEL);
        dl.addLine(viewport.x(), viewport.y() + HEADER_HEIGHT, viewport.right(), viewport.y() + HEADER_HEIGHT,
            UiTheme.BORDER, 1.0f);
        ImGui.setCursorScreenPos(viewport.x() + CONTENT_PAD, viewport.y() + CONTENT_PAD);
        ImGui.beginChild(childId, viewport.width() - CONTENT_PAD * 2.0f, viewport.height() - CONTENT_PAD * 2.0f, false);
    }
}
