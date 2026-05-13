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
import fr.riege.ebsl.common.domain.analytics.AnalyticsEventLog;
import fr.riege.ebsl.common.feature.module.BotModuleRegistry;
import fr.riege.ebsl.common.feature.module.PathfinderModule;
import fr.riege.ebsl.common.feature.module.PathfinderModuleCategory;
import fr.riege.ebsl.common.feature.task.BotTask;
import fr.riege.ebsl.common.feature.task.BotTaskRegistry;
import fr.riege.ebsl.common.feature.ui.imgui.ImGuiPanelUtil;
import fr.riege.ebsl.common.feature.ui.imgui.settings.ImGuiSettingRenderContext;
import fr.riege.ebsl.common.feature.ui.imgui.settings.ImGuiSettingRendererRegistry;
import fr.riege.ebsl.common.feature.ui.layout.ViewportLayout;
import fr.riege.ebsl.common.feature.ui.state.EbslUiState;
import fr.riege.ebsl.common.feature.ui.state.MainViewTab;
import fr.riege.ebsl.common.feature.ui.state.RightPanelMode;
import fr.riege.ebsl.common.platform.EbslPlatform;
import fr.riege.ebsl.common.platform.service.NavigationService;
import imgui.ImGui;
import imgui.flag.ImGuiCol;
import imgui.type.ImString;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

public final class ImGuiModulesPanel implements ImGuiUiPanel {
    private final Map<String, ImString> stringValues = new HashMap<>();
    private final ImGuiScriptNodePalettePanel scriptNodePalettePanel = new ImGuiScriptNodePalettePanel();
    private final ImGuiScriptLoaderPanel scriptLoaderPanel = new ImGuiScriptLoaderPanel();

    @Override
    public void render(EbslUiState state, ViewportLayout layout, NavigationService navigation, EbslPlatform platform) {
        if (state.mainViewTab() == MainViewTab.SCRIPT) {
            scriptNodePalettePanel.render(state, layout.right());
            return;
        }
        ImGuiPanelUtil.nextFixedWindow(layout.right());
        if (ImGui.begin("Pathfinder botting##ebsl-right", ImGuiPanelUtil.FIXED_PANEL_FLAGS)) {
            renderModeButtons(state);
            ImGui.separator();
            renderActivePanel(state, platform);
            ImGui.end();
        }
    }

    private void renderModeButtons(EbslUiState state) {
        if (ImGui.button("Modules", 84.0f, 24.0f)) state.showModuleList();
        ImGui.sameLine();
        if (ImGui.button("Tasks", 84.0f, 24.0f)) state.showTaskList();
        ImGui.sameLine();
        if (ImGui.button("Scripts", 84.0f, 24.0f)) state.showScriptLoader();
    }

    private void renderActivePanel(EbslUiState state, EbslPlatform platform) {
        if (state.rightPanelMode() == RightPanelMode.MODULE_SETTINGS && state.selectedModule() != null) {
            renderModuleSettings(state);
        } else if (state.rightPanelMode() == RightPanelMode.TASK_SETTINGS && state.selectedTask() != null) {
            renderTaskSettings(state);
        } else if (state.rightPanelMode() == RightPanelMode.SCRIPT_LOADER) {
            scriptLoaderPanel.render(state, platform);
        } else if (state.rightPanelMode() == RightPanelMode.TASK_LIST) {
            renderTaskList(state);
        } else {
            renderModuleList(state);
        }
    }

    private void renderModuleList(EbslUiState state) {
        ImGui.text("Pathfinder modules");
        ImGui.separator();
        for (PathfinderModule module : BotModuleRegistry.modules()) {
            pushModuleButtonColor(module);
            if (ImGui.button(module.displayName(), -1.0f, 24.0f)) {
                state.showModuleSettings(module);
                AnalyticsEventLog.recordAnalytics("module", "Opened settings for " + module.displayName());
            }
            ImGui.popStyleColor(3);
        }
        ImGui.separator();
        Map<PathfinderModuleCategory, Integer> counts = new EnumMap<>(PathfinderModuleCategory.class);
        for (PathfinderModule module : BotModuleRegistry.modules()) {
            counts.merge(module.category(), 1, Integer::sum);
        }
        ImGui.text("Categories");
        for (PathfinderModuleCategory category : PathfinderModuleCategory.values()) {
            ImGui.textDisabled(category.displayName() + ": " + counts.getOrDefault(category, 0));
        }
    }

