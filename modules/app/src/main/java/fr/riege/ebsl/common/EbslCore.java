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
package fr.riege.ebsl.common;

import fr.riege.ebsl.common.core.event.CommonEventTypes;
import fr.riege.ebsl.common.core.settings.CommonSettingsStore;
import fr.riege.ebsl.common.domain.analytics.AnalyticsEventLog;
import fr.riege.ebsl.common.feature.module.BotModuleRegistry;
import fr.riege.ebsl.common.feature.task.BotTaskRegistry;
import fr.riege.ebsl.common.feature.terminal.*;
import fr.riege.ebsl.common.feature.ui.CommonImGuiOverlay;
import fr.riege.ebsl.common.pathfinding.debug.PathVisualizer;
import fr.riege.ebsl.common.pathfinding.diagnostics.PathfindingDiagnostics;
import fr.riege.ebsl.common.platform.EbslPlatform;
import fr.riege.ebsl.common.platform.render.RenderingSystem;
import fr.riege.ebsl.common.platform.service.EbslServices;
import fr.riege.ebsl.common.platform.service.NavigationService;
import fr.riege.ebsl.common.platform.service.UiService;

import java.util.StringJoiner;

public class EbslCore {
    private final EbslPlatform platform;
    private long settingsSaveTick;

    public EbslCore(EbslPlatform platform, NavigationService navigationService, UiService uiService) {
        this.platform = platform;
        EbslServices.installPlatform(platform);
        EbslServices.install(navigationService, uiService);
        CommonEventTypes.bootstrap();
        PathfindingDiagnostics.setTelemetrySink(AnalyticsEventLog::recordAnalytics);
        CommonSettingsStore.load(platform.storage());
        TerminalCommands.bootstrap();
        BotTaskRegistry.bootstrap();
        BotModuleRegistry.bootstrap(platform.events());
        CommonSettingsStore.loadBotSettings(platform.storage());
        registerCommands(platform);
        platform.input().registerUnfocusKeybind(() -> {
            platform.input().releaseMouse();
            if (uiService.isVisible() && !navigationService.isNavigating()) {
                platform.input().releaseGameplayKeys();
            }
        });
        platform.events().onTick(event -> navigationService.tick());
        platform.events().onTick(event -> BotTaskRegistry.update(platform));
        platform.events().onTick(event -> RenderingSystem.tick());
        platform.events().onRenderWorld(event -> {
            if (RenderingSystem.enabled()) {
                BotTaskRegistry.render(platform);
            }
        });
        platform.events().onTick(event -> {
            if (uiService.isVisible() && !platform.input().isMouseGrabbed() && !navigationService.isNavigating()) {
                platform.input().releaseGameplayKeys();
            }
        });
        platform.events().onTick(event -> autosaveSettings(platform, event.tick()));
        platform.events().onRenderWorld(event -> {
            platform.render().beginFrame(
                event.camX(),
                event.camY(),
                event.camZ(),
                event.viewMatrix(),
                event.projMatrix());
            RenderingSystem.renderWorld(platform.render());
            if (RenderingSystem.enabled()) {
                PathVisualizer.renderWorld(platform.render());
            }
            platform.render().endFrame();
        });
        platform.imgui().registerFrame(() -> CommonImGuiOverlay.render(platform, navigationService, uiService));
    }

    public EbslPlatform platform() {
        return platform;
    }

    private static void registerCommands(EbslPlatform platform) {
        for (CommandMeta meta : CommandRegistry.allMeta()) {
            if (meta.scope() == CommandScope.TERMINAL) {
                continue;
            }
            platform.commands().register(meta.name(), meta.description(), (args, output) -> {
                CommandResult result = CommandRegistry.dispatch(commandLine(meta.name(), args));
                for (String line : result.lines()) {
                    if (result.success()) {
                        platform.commands().printSuccess(line);
                    } else {
                        platform.commands().printError(line);
                    }
                }
            });
        }
    }

    private static String commandLine(String name, String[] args) {
        StringJoiner joiner = new StringJoiner(" ");
        joiner.add(name);
        for (String arg : args) {
            joiner.add(arg);
        }
        return joiner.toString();
    }

    private void autosaveSettings(EbslPlatform platform, long tick) {
        if (tick - settingsSaveTick < 100) {
            return;
        }
        settingsSaveTick = tick;
        CommonSettingsStore.save(platform.storage());
    }
}
