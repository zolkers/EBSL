package fr.riege.ebsl.common.event;

public final class KeyPressEvent extends Event {
    private final long windowHandle;
    private final int keyCode;
    private final int action;
    private final int modifiers;

    public KeyPressEvent(long windowHandle, int keyCode, int action, int modifiers) {
        this.windowHandle = windowHandle;
        this.keyCode = keyCode;
        this.action = action;
        this.modifiers = modifiers;
    }

    public long windowHandle() { return windowHandle; }
    public int keyCode() { return keyCode; }
    public int action() { return action; }
    public int modifiers() { return modifiers; }
}
