package fr.riege.ebsl.ui.imgui.panel;

import fr.riege.ebsl.api.EbslApi;
import fr.riege.ebsl.botting.module.PathfinderModule;
import fr.riege.ebsl.botting.module.PathfinderModuleCategory;
import fr.riege.ebsl.settings.BooleanSetting;
import fr.riege.ebsl.settings.ColorSetting;
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
        if (ImGui.begin("Pathfinder modules##ebsl-right", ImGuiPanelUtil.FIXED_PANEL_FLAGS)) {
            ImGui.text("Pathfinder modules");
            ImGui.separator();
            if (state.rightPanelMode() == RightPanelMode.MODULE_SETTINGS && state.selectedModule() != null) {
                renderModuleSettings(state);
            } else {
                renderModuleList(state);
            }
            ImGui.end();
        }
    }

    private void renderModuleList(EbslUiState state) {
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
            if (setting instanceof BooleanSetting boolSetting) {
                ImBoolean value = new ImBoolean(boolSetting.value());
                if (ImGui.checkbox(setting.displayName(), value)) {
                    boolSetting.setValue(value.get());
                    saveSetting(module, setting);
                }
            } else if (setting instanceof IntSetting intSetting) {
                int[] value = {intSetting.value()};
                if (ImGui.sliderInt(setting.displayName(), value, intSetting.min(), intSetting.max())) {
                    intSetting.setValue(value[0]);
                    saveSetting(module, setting);
                }
            } else if (setting instanceof StringSetting stringSetting) {
                ImString value = stringValues.computeIfAbsent(
                    module.id() + "." + setting.id(),
                    ignored -> new ImString(stringSetting.value(), 512));
                if (!value.get().equals(stringSetting.value())) {
                    value.set(stringSetting.value());
                }
                if (ImGui.inputText(setting.displayName(), value)) {
                    stringSetting.setValue(value.get());
                    saveSetting(module, setting);
                }
            } else if (setting instanceof ColorSetting colorSetting) {
                renderColorSetting(module, colorSetting);
            } else if (setting instanceof EnumSetting<?> enumSetting) {
                renderEnumSetting(module, enumSetting);
            } else if (setting instanceof StringListSetting listSetting) {
                renderStringListSetting(module, listSetting);
            }
        }
    }

    private void renderColorSetting(PathfinderModule module, ColorSetting setting) {
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
            saveSetting(module, setting);
        }
    }

    @SuppressWarnings("unchecked")
    private <E extends Enum<E>> void renderEnumSetting(PathfinderModule module, EnumSetting<?> raw) {
        EnumSetting<E> setting = (EnumSetting<E>) raw;
        E[] values = setting.enumType().getEnumConstants();
        String[] labels = new String[values.length];
        for (int i = 0; i < values.length; i++) {
            labels[i] = values[i].toString();
        }
        ImInt idx = new ImInt(setting.value().ordinal());
        if (ImGui.combo(setting.displayName(), idx, labels)) {
            setting.setValue(values[idx.get()]);
            saveSetting(module, setting);
        }
    }

    private void renderStringListSetting(PathfinderModule module, StringListSetting setting) {
        ImGui.text(setting.displayName());
        if (ImGui.beginChild("##" + module.id() + "." + setting.id(), 0.0f, 124.0f, true)) {
            for (int i = 0; i < setting.value().size(); i++) {
                String key = module.id() + "." + setting.id() + "." + i;
                String entry = setting.value().get(i);
                ImString value = stringValues.computeIfAbsent(key, ignored -> new ImString(entry, 128));
                if (!value.get().equals(entry)) {
                    value.set(entry);
                }
                ImGui.pushItemWidth(-38.0f);
                if (ImGui.inputText("##entry-" + key, value)) {
                    setting.setEntry(i, value.get());
                    saveSetting(module, setting);
                }
                ImGui.popItemWidth();
                ImGui.sameLine();
                if (ImGui.button("X##delete-" + key, 24.0f, 22.0f)) {
                    setting.removeEntry(i);
                    stringValues.remove(key);
                    saveSetting(module, setting);
                    break;
                }
            }
            ImGui.endChild();
        }
        if (ImGui.button("Add row##" + module.id() + "." + setting.id(), 86.0f, 24.0f)) {
            setting.addEntry("minecraft:stone");
            saveSetting(module, setting);
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

    private void saveSetting(PathfinderModule module, Setting<?> setting) {
        EbslApi.modules().saveSettings();
        EbslApi.modules().notifySettingChanged(module, setting);
        EbslApi.analytics().record("setting", module.displayName() + "." + setting.id() + "=" + setting.value());
    }
}
