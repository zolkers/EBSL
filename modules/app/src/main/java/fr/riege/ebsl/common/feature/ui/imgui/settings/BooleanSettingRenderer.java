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
