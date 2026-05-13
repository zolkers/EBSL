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

/**
 * Renders one setting type inside ImGui configuration panels.
 *
 * <p>Implementations declare the setting class they support and update values through the provided render context.</p>
 */
interface ImGuiSettingRenderer {
    /**
     * Returns the concrete setting type rendered by this renderer.
 *
     * @return the requested values
     */
    Class<? extends Setting<?>> settingType();

    /**
     * Renders this component for the active frame using the supplied runtime context.
 *
     * @param setting the setting being rendered or updated
     * @param context the context describing the operation being performed
     */
    void render(Setting<?> setting, ImGuiSettingRenderContext context);
}
