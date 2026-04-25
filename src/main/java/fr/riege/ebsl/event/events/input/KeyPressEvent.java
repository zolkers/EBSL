package fr.riege.ebsl.event.events.input;

import fr.riege.ebsl.event.Event;
import net.minecraft.client.input.KeyEvent;

public final class KeyPressEvent extends Event {
    private final long window;
    private final int key;
    private final KeyEvent keyEvent;

    public KeyPressEvent(long window, int key, KeyEvent keyEvent) {
        this.window = window;
        this.key = key;
        this.keyEvent = keyEvent;
    }

    public long getWindow() { return window; }
    public int getKey() { return key; }
    public KeyEvent getKeyEvent() { return keyEvent; }
}
