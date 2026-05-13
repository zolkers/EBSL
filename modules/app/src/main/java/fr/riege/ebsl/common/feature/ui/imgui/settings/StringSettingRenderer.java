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

import fr.riege.ebsl.common.core.settings.Setting;
import fr.riege.ebsl.common.core.settings.StringSetting;
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
