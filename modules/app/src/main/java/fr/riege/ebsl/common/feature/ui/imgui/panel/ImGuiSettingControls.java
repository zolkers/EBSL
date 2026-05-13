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

import fr.riege.ebsl.common.core.settings.Setting;
import fr.riege.ebsl.common.feature.ui.imgui.settings.ImGuiSettingRenderContext;
import fr.riege.ebsl.common.feature.ui.imgui.settings.ImGuiSettingRendererRegistry;
import fr.riege.ebsl.common.pathfinding.settings.PathfinderSettings;
import imgui.ImGui;
import imgui.flag.ImGuiCond;
import imgui.type.ImString;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

final class ImGuiSettingControls {
    private static final Map<String, ImString> TEXT_VALUES = new HashMap<>();

    private ImGuiSettingControls() {
    }

    static void renderGroup(String label, List<Setting<?>> settings) {
        ImGui.setNextItemOpen(true, ImGuiCond.Once);
        if (!ImGui.collapsingHeader(label)) return;
        ImGui.indent(10.0f);
        for (Setting<?> setting : settings) render(setting);
        ImGui.unindent(10.0f);
        ImGui.spacing();
    }

    static void reset(List<Setting<?>> settings) {
        for (Setting<?> setting : settings) {
            setting.resetToDefault();
        }
        PathfinderSettings.save();
    }

    private static void render(Setting<?> setting) {
        ImGuiSettingRendererRegistry.render(
            setting,
            new ImGuiSettingRenderContext("pathfinder-setting", -1.0f, PathfinderSettings::save, TEXT_VALUES)
        );
    }
}
