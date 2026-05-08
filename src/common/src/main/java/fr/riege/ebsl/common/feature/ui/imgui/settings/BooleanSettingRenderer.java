package fr.riege.ebsl.common.feature.ui.imgui.settings;

import fr.riege.ebsl.common.core.settings.BooleanSetting;
import fr.riege.ebsl.common.core.settings.Setting;
import imgui.ImGui;
import imgui.type.ImBoolean;

final class BooleanSettingRenderer implements ImGuiSettingRenderer {
    @Override
    public Class<BooleanSetting> settingType() {
        return BooleanSetting.class;
    }

    @Override
    public void render(Setting<?> raw, ImGuiSettingRenderContext context) {
        BooleanSetting setting = (BooleanSetting) raw;
        ImBoolean value = new ImBoolean(setting.value());
        if (ImGui.checkbox(context.label(setting), value)) {
            setting.setValue(value.get());
            context.changed();
        }
    }
}
