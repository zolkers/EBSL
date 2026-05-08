package fr.riege.ebsl.common;

import fr.riege.ebsl.common.core.event.CommonEventTypes;
import fr.riege.ebsl.common.feature.module.BotModuleRegistry;
import fr.riege.ebsl.common.pathfinding.debug.PathVisualizer;
import fr.riege.ebsl.common.platform.EbslPlatform;
import fr.riege.ebsl.common.platform.render.RenderingSystem;
import fr.riege.ebsl.common.platform.service.EbslServices;
import fr.riege.ebsl.common.platform.service.NavigationService;
import fr.riege.ebsl.common.platform.service.UiService;
import fr.riege.ebsl.common.core.settings.CommonSettingsStore;
import fr.riege.ebsl.common.feature.task.BotTaskRegistry;
import fr.riege.ebsl.common.feature.terminal.*;
import fr.riege.ebsl.common.feature.ui.CommonImGuiOverlay;

import java.util.StringJoiner;

public class EbslCore {
    private final EbslPlatform platform;
    private long settingsSaveTick;

    public EbslCore(EbslPlatform platform, NavigationService navigationService, UiService uiService) {
        this.platform = platform;
        EbslServices.installPlatform(platform);
        EbslServices.install(navigationService, uiService);
        CommonEventTypes.bootstrap();
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
        platform.imgui().registerFrame(() -> {
            CommonImGuiOverlay.render(platform, navigationService, uiService);
        });
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
