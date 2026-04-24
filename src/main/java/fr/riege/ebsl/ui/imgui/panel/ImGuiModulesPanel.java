package fr.riege.ebsl.ui.imgui.panel;

import fr.riege.ebsl.analytics.AnalyticsEventLog;
import fr.riege.ebsl.botting.module.BotModule;
import fr.riege.ebsl.botting.module.BotModuleCategory;
import fr.riege.ebsl.botting.registry.BotModuleRegistry;
import fr.riege.ebsl.botting.storage.BotModuleSettingsStore;
import fr.riege.ebsl.settings.BooleanSetting;
import fr.riege.ebsl.settings.IntSetting;
import fr.riege.ebsl.settings.Setting;
import fr.riege.ebsl.ui.imgui.ImGuiPanelUtil;
import fr.riege.ebsl.ui.layout.ViewportLayout;
import fr.riege.ebsl.ui.state.EbslUiState;
import fr.riege.ebsl.ui.state.RightPanelMode;
import imgui.ImGui;
import imgui.type.ImBoolean;

import java.util.EnumMap;
import java.util.Map;

public final class ImGuiModulesPanel implements ImGuiUiPanel {
    @Override
    public void render(EbslUiState state, ViewportLayout layout) {
        ImGuiPanelUtil.nextFixedWindow(layout.right());
        if (ImGui.begin("Bot modules##ebsl-right", ImGuiPanelUtil.FIXED_PANEL_FLAGS)) {
            ImGui.text("Bot modules");
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
        for (BotModule module : BotModuleRegistry.modules()) {
            if (ImGui.button(module.displayName(), -1.0f, 24.0f)) {
                state.showModuleSettings(module);
                AnalyticsEventLog.record("module", "Opened settings for " + module.displayName());
            }
        }
        ImGui.separator();
        Map<BotModuleCategory, Integer> counts = new EnumMap<>(BotModuleCategory.class);
        for (BotModule module : BotModuleRegistry.modules()) {
            counts.merge(module.category(), 1, Integer::sum);
        }
        ImGui.text("Categories");
        for (BotModuleCategory category : BotModuleCategory.values()) {
            ImGui.textDisabled(category.displayName() + ": " + counts.getOrDefault(category, 0));
        }
    }

    private void renderModuleSettings(EbslUiState state) {
        BotModule module = state.selectedModule();
        if (ImGui.button("Back", 72.0f, 24.0f)) {
            state.showModuleList();
        }
        ImGui.sameLine();
        if (ImGui.button("Reset to default", 130.0f, 24.0f)) {
            module.resetSettings();
            BotModuleSettingsStore.save();
            AnalyticsEventLog.record("module", "Reset " + module.displayName());
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
            }
        }
    }

    private void saveSetting(BotModule module, Setting<?> setting) {
        BotModuleSettingsStore.save();
        AnalyticsEventLog.record("setting", module.displayName() + "." + setting.id() + "=" + setting.value());
    }
}
