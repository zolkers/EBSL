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

import fr.riege.ebsl.common.feature.scripting.manager.EbslScriptManager;
import fr.riege.ebsl.common.feature.ui.imgui.ImGuiPanelUtil;
import fr.riege.ebsl.common.feature.ui.layout.UiRect;
import fr.riege.ebsl.common.feature.ui.state.EbslUiState;
import fr.riege.ebsl.common.platform.EbslPlatform;
import imgui.ImGui;
import imgui.type.ImString;
import java.util.List;

public final class ImGuiScriptManagerPanel {
    private final ImString newFileName = new ImString(EbslScriptManager.stripExtension(EbslScriptManager.DEFAULT_FILE), 96);

    public void render(EbslUiState state, UiRect rect, EbslPlatform platform) {
        EbslScriptManager manager = new EbslScriptManager(platform.storage());
        ImGuiPanelUtil.nextFixedWindow(rect);
        if (ImGui.begin("EBSL scripts##ebsl-left-scripts", ImGuiPanelUtil.FIXED_PANEL_FLAGS)) {
            renderScriptList(state, manager);
            renderScriptActions(state, manager);
            ImGui.spacing();
            ImGui.textDisabled("Editing only. Run scripts from Main > Scripts.");
            ImGui.end();
        }
    }

    private void renderScriptList(EbslUiState state, EbslScriptManager manager) {
        ImGui.text("Scripts");
        ImGui.separator();
        if (ImGui.beginChild("##ebsl-script-list", 0.0f, 220.0f, true)) {
            for (String script : manager.scripts()) {
                boolean selected = script.equals(state.selectedScriptFile());
                if (ImGui.selectable(script, selected)) {
                    state.selectScriptFile(script);
                }
            }
            ImGui.endChild();
        }
    }

    private void renderScriptActions(EbslUiState state, EbslScriptManager manager) {
        ImGui.separator();
        ImGui.inputText("Name", newFileName);
        if (ImGui.button("Create", -1.0f, 24.0f)) {
            createScript(state, manager);
        }
        if (ImGui.button("Delete selected", -1.0f, 24.0f)) {
            deleteSelectedScript(state, manager);
        }
    }

    private void createScript(EbslUiState state, EbslScriptManager manager) {
        String file = EbslScriptManager.normalizeFileName(newFileName.get());
        manager.create(file);
        state.selectScriptFile(file);
        newFileName.set(EbslScriptManager.stripExtension(file));
    }

    private static void deleteSelectedScript(EbslUiState state, EbslScriptManager manager) {
        manager.delete(state.selectedScriptFile());
        List<String> scripts = manager.scripts();
        if (scripts.isEmpty()) {
            manager.create(EbslScriptManager.DEFAULT_FILE);
            state.selectScriptFile(EbslScriptManager.DEFAULT_FILE);
        } else {
            state.selectScriptFile(scripts.getFirst());
        }
    }
}
