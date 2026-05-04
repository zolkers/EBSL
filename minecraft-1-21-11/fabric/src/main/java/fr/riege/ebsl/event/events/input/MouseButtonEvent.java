package fr.riege.ebsl.event.events.input;

import fr.riege.ebsl.event.Event;
import net.minecraft.client.input.MouseButtonInfo;

public final class MouseButtonEvent extends Event {
    private final long windowHandle;
    private final MouseButtonInfo button;
    private final int action;

    public MouseButtonEvent(long windowHandle, MouseButtonInfo button, int action) {
        this.windowHandle = windowHandle;
        this.button = button;
        this.action = action;
    }

    public long getWindowHandle() { return windowHandle; }
    public MouseButtonInfo getButton() { return button; }
    public int getAction() { return action; }
}
