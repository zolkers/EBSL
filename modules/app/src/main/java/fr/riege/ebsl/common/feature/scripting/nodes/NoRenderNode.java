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

package fr.riege.ebsl.common.feature.scripting.nodes;

import fr.riege.ebsl.common.core.settings.BooleanSetting;
import fr.riege.ebsl.common.feature.scripting.EbslNodeInvocation;
import fr.riege.ebsl.common.feature.scripting.annotations.EbslNodeDefinition;
import fr.riege.ebsl.common.feature.scripting.enums.EbslNodeType;
import fr.riege.ebsl.common.platform.render.RenderingSystem;

@EbslNodeDefinition(value = EbslNodeType.NO_RENDER, aliases = {"disable_render", "render_off"})
public final class NoRenderNode extends AbstractEbslNode {
    @Override
    protected void registerSettings() {
        registerSetting(new BooleanSetting("disable", "Disable Rendering", true));
    }

    @Override
    public int start(EbslNodeInvocation invocation) {
        RenderingSystem.setEnabled(!disable(invocation.has(0) ? invocation.arg(0) : ""));
        return 0;
    }

    private static boolean disable(String token) {
        return NoRenderDirective.disablesRendering(token);
    }
}
