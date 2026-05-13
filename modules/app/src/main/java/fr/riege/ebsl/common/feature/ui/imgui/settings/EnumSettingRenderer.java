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

import fr.riege.ebsl.common.core.settings.EnumSetting;
import fr.riege.ebsl.common.core.settings.Setting;
import imgui.ImGui;
import imgui.type.ImInt;

import java.util.Locale;

final class EnumSettingRenderer implements ImGuiSettingRenderer {
    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public Class<? extends Setting<?>> settingType() {
        return (Class<? extends Setting<?>>) (Class<?>) EnumSetting.class;
    }

    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public void render(Setting<?> raw, ImGuiSettingRenderContext context) {
        EnumSetting setting = (EnumSetting) raw;
        Enum[] values = (Enum[]) setting.enumType().getEnumConstants();
        String[] labels = new String[values.length];
        for (int i = 0; i < values.length; i++) {
            labels[i] = values[i].name().toLowerCase(Locale.ROOT);
        }
        ImInt selected = new ImInt(((Enum<?>) setting.value()).ordinal());
        context.applyItemWidth();
        if (ImGui.combo(context.label(setting), selected, labels)) {
            setting.setValue(values[selected.get()]);
            context.changed();
        }
    }
}
