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

import fr.riege.ebsl.common.core.settings.CommonSettingsStore;
import fr.riege.ebsl.common.feature.scripting.manager.EbslScriptDocument;
import fr.riege.ebsl.common.feature.scripting.manager.EbslScriptManager;
import fr.riege.ebsl.common.feature.scripting.manager.EbslScriptView;
import fr.riege.ebsl.common.feature.scripting.runtime.EbslScriptEngine;
import fr.riege.ebsl.common.feature.ui.layout.UiRect;
import fr.riege.ebsl.common.feature.ui.layout.UiTheme;
import fr.riege.ebsl.common.feature.ui.state.EbslUiState;
import fr.riege.ebsl.common.platform.EbslPlatform;
import fr.riege.ebsl.common.platform.service.EbslServices;
import imgui.ImDrawList;
import imgui.ImGui;
import imgui.type.ImString;

public final class ImGuiScriptEditorPanel {
    private static final int BUFFER_SIZE = 65536;

    private final ImString source = new ImString(EbslScriptManager.DEFAULT_SOURCE, BUFFER_SIZE);
    private final ImGuiEbslCodeEditor codeEditor = new ImGuiEbslCodeEditor();
    private final ImGuiScriptEditorPopups popups = new ImGuiScriptEditorPopups();
    private final ImGuiScriptGraphView graphView = new ImGuiScriptGraphView(source, this::setStatus);
    private String loadedFile = "";
    private int loadedRevision = -1;
    private String status = "idle";

    public void render(EbslUiState state, UiRect viewport, EbslPlatform platform) {
        ensureLoaded(state, platform);
        consumeRequestedInsert(state);

        ImDrawList dl = ImGui.getWindowDrawList();
        dl.addRectFilled(viewport.x(), viewport.y(), viewport.right(), viewport.bottom(), UiTheme.BG_PANEL_DARK);
        dl.addRectFilled(viewport.x(), viewport.y(), viewport.right(), viewport.y() + 34.0f, UiTheme.BG_PANEL);

        ImGui.setCursorScreenPos(viewport.x() + 10.0f, viewport.y() + 7.0f);
        renderToolbar(state, platform);

        UiRect editor = new UiRect(viewport.x() + 10, viewport.y() + 44, viewport.width() - 20, viewport.height() - 54);
        if (state.scriptView() == EbslScriptView.GRAPH) {
            renderGraph(editor, platform, !popups.blocksBackgroundInteraction());
        } else {
            renderCode(editor);
        }
        popups.render(viewport, this::saveSettings);
    }

    private void renderToolbar(EbslUiState state, EbslPlatform platform) {
        ImGui.text(state.selectedScriptFile());
        ImGui.sameLine(190.0f);
        for (EbslScriptView view : EbslScriptView.values()) {
            if (ImGui.button(view.label(), 64.0f, 22.0f)) {
                state.setScriptView(view);
            }
            ImGui.sameLine();
        }
        if (ImGui.button("Save", 64.0f, 22.0f)) {
            new EbslScriptManager(platform.storage()).save(state.selectedScriptFile(), source.get());
            status = "saved";
        }
        ImGui.sameLine();
        if (ImGui.button("Reload", 70.0f, 22.0f)) {
            load(state, platform);
        }
        ImGui.sameLine();
        if (ImGui.button("Validate", 78.0f, 22.0f)) {
            validate();
        }
        ImGui.sameLine();
        if (ImGui.button("Doc", 54.0f, 22.0f)) {
            popups.requestDoc();
        }
        ImGui.sameLine();
        if (ImGui.button("Settings", 82.0f, 22.0f)) {
            popups.requestSettings();
        }
        ImGui.sameLine();
        ImGui.textDisabled(status);
    }

    private void renderCode(UiRect editor) {
        codeEditor.render(editor, source);
    }

    private void renderGraph(UiRect editor, EbslPlatform platform, boolean interactive) {
        graphView.render(editor, platform, interactive);
    }

    private void saveSettings() {
        CommonSettingsStore.save(EbslServices.platform().storage());
    }

    private void consumeRequestedInsert(EbslUiState state) {
        String insert = state.consumeScriptInsert();
        if (!insert.isBlank()) {
            insertNode(insert);
        }
    }

    private void insertNode(String command) {
        String current = source.get();
        String separator = current.isBlank() || current.endsWith("\n") ? "" : "\n";
        source.set(current + separator + command + "\n");
        status = "inserted " + command;
    }

    private void validate() {
        try {
            EbslScriptEngine.compile(source.get());
            status = "valid";
        } catch (RuntimeException exception) {
            status = "error: " + exception.getMessage();
        }
    }

    private void ensureLoaded(EbslUiState state, EbslPlatform platform) {
        if (!loadedFile.equals(state.selectedScriptFile()) || loadedRevision != state.scriptRevision()) {
            load(state, platform);
        }
    }

    private void load(EbslUiState state, EbslPlatform platform) {
        EbslScriptManager manager = new EbslScriptManager(platform.storage());
        EbslScriptDocument document = manager.load(state.selectedScriptFile());
        loadedFile = document.fileName();
        loadedRevision = state.scriptRevision();
        source.set(document.source());
        graphView.loadGraphLayout(manager, loadedFile);
        status = "loaded";
    }

    private void setStatus(String status) {
        this.status = status;
    }
}
