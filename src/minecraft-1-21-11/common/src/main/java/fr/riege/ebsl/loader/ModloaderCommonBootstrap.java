package fr.riege.ebsl.loader;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.Window;
import fr.riege.ebsl.common.EbslCore;
import fr.riege.ebsl.common.event.CharTypedEvent;
import fr.riege.ebsl.common.event.KeyPressEvent;
import fr.riege.ebsl.common.event.MouseButtonEvent;
import fr.riege.ebsl.common.event.RenderHudEvent;
import fr.riege.ebsl.common.event.RenderWorldEvent;
import fr.riege.ebsl.common.event.TickEvent;
import fr.riege.ebsl.common.layer.ICommandLayer;
import fr.riege.ebsl.common.layer.IImGuiLayer;
import fr.riege.ebsl.common.layer.IInputLayer;
import fr.riege.ebsl.common.layer.IPhysicsLayer;
import fr.riege.ebsl.common.platform.EbslPlatform;
import fr.riege.ebsl.loader.layer.ModloaderEventBus;
import fr.riege.ebsl.loader.layer.ModloaderNavigationService;
import fr.riege.ebsl.loader.layer.ModloaderUiService;
import fr.riege.ebsl.loader.layer.MinecraftImGuiLayer;
import fr.riege.ebsl.loader.ui.DockingInputHandler;
import fr.riege.ebsl.mc.McPlayerLayer;
import fr.riege.ebsl.mc.McRenderLayer;
import fr.riege.ebsl.mc.McStorageLayer;
import fr.riege.ebsl.mc.McWorldLayer;
import net.minecraft.client.Minecraft;
import org.joml.Matrix4f;

import java.nio.file.Path;

public final class ModloaderCommonBootstrap {
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
        navigation = new ModloaderNavigationService(world, player, physics);
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
            .build();

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
        return events != null && events.post(new fr.riege.ebsl.common.event.events.input.GrabMouseEvent()).isCancelled();
    }

    public static boolean onMouseButton(long windowHandle, int button, int action) {
        return events != null && events.fireMouseButton(new MouseButtonEvent(windowHandle, button, action)).isCancelled();
    }

    public static double remapScaledX(Window window, double rawX, double scaledX) {
        if (events == null) return scaledX;
        fr.riege.ebsl.common.event.ScaledMousePosEvent event =
            events.post(new fr.riege.ebsl.common.event.ScaledMousePosEvent(rawX, scaledX, fr.riege.ebsl.common.event.ScaledMousePosEvent.Axis.X));
        return event.scaledPos();
    }

    public static double remapScaledY(Window window, double rawY, double scaledY) {
        if (events == null) return scaledY;
        fr.riege.ebsl.common.event.ScaledMousePosEvent event =
            events.post(new fr.riege.ebsl.common.event.ScaledMousePosEvent(rawY, scaledY, fr.riege.ebsl.common.event.ScaledMousePosEvent.Axis.Y));
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

    private static float[] toArray(Matrix4f matrix) {
        float[] values = new float[16];
        matrix.get(values);
        return values;
    }
}
