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

import fr.riege.ebsl.common.feature.ui.imgui.panel.*;
import fr.riege.ebsl.common.feature.ui.layout.UiRect;
import fr.riege.ebsl.common.feature.ui.layout.ViewportLayout;
import fr.riege.ebsl.common.feature.ui.state.EbslUiState;
import fr.riege.ebsl.common.platform.EbslPlatform;
import fr.riege.ebsl.common.platform.service.NavigationService;
import imgui.ImGui;
import imgui.ImGuiStyle;

import java.util.List;

public final class ImGuiViewportRenderer {
    private final List<ImGuiUiPanel> panels = List.of(
        new ImGuiHeaderPanel(),
        new ImGuiGoalsPanel(),
        new ImGuiCenterViewportPanel(),
        new ImGuiModulesPanel(),
        new ImGuiAnalyticsPanel());
    private boolean styleApplied;

    public void render(EbslUiState state, ViewportLayout layout, NavigationService navigation, EbslPlatform platform) {
        applyStyleOnce();
        for (ImGuiUiPanel panel : panels) {
            panel.render(state, layout, navigation, platform);
        }
        drawLayoutLines(layout);
    }

    private void drawLayoutLines(ViewportLayout layout) {
        int strong = 0xFF4A90E2;
        int soft = 0xAA26313D;
        ImGuiPanelUtil.drawRectBorder(layout.center(), strong, 2.0f);
        ImGuiPanelUtil.drawRectBorder(layout.left(), soft, 1.0f);
        ImGuiPanelUtil.drawRectBorder(layout.right(), soft, 1.0f);
        ImGuiPanelUtil.drawRectBorder(layout.bottom(), soft, 1.0f);
        UiRect center = layout.center();
        ImGuiPanelUtil.drawLine(center.x(), center.y(), center.right(), center.y(), strong, 2.0f);
        ImGuiPanelUtil.drawLine(center.x(), center.bottom(), center.right(), center.bottom(), strong, 2.0f);
    }

    private void applyStyleOnce() {
        if (styleApplied) return;
        styleApplied = true;
        ImGuiStyle style = ImGui.getStyle();
        style.setWindowRounding(0.0f);
        style.setChildRounding(0.0f);
        style.setFrameRounding(3.0f);
        style.setGrabRounding(3.0f);
        style.setWindowBorderSize(0.0f);
        style.setFrameBorderSize(0.0f);
        style.setWindowPadding(8.0f, 8.0f);
        style.setItemSpacing(8.0f, 6.0f);
    }
}
