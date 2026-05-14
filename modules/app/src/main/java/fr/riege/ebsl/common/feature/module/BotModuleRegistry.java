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

package fr.riege.ebsl.common.feature.module;

import fr.riege.ebsl.common.core.registry.IRegistry;
import fr.riege.ebsl.common.core.registry.MapRegistry;
import fr.riege.ebsl.common.core.settings.Setting;
import fr.riege.ebsl.common.core.threading.EbslThreadDomain;
import fr.riege.ebsl.common.core.threading.EbslThreading;
import fr.riege.ebsl.common.feature.module.blacklist.PathfinderBlockBlacklistModule;
import fr.riege.ebsl.common.feature.module.overlay.BlockTargetModule;
import fr.riege.ebsl.common.feature.module.overlay.KeyDisplayModule;
import fr.riege.ebsl.common.feature.module.overlay.MoveTypeOverlayModule;
import fr.riege.ebsl.common.feature.ui.layout.UiRect;
import fr.riege.ebsl.common.platform.EbslPlatform;
import fr.riege.ebsl.common.platform.layer.IEventBus;
import fr.riege.ebsl.common.platform.service.NavigationService;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class BotModuleRegistry {
    private static final IRegistry<String, PathfinderModule> MODULES = new MapRegistry<>(null);
    private static final Map<String, Boolean> lastEnabled = new HashMap<>();
    private static final Set<String> runningAsyncRenders = ConcurrentHashMap.newKeySet();
    private static IEventBus bus;

    private BotModuleRegistry() {}

    public static void bootstrap(IEventBus eventBus) {
        bus = eventBus;
        if (MODULES.get(PathfinderBlockBlacklistModule.INSTANCE.id()) == null) {
            register(PathfinderBlockBlacklistModule.INSTANCE);
        }
        registerIfAbsent(KeyDisplayModule.INSTANCE);
        registerIfAbsent(BlockTargetModule.INSTANCE);
        registerIfAbsent(MoveTypeOverlayModule.INSTANCE);
        for (PathfinderModule module : MODULES.values()) {
            lastEnabled.put(module.id(), module.isEnabled());
            if (module.isEnabled()) {
                runLifecycle(module.id() + ".onEnable", () -> module.onEnable(bus));
            }
        }
    }

    public static void register(PathfinderModule module) {
        MODULES.register(module.id(), module);
    }

    private static void registerIfAbsent(PathfinderModule module) {
        if (MODULES.get(module.id()) == null) {
            register(module);
        }
    }

    public static void onSettingChanged(PathfinderModule module, Setting<?> setting) {
        module.onSettingChanged(setting);
        syncLifecycle(module);
    }

    public static void notifySettingChanged(PathfinderModule module, Setting<?> setting) {
        onSettingChanged(module, setting);
    }

    public static void syncLifecycle(PathfinderModule module) {
        boolean isEnabled = module.isEnabled();
        Boolean was = lastEnabled.put(module.id(), isEnabled);
        if (was == null || was == isEnabled) return;
        if (isEnabled) {
            runLifecycle(module.id() + ".onEnable", () -> module.onEnable(bus));
        } else {
            runLifecycle(module.id() + ".onDisable", module::onDisable);
        }
    }

    public static void resetToDefaultsAndSave(PathfinderModule module) {
        module.resetSettings();
    }

    public static Collection<PathfinderModule> modules() {
        return MODULES.values();
    }

    public static PathfinderModule get(String id) {
        return MODULES.get(id);
    }

    public static void renderGameViewport(EbslPlatform platform, NavigationService navigation, UiRect viewport) {
        for (PathfinderModule module : MODULES.values()) {
            if (module.isEnabled()) {
                renderModuleGameViewport(platform, navigation, viewport, module);
            }
        }
    }

    private static void renderModuleGameViewport(EbslPlatform platform, NavigationService navigation, UiRect viewport,
                                                 PathfinderModule module) {
        if (!module.renderGameViewportAsync()) {
            runLifecycle(module.id() + ".renderGameViewport",
                () -> module.renderGameViewport(platform, navigation, viewport));
            return;
        }
        if (!runningAsyncRenders.add(module.id())) {
            return;
        }
        EbslThreading.modules()
            .run(module.id() + ".renderGameViewport", () -> module.renderGameViewport(platform, navigation, viewport))
            .whenComplete((unused, throwable) -> runningAsyncRenders.remove(module.id()));
    }

    private static void runLifecycle(String owner, Runnable action) {
        try {
            action.run();
        } catch (RuntimeException exception) {
            EbslThreading.report(EbslThreadDomain.MODULES, owner, exception);
        }
    }
}
