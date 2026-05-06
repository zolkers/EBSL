package fr.riege.ebsl.common.event;

public final class MouseButtonEvent extends Event {
    private final long windowHandle;
    private final int button;
    private final int action;

    public MouseButtonEvent(long windowHandle, int button, int action) {
        this.windowHandle = windowHandle;
        this.button = button;
        this.action = action;
    }

    public long windowHandle() { return windowHandle; }
    public int button() { return button; }
    public int action() { return action; }
}
