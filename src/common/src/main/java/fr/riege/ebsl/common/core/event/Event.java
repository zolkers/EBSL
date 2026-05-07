package fr.riege.ebsl.common.core.event;

public abstract class Event {
    private boolean cancelled;
    private final long timestamp;

    protected Event() {
        this.timestamp = System.currentTimeMillis();
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void cancel() {
        this.cancelled = true;
    }
}
