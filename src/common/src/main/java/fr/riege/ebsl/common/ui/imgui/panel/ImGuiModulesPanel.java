package fr.riege.ebsl.common.ui.imgui.panel;

import fr.riege.ebsl.common.analytics.AnalyticsEventLog;
import fr.riege.ebsl.common.module.BotModuleRegistry;
import fr.riege.ebsl.common.module.PathfinderModule;
import fr.riege.ebsl.common.module.PathfinderModuleCategory;
import fr.riege.ebsl.common.service.NavigationService;
import fr.riege.ebsl.common.settings.*;
import fr.riege.ebsl.common.task.BotTask;
import fr.riege.ebsl.common.task.BotTaskRegistry;
import fr.riege.ebsl.common.ui.imgui.ImGuiPanelUtil;
import fr.riege.ebsl.common.ui.layout.ViewportLayout;
import fr.riege.ebsl.common.ui.state.EbslUiState;
import fr.riege.ebsl.common.ui.state.RightPanelMode;
import imgui.ImGui;
import imgui.flag.ImGuiCol;
import imgui.type.ImBoolean;
import imgui.type.ImInt;
import imgui.type.ImString;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

public final class ImGuiModulesPanel implements ImGuiUiPanel {
    private final Map<String, ImString> stringValues = new HashMap<>();

    @Override
    public void render(EbslUiState state, ViewportLayout layout, NavigationService navigation) {
        ImGuiPanelUtil.nextFixedWindow(layout.right());
        if (ImGui.begin("Pathfinder botting##ebsl-right", ImGuiPanelUtil.FIXED_PANEL_FLAGS)) {
            if (ImGui.button("Modules", 84.0f, 24.0f)) state.showModuleList();
            ImGui.sameLine();
            if (ImGui.button("Tasks", 84.0f, 24.0f)) state.showTaskList();
            ImGui.separator();
            if (state.rightPanelMode() == RightPanelMode.MODULE_SETTINGS && state.selectedModule() != null) {
                renderModuleSettings(state);
            } else if (state.rightPanelMode() == RightPanelMode.TASK_SETTINGS && state.selectedTask() != null) {
                renderTaskSettings(state);
            } else if (state.rightPanelMode() == RightPanelMode.TASK_LIST) {
                renderTaskList(state);
            } else {
                renderModuleList(state);
            }
            ImGui.end();
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
        if (setting instanceof BooleanSetting s) {
            ImBoolean v = new ImBoolean(s.value());
            if (ImGui.checkbox(setting.displayName(), v)) { s.setValue(v.get()); save.run(); }
        } else if (setting instanceof IntSetting s) {
            int[] v = {s.value()};
            if (ImGui.sliderInt(setting.displayName(), v, s.min(), s.max())) { s.setValue(v[0]); save.run(); }
        } else if (setting instanceof DoubleSetting s) {
            float[] v = {(float) s.value().doubleValue()};
            if (ImGui.sliderFloat(setting.displayName(), v, (float) s.min(), (float) s.max())) {
                s.setValue((double) v[0]); save.run();
            }
        } else if (setting instanceof StringSetting s) {
            ImString v = stringValues.computeIfAbsent(ownerId + "." + setting.id(),
                ignored -> new ImString(s.value(), 512));
            if (!v.get().equals(s.value())) v.set(s.value());
            if (ImGui.inputText(setting.displayName(), v)) { s.setValue(v.get()); save.run(); }
        } else if (setting instanceof ColorSetting s) {
            renderColorSetting(s, save);
        } else if (setting instanceof EnumSetting<?> s) {
            renderEnumSetting(s, save);
        } else if (setting instanceof StringListSetting s) {
            renderStringListSetting(ownerId, s, save);
        }
    }

    private void renderColorSetting(ColorSetting setting, Runnable save) {
        int argb = setting.value();
        float a = ((argb >> 24) & 0xFF) / 255.0f;
        float r = ((argb >> 16) & 0xFF) / 255.0f;
        float g = ((argb >>  8) & 0xFF) / 255.0f;
        float b = ( argb        & 0xFF) / 255.0f;
        float[] col = {r, g, b, a};
        if (ImGui.colorEdit4(setting.displayName(), col)) {
            int packed = ((int)(col[3] * 255) << 24)
                | ((int)(col[0] * 255) << 16)
                | ((int)(col[1] * 255) <<  8)
                |  (int)(col[2] * 255);
            setting.setValue(packed);
            save.run();
        }
    }

    @SuppressWarnings("unchecked")
    private <E extends Enum<E>> void renderEnumSetting(EnumSetting<?> raw, Runnable save) {
        EnumSetting<E> setting = (EnumSetting<E>) raw;
        E[] vals = setting.enumType().getEnumConstants();
        String[] labels = new String[vals.length];
        for (int i = 0; i < vals.length; i++) labels[i] = vals[i].toString();
        ImInt idx = new ImInt(setting.value().ordinal());
        if (ImGui.combo(setting.displayName(), idx, labels)) { setting.setValue(vals[idx.get()]); save.run(); }
    }

    private void renderStringListSetting(String ownerId, StringListSetting setting, Runnable save) {
        ImGui.text(setting.displayName());
        if (ImGui.beginChild("##" + ownerId + "." + setting.id(), 0.0f, 124.0f, true)) {
            for (int i = 0; i < setting.value().size(); i++) {
                String key = ownerId + "." + setting.id() + "." + i;
                String entry = setting.value().get(i);
                ImString v = stringValues.computeIfAbsent(key, ignored -> new ImString(entry, 128));
                if (!v.get().equals(entry)) v.set(entry);
                ImGui.pushItemWidth(-38.0f);
                if (ImGui.inputText("##entry-" + key, v)) { setting.setEntry(i, v.get()); save.run(); }
                ImGui.popItemWidth();
                ImGui.sameLine();
                if (ImGui.button("X##delete-" + key, 24.0f, 22.0f)) {
                    setting.removeEntry(i); stringValues.remove(key); save.run(); break;
                }
            }
            ImGui.endChild();
        }
        if (ImGui.button("Add row##" + ownerId + "." + setting.id(), 86.0f, 24.0f)) {
            setting.addEntry("minecraft:stone"); save.run();
        }
    }

    private static void pushModuleButtonColor(PathfinderModule module) {
        int base  = module.isEnabled() ? 0xFF1B7F46 : 0xFF8A2630;
        int hover = module.isEnabled() ? 0xFF239D58 : 0xFFA8323E;
        int act   = module.isEnabled() ? 0xFF28B565 : 0xFFC23B49;
        ImGui.pushStyleColor(ImGuiCol.Button, base);
        ImGui.pushStyleColor(ImGuiCol.ButtonHovered, hover);
        ImGui.pushStyleColor(ImGuiCol.ButtonActive, act);
    }

    private static void pushTaskButtonColor(BotTask task) {
        int base  = task.isEnabled() ? 0xFF1B6F7F : 0xFF5C6570;
        int hover = task.isEnabled() ? 0xFF23889D : 0xFF747F8C;
        int act   = task.isEnabled() ? 0xFF28A0B5 : 0xFF8A96A5;
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
