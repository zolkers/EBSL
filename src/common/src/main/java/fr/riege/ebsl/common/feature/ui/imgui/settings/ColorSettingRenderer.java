package fr.riege.ebsl.common.feature.ui.imgui.settings;

import fr.riege.ebsl.common.core.settings.ColorSetting;
import fr.riege.ebsl.common.core.settings.Setting;
import imgui.ImGui;

final class ColorSettingRenderer implements ImGuiSettingRenderer {
    @Override
    public Class<ColorSetting> settingType() {
        return ColorSetting.class;
    }

    @Override
    public void render(Setting<?> raw, ImGuiSettingRenderContext context) {
        ColorSetting setting = (ColorSetting) raw;
        int argb = setting.value();
        float[] color = {
            ((argb >> 16) & 0xFF) / 255.0f,
            ((argb >> 8) & 0xFF) / 255.0f,
            (argb & 0xFF) / 255.0f,
            ((argb >> 24) & 0xFF) / 255.0f
        };
        if (ImGui.colorEdit4(context.label(setting), color)) {
            int packed = ((int) (color[3] * 255) << 24)
                | ((int) (color[0] * 255) << 16)
                | ((int) (color[1] * 255) << 8)
                | (int) (color[2] * 255);
            setting.setValue(packed);
            context.changed();
        }
    }
}
