/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 *
 * Copyright (C) 2026 Victor Riegert and EBSL contributors
 *
 * This file is part of EBSL.
 *
 * EBSL is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * EBSL is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public
 * License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with EBSL. If not, see <https://www.gnu.org/licenses/>.
 */

package fr.riege.ebsl.common.feature.ui.imgui.settings;

import fr.riege.ebsl.common.core.settings.Setting;
import fr.riege.ebsl.common.core.settings.StringListSetting;
import imgui.ImGui;
import imgui.type.ImString;

final class StringListSettingRenderer implements ImGuiSettingRenderer {
    @Override
    public Class<StringListSetting> settingType() {
        return StringListSetting.class;
    }

    @Override
    public void render(Setting<?> raw, ImGuiSettingRenderContext context) {
        StringListSetting setting = (StringListSetting) raw;
        ImGui.text(setting.displayName());
        if (ImGui.beginChild("##" + context.key(setting), 0.0f, 124.0f, true)) {
            for (int i = 0; i < setting.value().size(); i++) {
                String key = context.key(setting, i);
                String entry = setting.value().get(i);
                ImString value = context.textValue(key, entry, 128);
                ImGui.pushItemWidth(-38.0f);
                if (ImGui.inputText("##entry-" + key, value)) {
                    setting.setEntry(i, value.get());
                    context.changed();
                }
                ImGui.popItemWidth();
                ImGui.sameLine();
                if (ImGui.button("X##delete-" + key, 24.0f, 22.0f)) {
                    setting.removeEntry(i);
                    context.removeTextValue(key);
                    context.changed();
                    break;
                }
            }
            ImGui.endChild();
        }
        if (ImGui.button("Add row##" + context.key(setting), 86.0f, 24.0f)) {
            setting.addEntry("minecraft:stone");
            context.changed();
        }
    }
}
