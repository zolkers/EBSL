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

import fr.riege.ebsl.common.core.registry.IRegistry;
import fr.riege.ebsl.common.core.registry.MapRegistry;
import fr.riege.ebsl.common.core.settings.Setting;

public final class ImGuiSettingRendererRegistry {
    private static final IRegistry<Class<?>, ImGuiSettingRenderer> RENDERERS = new MapRegistry<>(null);

    static {
        register(new BooleanSettingRenderer());
        register(new IntSettingRenderer());
        register(new DoubleSettingRenderer());
        register(new StringSettingRenderer());
        register(new ColorSettingRenderer());
        register(new EnumSettingRenderer());
        register(new StringListSettingRenderer());
    }

    private ImGuiSettingRendererRegistry() {
    }

    public static void register(ImGuiSettingRenderer renderer) {
        RENDERERS.register(renderer.settingType(), renderer);
    }

    public static boolean render(Setting<?> setting, ImGuiSettingRenderContext context) {
        for (ImGuiSettingRenderer renderer : RENDERERS.values()) {
            if (renderer.settingType().isInstance(setting)) {
                renderer.render(setting, context);
                return true;
            }
        }
        return false;
    }
}
