package fr.riege.ebsl.loader;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.Window;
import fr.riege.ebsl.common.EbslCore;
import fr.riege.ebsl.common.core.event.*;
import fr.riege.ebsl.common.platform.layer.ICommandLayer;
import fr.riege.ebsl.common.platform.layer.IImGuiLayer;
import fr.riege.ebsl.common.platform.layer.IInputLayer;
import fr.riege.ebsl.common.platform.layer.IPhysicsLayer;
import fr.riege.ebsl.common.core.log.AppLog;
import fr.riege.ebsl.common.platform.EbslPlatform;
import fr.riege.ebsl.common.platform.render.RenderingSystem;
import fr.riege.ebsl.loader.layer.MinecraftImGuiLayer;
import fr.riege.ebsl.loader.layer.ModloaderEventBus;
import fr.riege.ebsl.loader.layer.ModloaderNavigationService;
import fr.riege.ebsl.loader.layer.ModloaderUiService;
import fr.riege.ebsl.loader.ui.DockingInputHandler;
import fr.riege.ebsl.mc.*;
import net.minecraft.client.Minecraft;
import org.joml.Matrix4f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

public final class ModloaderCommonBootstrap {
    private static final Logger LOGGER = LoggerFactory.getLogger("ebsl-bootstrap");

    private static ModloaderEventBus events;
    private static ModloaderNavigationService navigation;
    private static MinecraftImGuiLayer imgui;
    private static Minecraft client;
    private static ModloaderUiService ui;
    private static DockingInputHandler docking;
    private static long ticks;

    private ModloaderCommonBootstrap() {
    }

    public static void initialize(Minecraft client,
                                  Path configDir,
                                  IPhysicsLayer physics,
                                  ICommandLayer commands,
                                  IImGuiLayer imgui,
                                  IInputLayer input) {
        McWorldLayer world = new McWorldLayer(client);
        McPlayerLayer player = new McPlayerLayer(client);
        ModloaderCommonBootstrap.client = client;
        events = new ModloaderEventBus();
        navigation = new ModloaderNavigationService(world, player, physics, input);
        ModloaderCommonBootstrap.imgui = imgui instanceof MinecraftImGuiLayer layer ? layer : null;
        ui = new ModloaderUiService();

        EbslPlatform platform = EbslPlatform.builder()
            .world(world)
            .player(player)
            .render(new McRenderLayer())
            .storage(new McStorageLayer(configDir))
            .physics(physics)
            .events(events)
            .commands(commands)
            .imgui(imgui)
            .input(input)
            .entities(new McEntityLayer(client))
            .build();

        bootstrapAppLog();
        new EbslCore(platform, navigation, ui);
        if (ModloaderCommonBootstrap.imgui != null) {
            docking = DockingInputHandler.register(events, client, ui, ModloaderCommonBootstrap.imgui);
        }
    }

    public static void tick() {
        if (events == null) return;
        events.fireTick(new TickEvent(++ticks));
    }

    public static void onRenderWorld(Matrix4f projection, double camX, double camY, double camZ) {
        if (events == null) return;
        events.fireRenderWorld(new RenderWorldEvent(new float[0], toArray(projection), 0.0f, camX, camY, camZ));
    }

    public static void beforeRenderWorld() {
        if (navigation != null && RenderingSystem.enabled()) {
            navigation.renderCameraFrame();
        }
    }

    public static void onRenderHud(int screenWidth, int screenHeight, float tickDelta) {
        if (events == null) return;
        events.fireRenderHud(new RenderHudEvent(screenWidth, screenHeight, tickDelta));
    }

    public static void onRenderImGui() {
        if (imgui != null && ui != null && ui.isVisible()) {
            imgui.drawFrame();
        }
    }

    public static void onBlitToScreen(RenderTarget target) {
        if (docking != null) docking.onBlitToScreen(target);
    }

    public static boolean onGrabMouse() {
        return events != null && events.post(new fr.riege.ebsl.common.core.event.events.input.GrabMouseEvent()).isCancelled();
    }

    public static boolean onMouseButton(long windowHandle, int button, int action) {
        return events != null && events.fireMouseButton(new MouseButtonEvent(windowHandle, button, action)).isCancelled();
    }

    public static double remapScaledX(Window window, double rawX, double scaledX) {
        if (events == null) return scaledX;
        fr.riege.ebsl.common.core.event.ScaledMousePosEvent event =
            events.post(new fr.riege.ebsl.common.core.event.ScaledMousePosEvent(rawX, scaledX, fr.riege.ebsl.common.core.event.ScaledMousePosEvent.Axis.X));
        return event.scaledPos();
    }

    public static double remapScaledY(Window window, double rawY, double scaledY) {
        if (events == null) return scaledY;
        fr.riege.ebsl.common.core.event.ScaledMousePosEvent event =
            events.post(new fr.riege.ebsl.common.core.event.ScaledMousePosEvent(rawY, scaledY, fr.riege.ebsl.common.core.event.ScaledMousePosEvent.Axis.Y));
        return event.scaledPos();
    }

    public static boolean onKeyPress(long windowHandle, int keyCode, int action, int modifiers) {
        return events != null && events.fireKeyPress(new KeyPressEvent(windowHandle, keyCode, action, modifiers)).isCancelled();
    }

    public static boolean onCharTyped(long windowHandle, char character) {
        return events != null && events.fireCharTyped(new CharTypedEvent(windowHandle, character)).isCancelled();
    }

    public static boolean shouldSuppressImGuiInput() {
        return docking != null && docking.shouldSuppressImGuiInput();
    }

    private static void bootstrapAppLog() {
        AppLog.setAppender(receiver -> {
            try {
                org.apache.logging.log4j.core.LoggerContext ctx =
                    (org.apache.logging.log4j.core.LoggerContext) org.apache.logging.log4j.LogManager.getContext(false);
                var config = ctx.getConfiguration();
                var appender = new org.apache.logging.log4j.core.appender.AbstractAppender(
                    "EbslUiAppender", null, null, true,
                    org.apache.logging.log4j.core.config.Property.EMPTY_ARRAY) {
                    @Override public void append(org.apache.logging.log4j.core.LogEvent event) {
                        String time = java.time.format.DateTimeFormatter
                            .ofPattern("HH:mm:ss")
                            .withZone(java.time.ZoneId.systemDefault())
                            .format(java.time.Instant.ofEpochMilli(event.getTimeMillis()));
                        String loggerName = event.getLoggerName();
                        int dot = loggerName.lastIndexOf('.');
                        String shortLogger = dot >= 0 ? loggerName.substring(dot + 1) : loggerName;
                        receiver.receive(new AppLog.LogEntry(time, event.getLevel().name(), shortLogger,
                            event.getMessage().getFormattedMessage()));
                    }
                };
                appender.start();
                config.getRootLogger().addAppender(appender, org.apache.logging.log4j.Level.INFO, null);
                ctx.updateLoggers();
            } catch (Exception exception) {
                LOGGER.debug("Could not attach EBSL UI log appender.", exception);
            }
        });
        AppLog.bootstrap();
    }

    private static float[] toArray(Matrix4f matrix) {
        float[] values = new float[16];
        matrix.get(values);
        return values;
    }
}
