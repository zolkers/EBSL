package fr.riege.ebsl.event;

public interface EventBus {
    <T extends Event> void post(T event);

    <T extends Event> void post(T event, EventPhase phase);

    void unsubscribe(Subscription subscription);

    void unsubscribeAll(Object owner);

    <T extends Event> Subscription subscribe(Class<T> type, EventHandler<T> handler);

    <T extends Event> Subscription subscribe(Class<T> type, Object owner, EventHandler<T> handler);

    <T extends Event> Subscription subscribe(Class<T> type, int priority, EventHandler<T> handler);

    <T extends Event> Subscription subscribe(Class<T> type, EventPhase phase, EventHandler<T> handler);

    <T extends Event> Subscription subscribe(Class<T> type, Object owner, int priority, EventHandler<T> handler);

    <T extends Event> Subscription subscribe(Class<T> type, Object owner, EventPhase phase, EventHandler<T> handler);

    <T extends Event> Subscription subscribe(Class<T> type, Object owner, int priority, EventPhase phase, EventHandler<T> handler);
}
