package fr.riege.ebsl.common;

import fr.riege.ebsl.common.platform.EbslPlatform;
import fr.riege.ebsl.common.event.CommonEventTypes;
import fr.riege.ebsl.common.settings.CommonSettingsStore;
import fr.riege.ebsl.common.service.EbslServices;
import fr.riege.ebsl.common.service.NavigationService;
import fr.riege.ebsl.common.service.UiService;
import fr.riege.ebsl.common.terminal.CommandMeta;
import fr.riege.ebsl.common.terminal.CommandRegistry;
import fr.riege.ebsl.common.terminal.CommandResult;
import fr.riege.ebsl.common.terminal.CommandScope;
import fr.riege.ebsl.common.terminal.TerminalCommands;
import fr.riege.ebsl.common.ui.CommonImGuiOverlay;

import java.util.StringJoiner;

public class EbslCore {
    private final EbslPlatform platform;
    private long settingsSaveTick;

    public EbslCore(EbslPlatform platform) {
        this(platform, NoopNavigationService.INSTANCE, NoopUiService.INSTANCE);
    }

    public EbslCore(EbslPlatform platform, NavigationService navigationService, UiService uiService) {
        this.platform = platform;
        EbslServices.installPlatform(platform);
        EbslServices.install(navigationService, uiService);
        CommonEventTypes.bootstrap();
        CommonSettingsStore.load(platform.storage());
        TerminalCommands.bootstrap();
        registerCommands(platform);
        platform.input().registerUnfocusKeybind(() -> {
            platform.input().releaseMouse();
            if (uiService.isVisible()) {
                platform.input().releaseGameplayKeys();
            }
        });
        platform.events().onTick(event -> navigationService.tick());
        platform.events().onTick(event -> {
            if (uiService.isVisible() && !platform.input().isMouseGrabbed()) {
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

    private enum NoopNavigationService implements NavigationService {
        INSTANCE;

        @Override public void startBlockGoal(int x, int y, int z) {}
        @Override public void startColumnGoal(int x, int z) {}
        @Override public void startPathTest(int x, int y, int z) {}
        @Override public void startPathTestXZ(int x, int z) {}
        @Override public void stop(boolean announce) {}
        @Override public boolean isNavigating() { return false; }
        @Override public fr.riege.ebsl.common.pathfinding.Node.MoveType currentMoveType() {
            return fr.riege.ebsl.common.pathfinding.Node.MoveType.WALK;
        }
        @Override public boolean isWalkSneakLatched() { return false; }
        @Override public void setWalkSneakLatched(boolean value) {}
    }

    private enum NoopUiService implements UiService {
        INSTANCE;

        @Override public boolean toggle() { return false; }
        @Override public boolean isVisible() { return false; }
    }
}
