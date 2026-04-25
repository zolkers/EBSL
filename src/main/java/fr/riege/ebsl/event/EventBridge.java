package fr.riege.ebsl.event;

import fr.riege.ebsl.event.events.game.TickEvent;
import fr.riege.ebsl.event.events.network.NetworkPacketEvent;
import fr.riege.ebsl.event.events.render.RenderHudEvent;
import fr.riege.ebsl.event.events.render.RenderWorldEvent;
import fr.riege.ebsl.packet.PacketCaptureLog;
import fr.riege.ebsl.ui.imgui.DockingInputHandler;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;

public final class EventBridge {
    private final EventBus bus;
    private long tickCount;

    public EventBridge(EventBus bus) {
        this.bus = bus;
    }

    public void register() {
        registerAll();
        DockingInputHandler.register(bus);
        bus.subscribe(NetworkPacketEvent.class, e -> PacketCaptureLog.record(e.getDirection(), e.getPacket()));
        ClientTickEvents.START_CLIENT_TICK.register(client -> {
            tickCount++;
            bus.post(new TickEvent(client, tickCount), EventPhase.PRE);
        });
        ClientTickEvents.END_CLIENT_TICK.register(client ->
            bus.post(new TickEvent(client, tickCount), EventPhase.POST));
        HudRenderCallback.EVENT.register((graphics, deltaTracker) ->
            bus.post(new RenderHudEvent(graphics, deltaTracker.getGameTimeDeltaPartialTick(true))));
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> tickCount = 0L);
    }

    private static void registerAll() {
        EventRegistry.register(TickEvent.class);
        EventRegistry.register(RenderHudEvent.class);
        EventRegistry.register(RenderWorldEvent.class);
    }
}
