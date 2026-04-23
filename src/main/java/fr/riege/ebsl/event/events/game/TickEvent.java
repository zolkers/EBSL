package fr.riege.ebsl.event.events.game;

import fr.riege.ebsl.event.Event;
import net.minecraft.client.Minecraft;

public final class TickEvent extends Event {
    private final Minecraft client;
    private final long tick;

    public TickEvent(Minecraft client, long tick) {
        this.client = client;
        this.tick = tick;
    }

    public Minecraft getClient() {
        return client;
    }

    public long getTick() {
        return tick;
    }
}
