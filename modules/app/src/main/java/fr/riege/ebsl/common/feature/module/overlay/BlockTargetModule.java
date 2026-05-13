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
package fr.riege.ebsl.common.feature.module.overlay;

import fr.riege.ebsl.common.domain.world.BlockId;
import fr.riege.ebsl.common.feature.ui.layout.UiRect;
import fr.riege.ebsl.common.platform.EbslPlatform;
import fr.riege.ebsl.common.platform.service.NavigationService;
import imgui.ImGui;

@SuppressWarnings("java:S6548")
public final class BlockTargetModule extends AbstractAnchoredOverlayModule {
    public static final BlockTargetModule INSTANCE = new BlockTargetModule();

    private BlockTargetModule() {
        super(
            "block_target",
            "Block Target",
            "Shows the resource ID of the block currently looked at.",
            KeyDisplayAnchor.TOP_RIGHT);
    }

    @Override
    public void renderGameViewport(EbslPlatform platform, NavigationService navigation, UiRect viewport) {
        if (!isEnabled()) return;
        BlockId target = platform.player().targetedBlock();
        if (target == null) return;
        MoveTypeOverlayModule.renderBox(ImGui.getForegroundDrawList(), viewport, anchor(), target.toString());
    }
}
