package fr.riege.ebsl.common.event;

import fr.riege.ebsl.common.event.events.input.GrabMouseEvent;
import fr.riege.ebsl.common.packet.PacketCaptureEvent;

public final class CommonEventTypes {
    private static boolean registered;

    private CommonEventTypes() {
    }

    public static void bootstrap() {
        if (registered) {
            return;
        }
        registered = true;
        EventRegistry.register(TickEvent.class);
        EventRegistry.register(RenderWorldEvent.class);
        EventRegistry.register(RenderHudEvent.class);
        EventRegistry.register(KeyPressEvent.class);
        EventRegistry.register(MouseButtonEvent.class);
        EventRegistry.register(CharTypedEvent.class);
        EventRegistry.register(ScaledMousePosEvent.class);
        EventRegistry.register(BlitToScreenEvent.class);
        EventRegistry.register(GrabMouseEvent.class);
        EventRegistry.register(PacketCaptureEvent.class);
    }
}
