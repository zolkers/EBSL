package fr.riege.ebsl.common.feature.ui.imgui.settings;

import fr.riege.ebsl.common.core.settings.IntSetting;
import fr.riege.ebsl.common.core.settings.Setting;
import imgui.ImGui;
import imgui.type.ImInt;

final class IntSettingRenderer implements ImGuiSettingRenderer {
    @Override
    public Class<IntSetting> settingType() {
        return IntSetting.class;
    }

    @Override
    public void render(Setting<?> raw, ImGuiSettingRenderContext context) {
        IntSetting setting = (IntSetting) raw;
        ImInt value = new ImInt(setting.value());
        context.applyItemWidth();
        if (ImGui.inputInt(context.label(setting), value)) {
            setting.setValue(Math.clamp(value.get(), setting.min(), setting.max()));
            context.changed();
        }
    }
}
