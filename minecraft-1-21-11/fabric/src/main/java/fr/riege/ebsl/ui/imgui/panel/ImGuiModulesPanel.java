package fr.riege.ebsl.ui.imgui.panel;

import fr.riege.ebsl.api.EbslApi;
import fr.riege.ebsl.general.module.PathfinderModule;
import fr.riege.ebsl.general.module.PathfinderModuleCategory;
import fr.riege.ebsl.general.task.BotTask;
import fr.riege.ebsl.settings.BooleanSetting;
import fr.riege.ebsl.settings.ColorSetting;
import fr.riege.ebsl.settings.DoubleSetting;
import fr.riege.ebsl.settings.EnumSetting;
import fr.riege.ebsl.settings.IntSetting;
import fr.riege.ebsl.settings.Setting;
import fr.riege.ebsl.settings.StringListSetting;
import fr.riege.ebsl.settings.StringSetting;
import fr.riege.ebsl.ui.imgui.ImGuiPanelUtil;
import fr.riege.ebsl.ui.layout.ViewportLayout;
import fr.riege.ebsl.ui.state.EbslUiState;
import fr.riege.ebsl.ui.state.RightPanelMode;
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
    public void render(EbslUiState state, ViewportLayout layout) {
        ImGuiPanelUtil.nextFixedWindow(layout.right());
        if (ImGui.begin("Pathfinder botting##ebsl-right", ImGuiPanelUtil.FIXED_PANEL_FLAGS)) {
            if (ImGui.button("Modules", 84.0f, 24.0f)) {
                state.showModuleList();
            }
            ImGui.sameLine();
            if (ImGui.button("Tasks", 84.0f, 24.0f)) {
                state.showTaskList();
            }
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
        for (PathfinderModule module : EbslApi.gui().modules()) {
            pushModuleButtonColor(module);
            if (ImGui.button(module.displayName(), -1.0f, 24.0f)) {
                state.showModuleSettings(module);
                EbslApi.analytics().record("module", "Opened settings for " + module.displayName());
            }
            ImGui.popStyleColor(3);
        }
        ImGui.separator();
        Map<PathfinderModuleCategory, Integer> counts = new EnumMap<>(PathfinderModuleCategory.class);
        for (PathfinderModule module : EbslApi.gui().modules()) {
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
        for (BotTask task : EbslApi.gui().tasks()) {
            pushTaskButtonColor(task);
            if (ImGui.button(task.displayName(), -1.0f, 24.0f)) {
                state.showTaskSettings(task);
                EbslApi.analytics().record("task", "Opened settings for " + task.displayName());
            }
            ImGui.popStyleColor(3);
        }
        ImGui.separator();
        ImGui.text("Registered tasks");
        ImGui.textDisabled("Tasks: " + EbslApi.gui().tasks().size());
    }

    private void renderModuleSettings(EbslUiState state) {
        PathfinderModule module = state.selectedModule();
        if (ImGui.button("Back", 72.0f, 24.0f)) {
            state.showModuleList();
        }
        ImGui.sameLine();
        if (ImGui.button("Reset to default", 130.0f, 24.0f)) {
            EbslApi.modules().resetToDefaultsAndSave(module);
            EbslApi.analytics().record("module", "Reset " + module.displayName());
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
        if (ImGui.button("Back", 72.0f, 24.0f)) {
            state.showTaskList();
        }
        ImGui.sameLine();
        if (ImGui.button("Reset to default", 130.0f, 24.0f)) {
            EbslApi.tasks().resetToDefaultsAndSave(task);
            EbslApi.analytics().record("task", "Reset " + task.displayName());
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
            if (setting instanceof BooleanSetting boolSetting) {
                ImBoolean value = new ImBoolean(boolSetting.value());
                if (ImGui.checkbox(setting.displayName(), value)) {
                    boolSetting.setValue(value.get());
                    save.run();
                }
            } else if (setting instanceof IntSetting intSetting) {
                int[] value = {intSetting.value()};
                if (ImGui.sliderInt(setting.displayName(), value, intSetting.min(), intSetting.max())) {
                    intSetting.setValue(value[0]);
                    save.run();
                }
            } else if (setting instanceof DoubleSetting doubleSetting) {
                float[] value = {(float) doubleSetting.value().doubleValue()};
                if (ImGui.sliderFloat(setting.displayName(), value, (float) doubleSetting.min(), (float) doubleSetting.max())) {
                    doubleSetting.setValue((double) value[0]);
                    save.run();
                }
            } else if (setting instanceof StringSetting stringSetting) {
                ImString value = stringValues.computeIfAbsent(
                    ownerId + "." + setting.id(),
                    ignored -> new ImString(stringSetting.value(), 512));
                if (!value.get().equals(stringSetting.value())) {
                    value.set(stringSetting.value());
                }
                if (ImGui.inputText(setting.displayName(), value)) {
                    stringSetting.setValue(value.get());
                    save.run();
                }
            } else if (setting instanceof ColorSetting colorSetting) {
                renderColorSetting(colorSetting, save);
            } else if (setting instanceof EnumSetting<?> enumSetting) {
                renderEnumSetting(enumSetting, save);
            } else if (setting instanceof StringListSetting listSetting) {
                renderStringListSetting(ownerId, listSetting, save);
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
        E[] values = setting.enumType().getEnumConstants();
        String[] labels = new String[values.length];
        for (int i = 0; i < values.length; i++) {
            labels[i] = values[i].toString();
        }
        ImInt idx = new ImInt(setting.value().ordinal());
        if (ImGui.combo(setting.displayName(), idx, labels)) {
            setting.setValue(values[idx.get()]);
            save.run();
        }
    }

    private void renderStringListSetting(String ownerId, StringListSetting setting, Runnable save) {
        ImGui.text(setting.displayName());
        if (ImGui.beginChild("##" + ownerId + "." + setting.id(), 0.0f, 124.0f, true)) {
            for (int i = 0; i < setting.value().size(); i++) {
                String key = ownerId + "." + setting.id() + "." + i;
                String entry = setting.value().get(i);
                ImString value = stringValues.computeIfAbsent(key, ignored -> new ImString(entry, 128));
                if (!value.get().equals(entry)) {
                    value.set(entry);
                }
                ImGui.pushItemWidth(-38.0f);
                if (ImGui.inputText("##entry-" + key, value)) {
                    setting.setEntry(i, value.get());
                    save.run();
                }
                ImGui.popItemWidth();
                ImGui.sameLine();
                if (ImGui.button("X##delete-" + key, 24.0f, 22.0f)) {
                    setting.removeEntry(i);
                    stringValues.remove(key);
                    save.run();
                    break;
                }
            }
            ImGui.endChild();
        }
        if (ImGui.button("Add row##" + ownerId + "." + setting.id(), 86.0f, 24.0f)) {
            setting.addEntry("minecraft:stone");
            save.run();
        }
    }

    private static void pushModuleButtonColor(PathfinderModule module) {
        int base = module.isEnabled() ? 0xFF1B7F46 : 0xFF8A2630;
        int hover = module.isEnabled() ? 0xFF239D58 : 0xFFA8323E;
        int active = module.isEnabled() ? 0xFF28B565 : 0xFFC23B49;
        ImGui.pushStyleColor(ImGuiCol.Button, base);
        ImGui.pushStyleColor(ImGuiCol.ButtonHovered, hover);
        ImGui.pushStyleColor(ImGuiCol.ButtonActive, active);
    }

    private static void pushTaskButtonColor(BotTask task) {
        int base = task.isEnabled() ? 0xFF1B6F7F : 0xFF5C6570;
        int hover = task.isEnabled() ? 0xFF23889D : 0xFF747F8C;
        int active = task.isEnabled() ? 0xFF28A0B5 : 0xFF8A96A5;
        ImGui.pushStyleColor(ImGuiCol.Button, base);
        ImGui.pushStyleColor(ImGuiCol.ButtonHovered, hover);
        ImGui.pushStyleColor(ImGuiCol.ButtonActive, active);
    }

    private void saveSetting(PathfinderModule module, Setting<?> setting) {
        EbslApi.modules().saveSettings();
        EbslApi.modules().notifySettingChanged(module, setting);
        EbslApi.analytics().record("setting", module.displayName() + "." + setting.id() + "=" + setting.value());
    }

    private void saveSetting(BotTask task, Setting<?> setting) {
        EbslApi.tasks().saveSettings();
        EbslApi.tasks().notifySettingChanged(task, setting);
        EbslApi.analytics().record("setting", task.displayName() + "." + setting.id() + "=" + setting.value());
    }
}
