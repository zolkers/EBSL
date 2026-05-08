package fr.riege.ebsl.common.feature.ui.imgui.settings;

import fr.riege.ebsl.common.core.settings.StringSetting;
import fr.riege.ebsl.common.core.settings.Setting;
import imgui.ImGui;
import imgui.type.ImString;

final class StringSettingRenderer implements ImGuiSettingRenderer {
    @Override
    public Class<StringSetting> settingType() {
        return StringSetting.class;
    }

    @Override
    public void render(Setting<?> raw, ImGuiSettingRenderContext context) {
        StringSetting setting = (StringSetting) raw;
        ImString value = context.textValue(context.key(setting), setting.value(), 512);
        context.applyItemWidth();
        if (ImGui.inputText(context.label(setting), value)) {
            setting.setValue(value.get());
            context.changed();
        }
    }
}
