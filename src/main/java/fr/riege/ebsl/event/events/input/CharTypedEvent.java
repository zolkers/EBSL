package fr.riege.ebsl.event.events.input;

import fr.riege.ebsl.event.Event;
import net.minecraft.client.input.CharacterEvent;

public final class CharTypedEvent extends Event {
    private final long window;
    private final CharacterEvent charEvent;

    public CharTypedEvent(long window, CharacterEvent charEvent) {
        this.window = window;
        this.charEvent = charEvent;
    }

    public long getWindow() { return window; }
    public CharacterEvent getCharEvent() { return charEvent; }
}
