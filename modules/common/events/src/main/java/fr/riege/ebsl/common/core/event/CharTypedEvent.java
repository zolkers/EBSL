package fr.riege.ebsl.common.core.event;

public final class CharTypedEvent extends Event {
    private final long windowHandle;
    private final char character;

    public CharTypedEvent(long windowHandle, char character) {
        this.windowHandle = windowHandle;
        this.character = character;
    }

    public long windowHandle() { return windowHandle; }
    public char character() { return character; }
}
