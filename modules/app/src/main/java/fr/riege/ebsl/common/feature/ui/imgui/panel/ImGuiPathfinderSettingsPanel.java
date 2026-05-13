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

package fr.riege.ebsl.common.feature.ui.imgui.panel;

import fr.riege.ebsl.common.feature.ui.layout.UiRect;
import fr.riege.ebsl.common.pathfinding.settings.PathfinderSettings;
import imgui.ImGui;

final class ImGuiPathfinderSettingsPanel {
    void render(UiRect viewport) {
        ImGuiPanelChrome.begin(viewport, "##pf-settings-scroll");
        ImGui.text("Pathfinder Settings");
        ImGui.sameLine();
        if (ImGui.button("Reset to defaults", 148.0f, 18.0f)) PathfinderSettings.resetToDefaults();
        ImGui.spacing();
        ImGuiSettingControls.renderGroup("General", PathfinderSettings.generalSettings());
        ImGuiSettingControls.renderGroup("Movement cost", PathfinderSettings.movementCostSettings());
        ImGuiSettingControls.renderGroup("Path quality", PathfinderSettings.qualitySettings());
        ImGuiSettingControls.renderGroup("Execution", PathfinderSettings.executionSettings());
        ImGuiSettingControls.renderGroup("Recovery", PathfinderSettings.recoverySettings());
        ImGuiSettingControls.renderGroup("Smoothing", PathfinderSettings.smoothingSettings());
        ImGui.endChild();
    }
}
