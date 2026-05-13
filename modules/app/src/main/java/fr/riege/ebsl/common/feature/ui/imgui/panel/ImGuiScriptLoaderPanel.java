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

import fr.riege.ebsl.common.feature.scripting.manager.EbslScriptManager;
import fr.riege.ebsl.common.feature.scripting.runtime.EbslScriptTask;
import fr.riege.ebsl.common.feature.ui.state.EbslUiState;
import fr.riege.ebsl.common.platform.EbslPlatform;
import imgui.ImGui;
import imgui.flag.ImGuiCol;

import java.util.List;

public final class ImGuiScriptLoaderPanel {
    private static final int RUNNING_TEXT = 0xFF52D273;
    private static final int RUNNING_HEADER = 0x5534B55F;
    private static final int RUNNING_HEADER_HOVERED = 0x7740C96F;
    private static final int RUNNING_HEADER_ACTIVE = 0x9948D97A;

    public void render(EbslUiState state, EbslPlatform platform) {
        EbslScriptManager manager = new EbslScriptManager(platform.storage());
        List<String> activeFiles = EbslScriptTask.INSTANCE.activeFiles();
        ImGui.text("Script loader");
        ImGui.separator();
        for (String script : manager.scripts()) {
            boolean selected = script.equals(state.selectedScriptFile());
            boolean running = activeFiles.contains(EbslScriptManager.normalizeFileName(script));
            if (running) {
                pushRunningScriptStyle();
            }
            if (ImGui.selectable(script, selected)) {
                state.selectScriptFile(script);
            }
            if (running) {
                ImGui.popStyleColor(4);
            }
        }
        ImGui.separator();
        ImGui.textDisabled("Selected: " + state.selectedScriptFile());
        if (ImGui.button("Load into task", -1.0f, 24.0f)) {
            EbslScriptTask.INSTANCE.loadFile(state.selectedScriptFile());
        }
        if (ImGui.button("Run now", -1.0f, 24.0f)) {
            EbslScriptTask.INSTANCE.runFile(state.selectedScriptFile());
        }
        if (ImGui.button("Stop selected", -1.0f, 24.0f)) {
            EbslScriptTask.INSTANCE.stop(state.selectedScriptFile());
        }
        if (ImGui.button("Stop all", -1.0f, 24.0f)) {
            EbslScriptTask.INSTANCE.stopAll();
        }
        ImGui.textDisabled("Task status: " + EbslScriptTask.INSTANCE.status());
        for (String line : EbslScriptTask.INSTANCE.activeLines()) {
            ImGui.textDisabled(line);
        }
    }

    private static void pushRunningScriptStyle() {
        ImGui.pushStyleColor(ImGuiCol.Text, RUNNING_TEXT);
        ImGui.pushStyleColor(ImGuiCol.Header, RUNNING_HEADER);
        ImGui.pushStyleColor(ImGuiCol.HeaderHovered, RUNNING_HEADER_HOVERED);
        ImGui.pushStyleColor(ImGuiCol.HeaderActive, RUNNING_HEADER_ACTIVE);
    }
}
