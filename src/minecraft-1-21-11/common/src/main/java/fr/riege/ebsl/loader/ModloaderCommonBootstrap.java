package fr.riege.ebsl.loader;

import fr.riege.ebsl.common.EbslCore;
import fr.riege.ebsl.common.event.CharTypedEvent;
import fr.riege.ebsl.common.event.KeyPressEvent;
import fr.riege.ebsl.common.event.MouseButtonEvent;
import fr.riege.ebsl.common.event.RenderWorldEvent;
import fr.riege.ebsl.common.event.TickEvent;
import fr.riege.ebsl.common.layer.ICommandLayer;
import fr.riege.ebsl.common.layer.IImGuiLayer;
import fr.riege.ebsl.common.layer.IPhysicsLayer;
import fr.riege.ebsl.common.platform.EbslPlatform;
import fr.riege.ebsl.loader.layer.ModloaderEventBus;
import fr.riege.ebsl.loader.layer.ModloaderNavigationService;
import fr.riege.ebsl.loader.layer.ModloaderUiService;
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
    private static long ticks;

    private ModloaderCommonBootstrap() {
    }

    public static void initialize(Minecraft client,
                                  Path configDir,
                                  IPhysicsLayer physics,
                                  ICommandLayer commands,
                                  IImGuiLayer imgui) {
        McWorldLayer world = new McWorldLayer(client);
        McPlayerLayer player = new McPlayerLayer(client);
        events = new ModloaderEventBus();
        navigation = new ModloaderNavigationService(world, player, physics);

        EbslPlatform platform = EbslPlatform.builder()
            .world(world)
            .player(player)
            .render(new McRenderLayer())
            .storage(new McStorageLayer(configDir))
            .physics(physics)
            .events(events)
            .commands(commands)
            .imgui(imgui)
            .build();

        new EbslCore(platform, navigation, new ModloaderUiService());
    }

    public static void tick() {
        if (events == null) return;
        events.fireTick(new TickEvent(++ticks));
    }

    public static void onRenderWorld(Matrix4f projection, double camX, double camY, double camZ) {
        if (events == null) return;
        events.fireRenderWorld(new RenderWorldEvent(new float[0], toArray(projection), 0.0f, camX, camY, camZ));
    }

    public static void onKeyPress(int keyCode, int action, int modifiers) {
        if (events != null) events.fireKeyPress(new KeyPressEvent(keyCode, action, modifiers));
    }

    public static void onMouseButton(int button, int action) {
        if (events != null) events.fireMouseButton(new MouseButtonEvent(button, action));
    }

    public static void onCharTyped(char character) {
        if (events != null) events.fireCharTyped(new CharTypedEvent(character));
    }

    private static float[] toArray(Matrix4f matrix) {
        float[] values = new float[16];
        matrix.get(values);
        return values;
    }
}
