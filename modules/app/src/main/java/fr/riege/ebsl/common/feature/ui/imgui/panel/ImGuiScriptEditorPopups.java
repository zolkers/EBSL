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

import fr.riege.ebsl.common.core.settings.Setting;
import fr.riege.ebsl.common.feature.registry.FeatureRegistries;
import fr.riege.ebsl.common.feature.scripting.docs.*;
import fr.riege.ebsl.common.feature.scripting.highlight.EbslCodeEditorSettings;
import fr.riege.ebsl.common.feature.ui.imgui.settings.ImGuiSettingRenderContext;
import fr.riege.ebsl.common.feature.ui.layout.UiRect;
import imgui.ImGui;
import imgui.flag.ImGuiCond;
import imgui.flag.ImGuiWindowFlags;
import imgui.type.ImString;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

final class ImGuiScriptEditorPopups {
    private static final String DOC_POPUP_ID = "EBSL Language Doc##ebsl-language-doc";
    private static final String IDE_SETTINGS_POPUP_ID = "IDE Settings##ebsl-ide-settings";

    private final Map<String, ImString> editorSettingTextValues = new HashMap<>();
    private boolean docOpenRequested;
    private boolean ideSettingsOpenRequested;

    void requestDoc() {
        docOpenRequested = true;
    }

    void requestSettings() {
        ideSettingsOpenRequested = true;
    }

    boolean blocksBackgroundInteraction() {
        return docOpenRequested
            || ideSettingsOpenRequested
            || ImGui.isPopupOpen(DOC_POPUP_ID)
            || ImGui.isPopupOpen(IDE_SETTINGS_POPUP_ID);
    }

    void render(UiRect viewport, Runnable saveSettings) {
        renderDocPopup(viewport);
        renderIdeSettingsPopup(viewport, saveSettings);
    }

    private void renderIdeSettingsPopup(UiRect viewport, Runnable saveSettings) {
        if (ideSettingsOpenRequested) {
            ImGui.openPopup(IDE_SETTINGS_POPUP_ID);
            ideSettingsOpenRequested = false;
        }
        float width = Math.min(560.0f, viewport.width() - 80.0f);
        float height = Math.min(520.0f, viewport.height() - 80.0f);
        float x = viewport.x() + (viewport.width() - width) * 0.5f;
        float y = viewport.y() + (viewport.height() - height) * 0.5f;
        ImGui.setNextWindowPos(x, y, ImGuiCond.Always);
        ImGui.setNextWindowSize(width, height, ImGuiCond.Always);
        int flags = ImGuiWindowFlags.NoCollapse
            | ImGuiWindowFlags.NoSavedSettings
            | ImGuiWindowFlags.NoResize
            | ImGuiWindowFlags.NoMove;
        if (ImGui.beginPopupModal(IDE_SETTINGS_POPUP_ID, flags)) {
            if (ImGui.button("Close", 72.0f, 22.0f)) {
                ImGui.closeCurrentPopup();
            }
            ImGui.sameLine();
            if (ImGui.button("Reset", 72.0f, 22.0f)) {
                EbslCodeEditorSettings.resetToDefaults();
                saveSettings.run();
            }
            ImGui.separator();
            if (ImGui.beginChild("##ebsl-ide-settings-scroll", width - 24.0f, height - 72.0f, false)) {
                ImGuiSettingRenderContext context = new ImGuiSettingRenderContext(
                    "script-editor-setting", -1.0f, saveSettings, editorSettingTextValues);
                renderIdeSettingsGroup("Editor", EbslCodeEditorSettings.editorAppearanceSettings(), context);
                ImGui.spacing();
                renderIdeSettingsGroup("Language", EbslCodeEditorSettings.languageThemeSettings(), context);
                ImGui.endChild();
            }
            ImGui.endPopup();
        }
    }

    private static void renderIdeSettingsGroup(String title, List<Setting<?>> settings, ImGuiSettingRenderContext context) {
        ImGui.text(title);
        ImGui.separator();
        for (Setting<?> setting : settings) {
            FeatureRegistries.ui().renderSetting(setting, context);
        }
    }

    private void renderDocPopup(UiRect viewport) {
        if (docOpenRequested) {
            ImGui.openPopup(DOC_POPUP_ID);
            docOpenRequested = false;
        }
        float width = Math.min(760.0f, viewport.width() - 80.0f);
        float height = Math.min(620.0f, viewport.height() - 80.0f);
        float x = viewport.x() + (viewport.width() - width) * 0.5f;
        float y = viewport.y() + (viewport.height() - height) * 0.5f;
        ImGui.setNextWindowPos(x, y, ImGuiCond.Always);
        ImGui.setNextWindowSize(width, height, ImGuiCond.Always);
        int flags = ImGuiWindowFlags.NoCollapse
            | ImGuiWindowFlags.NoSavedSettings
            | ImGuiWindowFlags.NoResize
            | ImGuiWindowFlags.NoMove;
        if (ImGui.beginPopupModal(DOC_POPUP_ID, flags)) {
            if (ImGui.button("Close", 72.0f, 22.0f)) {
                ImGui.closeCurrentPopup();
            }
            ImGui.sameLine();
            ImGui.textDisabled("Generated from language registries");
            ImGui.separator();
            if (ImGui.beginChild("##ebsl-doc-scroll", width - 24.0f, height - 76.0f, false)) {
                renderLanguageDoc(EbslLanguageDocGenerator.generate());
                ImGui.endChild();
            }
            ImGui.endPopup();
        }
    }

    private static void renderLanguageDoc(EbslLanguageDoc doc) {
        for (EbslLanguageDocSection section : doc.sections()) {
            ImGui.text(section.title());
            ImGui.separator();
            for (EbslLanguageDocEntry entry : section.entries()) {
                renderDocEntry(entry);
            }
            ImGui.spacing();
        }
    }

    private static void renderDocEntry(EbslLanguageDocEntry entry) {
        ImGui.textColored(0.49f, 0.83f, 0.99f, 1.0f, entry.id());
        if (!entry.title().isBlank() && !entry.title().equals(entry.id())) {
            ImGui.sameLine();
            ImGui.textDisabled(entry.title());
        }
        if (!entry.description().isBlank()) {
            ImGui.textWrapped(entry.description());
        }
        docLine("Usage", entry.usage());
        docLine("Sample", entry.sample());
        if (!entry.aliases().isEmpty()) {
            docLine("Aliases", String.join(", ", entry.aliases()));
        }
        for (EbslLanguageDocParameter parameter : entry.parameters()) {
            docLine(parameter.id(), parameter.label() + " = " + parameter.defaultValue());
            if (!parameter.description().isBlank()) {
                ImGui.textWrapped(parameter.description());
            }
        }
        ImGui.spacing();
    }

    private static void docLine(String label, String value) {
        if (value == null || value.isBlank()) {
            return;
        }
        ImGui.textDisabled(label + ":");
        ImGui.sameLine();
        ImGui.textWrapped(value);
    }
}