    private void renderTaskList(EbslUiState state) {
        ImGui.text("Task manager");
        ImGui.separator();
        for (BotTask task : BotTaskRegistry.tasks()) {
            pushTaskButtonColor(task);
            if (ImGui.button(task.displayName(), -1.0f, 24.0f)) {
                state.showTaskSettings(task);
                AnalyticsEventLog.recordAnalytics("task", "Opened settings for " + task.displayName());
            }
            ImGui.popStyleColor(3);
        }
        ImGui.separator();
        ImGui.text("Registered tasks");
        ImGui.textDisabled("Tasks: " + BotTaskRegistry.tasks().size());
    }

    private void renderModuleSettings(EbslUiState state) {
        PathfinderModule module = state.selectedModule();
        if (ImGui.button("Back", 72.0f, 24.0f)) state.showModuleList();
        ImGui.sameLine();
        if (ImGui.button("Reset to default", 130.0f, 24.0f)) {
            BotModuleRegistry.resetToDefaultsAndSave(module);
            AnalyticsEventLog.recordAnalytics("module", "Reset " + module.displayName());
        }
        ImGui.separator();
        ImGui.text(module.displayName());
        ImGui.textDisabled(module.category().displayName());
        ImGui.spacing();
        for (Setting<?> setting : module.settings()) {
            renderSetting(module.id(), setting, () -> saveSetting(module, setting));
        }
    }

    private void renderTaskSettings(EbslUiState state) {
        BotTask task = state.selectedTask();
        if (ImGui.button("Back", 72.0f, 24.0f)) state.showTaskList();
        ImGui.sameLine();
        if (ImGui.button("Reset to default", 130.0f, 24.0f)) {
            BotTaskRegistry.resetToDefaultsAndSave(task);
            AnalyticsEventLog.recordAnalytics("task", "Reset " + task.displayName());
        }
        ImGui.separator();
        ImGui.text(task.displayName());
        ImGui.textDisabled(task.description());
        ImGui.spacing();
        for (Setting<?> setting : task.settings()) {
            renderSetting(task.id(), setting, () -> saveSetting(task, setting));
        }
    }

    private void renderSetting(String ownerId, Setting<?> setting, Runnable save) {
        ImGuiSettingRendererRegistry.render(setting, new ImGuiSettingRenderContext(ownerId, -1.0f, save, stringValues));
    }

    private static void pushModuleButtonColor(PathfinderModule module) {
        int base = module.isEnabled() ? 0xFF1B7F46 : 0xFF8A2630;
        int hover = module.isEnabled() ? 0xFF239D58 : 0xFFA8323E;
        int act = module.isEnabled() ? 0xFF28B565 : 0xFFC23B49;
        ImGui.pushStyleColor(ImGuiCol.Button, base);
        ImGui.pushStyleColor(ImGuiCol.ButtonHovered, hover);
        ImGui.pushStyleColor(ImGuiCol.ButtonActive, act);
    }

    private static void pushTaskButtonColor(BotTask task) {
        int base = task.isEnabled() ? 0xFF1B6F7F : 0xFF5C6570;
        int hover = task.isEnabled() ? 0xFF23889D : 0xFF747F8C;
        int act = task.isEnabled() ? 0xFF28A0B5 : 0xFF8A96A5;
        ImGui.pushStyleColor(ImGuiCol.Button, base);
        ImGui.pushStyleColor(ImGuiCol.ButtonHovered, hover);
        ImGui.pushStyleColor(ImGuiCol.ButtonActive, act);
    }

    private void saveSetting(PathfinderModule module, Setting<?> setting) {
        BotModuleRegistry.notifySettingChanged(module, setting);
        AnalyticsEventLog.recordAnalytics("setting", module.displayName() + "." + setting.id() + "=" + setting.value());
    }

    private void saveSetting(BotTask task, Setting<?> setting) {
        BotTaskRegistry.notifySettingChanged(task, setting);
        AnalyticsEventLog.recordAnalytics("setting", task.displayName() + "." + setting.id() + "=" + setting.value());
    }

}
