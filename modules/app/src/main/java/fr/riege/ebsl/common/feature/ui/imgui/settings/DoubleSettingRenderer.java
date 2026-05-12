package fr.riege.ebsl.common.feature.ui.imgui.settings;

import fr.riege.ebsl.common.core.settings.DoubleSetting;
import fr.riege.ebsl.common.core.settings.Setting;
import imgui.ImGui;
import imgui.type.ImDouble;

final class DoubleSettingRenderer implements ImGuiSettingRenderer {
    @Override
    public Class<DoubleSetting> settingType() {
        return DoubleSetting.class;
    }

    @Override
    public void render(Setting<?> raw, ImGuiSettingRenderContext context) {
        DoubleSetting setting = (DoubleSetting) raw;
        ImDouble value = new ImDouble(setting.value());
        context.applyItemWidth();
        if (ImGui.inputDouble(context.label(setting), value, 0.1, 1.0, "%.3f")) {
            setting.setValue(Math.clamp(value.get(), setting.min(), setting.max()));
            context.changed();
        }
    }
}
