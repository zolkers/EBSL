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

import fr.riege.ebsl.common.core.settings.EnumSetting;
import fr.riege.ebsl.common.feature.AbstractEnabledFeature;
import fr.riege.ebsl.common.feature.module.PathfinderModule;
import fr.riege.ebsl.common.feature.module.PathfinderModuleCategory;

abstract class AbstractAnchoredOverlayModule extends AbstractEnabledFeature implements PathfinderModule {
    private final EnumSetting<KeyDisplayAnchor> anchorSetting;

    AbstractAnchoredOverlayModule(String id, String displayName, String description, KeyDisplayAnchor defaultAnchor) {
        super(id, displayName, description);
        anchorSetting = registerSetting(new EnumSetting<>("anchor", "Position", defaultAnchor, KeyDisplayAnchor.class));
    }

    @Override
    public final PathfinderModuleCategory category() {
        return PathfinderModuleCategory.RENDER;
    }

    final KeyDisplayAnchor anchor() {
        return anchorSetting.value();
    }
}
