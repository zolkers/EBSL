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
