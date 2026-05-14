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

package fr.riege.ebsl.common.feature.registry;

import fr.riege.ebsl.common.core.settings.Setting;
import fr.riege.ebsl.common.feature.module.BotModuleRegistry;
import fr.riege.ebsl.common.feature.module.PathfinderModule;
import fr.riege.ebsl.common.feature.ui.layout.UiRect;
import fr.riege.ebsl.common.platform.EbslPlatform;
import fr.riege.ebsl.common.platform.layer.IEventBus;
import fr.riege.ebsl.common.platform.service.NavigationService;

import java.util.Collection;

public final class ModuleCatalog {
    ModuleCatalog() {
    }

    public void bootstrap(IEventBus eventBus) {
        BotModuleRegistry.bootstrap(eventBus);
    }

    public Collection<PathfinderModule> all() {
        return BotModuleRegistry.modules();
    }

    public PathfinderModule get(String id) {
        return BotModuleRegistry.get(id);
    }

    public void settingChanged(PathfinderModule module, Setting<?> setting) {
        BotModuleRegistry.onSettingChanged(module, setting);
    }

    public void notifySettingChanged(PathfinderModule module, Setting<?> setting) {
        BotModuleRegistry.notifySettingChanged(module, setting);
    }

    public void syncLifecycle(PathfinderModule module) {
        BotModuleRegistry.syncLifecycle(module);
    }

    public void resetToDefaultsAndSave(PathfinderModule module) {
        BotModuleRegistry.resetToDefaultsAndSave(module);
    }

    public void renderGameViewport(EbslPlatform platform, NavigationService navigation, UiRect viewport) {
        BotModuleRegistry.renderGameViewport(platform, navigation, viewport);
    }
}
