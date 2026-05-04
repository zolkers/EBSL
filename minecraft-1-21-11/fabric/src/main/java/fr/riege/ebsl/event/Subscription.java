package fr.riege.ebsl.event;

public final class Subscription {
    private final Class<? extends Event> eventType;
    private final EventHandler<?> handler;
    private final int priority;
    private final EventPhase phase;
    private volatile boolean active;

    public Subscription(Class<? extends Event> eventType,
                        EventHandler<?> handler,
                        int priority,
                        EventPhase phase) {
        this.eventType = eventType;
        this.handler = handler;
        this.priority = priority;
        this.phase = phase;
        this.active = true;
    }

    public Class<? extends Event> getEventType() {
        return eventType;
    }

    public EventHandler<?> getHandler() {
        return handler;
    }

    public int getPriority() {
        return priority;
    }

    public EventPhase getPhase() {
        return phase;
    }

    public boolean isActive() {
        return active;
    }

    public void deactivate() {
        this.active = false;
    }
}
