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
package fr.riege.ebsl.common.feature.ui.imgui.panel;

import fr.riege.ebsl.common.feature.ui.layout.ViewportLayout;
import fr.riege.ebsl.common.feature.ui.state.EbslUiState;
import fr.riege.ebsl.common.platform.EbslPlatform;
import fr.riege.ebsl.common.platform.service.NavigationService;

/**
 * Renders one ImGui panel in the EBSL overlay shell.
 *
 * <p>Panels receive shared UI state, layout geometry, navigation state, and platform services each frame.</p>
 */
public interface ImGuiUiPanel {
    /**
     * Renders this component for the active frame using the supplied runtime context.
 *
     * @param state the current UI state
     * @param layout the current viewport layout
     * @param navigation the navigation service used by the component
     * @param platform the platform services available to the component
     */
    void render(EbslUiState state, ViewportLayout layout, NavigationService navigation, EbslPlatform platform);
}
