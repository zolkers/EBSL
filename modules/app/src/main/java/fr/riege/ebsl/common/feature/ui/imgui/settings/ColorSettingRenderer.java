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
