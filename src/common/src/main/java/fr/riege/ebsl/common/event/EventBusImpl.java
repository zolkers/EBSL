package fr.riege.ebsl.common.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public final class EventBusImpl implements EventBus {
    private static final Logger LOGGER = LoggerFactory.getLogger("ebsl-eventbus");

    private final Map<Class<? extends Event>, List<Subscription>> byType = new ConcurrentHashMap<>();
    private final Map<Object, List<Subscription>> byOwner = new ConcurrentHashMap<>();

    @Override
    public <T extends Event> void post(T event) {
        doPost(event, null);
    }

    @Override
    public <T extends Event> void post(T event, EventPhase phase) {
        doPost(event, phase);
    }

    @SuppressWarnings("unchecked")
    private <T extends Event> void doPost(T event, EventPhase phase) {
        if (event == null) {
            return;
        }

        List<Subscription> subscriptions = byType.get(event.getClass());
        if (subscriptions == null || subscriptions.isEmpty()) {
            return;
        }

        for (Subscription subscription : subscriptions) {
            if (!subscription.isActive()) {
                continue;
            }
            if (subscription.getPhase() != null && subscription.getPhase() != phase) {
                continue;
            }
            if (event.isCancelled()) {
                break;
            }
            try {
                ((EventHandler<T>) subscription.getHandler()).handle(event);
            } catch (Exception exception) {
                LOGGER.error("Exception in handler for {}", event.getClass().getSimpleName(), exception);
            }
        }
    }

    @Override
    public <T extends Event> Subscription subscribe(Class<T> type, EventHandler<T> handler) {
        return doSubscribe(type, null, EventPriority.NORMAL, null, handler);
    }

    @Override
    public <T extends Event> Subscription subscribe(Class<T> type, Object owner, EventHandler<T> handler) {
        return doSubscribe(type, owner, EventPriority.NORMAL, null, handler);
    }

    @Override
    public <T extends Event> Subscription subscribe(Class<T> type, int priority, EventHandler<T> handler) {
        return doSubscribe(type, null, priority, null, handler);
    }

    @Override
    public <T extends Event> Subscription subscribe(Class<T> type, EventPhase phase, EventHandler<T> handler) {
        return doSubscribe(type, null, EventPriority.NORMAL, phase, handler);
    }

    @Override
    public <T extends Event> Subscription subscribe(Class<T> type, Object owner, int priority, EventHandler<T> handler) {
        return doSubscribe(type, owner, priority, null, handler);
    }

    @Override
    public <T extends Event> Subscription subscribe(Class<T> type, Object owner, EventPhase phase, EventHandler<T> handler) {
        return doSubscribe(type, owner, EventPriority.NORMAL, phase, handler);
    }

    @Override
    public <T extends Event> Subscription subscribe(Class<T> type,
                                                    Object owner,
                                                    int priority,
                                                    EventPhase phase,
                                                    EventHandler<T> handler) {
        return doSubscribe(type, owner, priority, phase, handler);
    }

    private <T extends Event> Subscription doSubscribe(Class<T> type,
                                                       Object owner,
                                                       int priority,
                                                       EventPhase phase,
                                                       EventHandler<T> handler) {
        Subscription subscription = new Subscription(type, handler, priority, phase);
        byType.computeIfAbsent(type, ignored -> new CopyOnWriteArrayList<>()).add(subscription);
        sortByType(type);
        if (owner != null) {
            byOwner.computeIfAbsent(owner, ignored -> new CopyOnWriteArrayList<>()).add(subscription);
        }
        return subscription;
    }

    @Override
    public void unsubscribe(Subscription subscription) {
        if (subscription == null) {
            return;
        }
        subscription.deactivate();
        List<Subscription> subscriptions = byType.get(subscription.getEventType());
        if (subscriptions != null) {
            subscriptions.remove(subscription);
        }
        byOwner.values().forEach(ownerSubscriptions -> ownerSubscriptions.remove(subscription));
    }

    @Override
    public void unsubscribeAll(Object owner) {
        if (owner == null) {
            return;
        }
        List<Subscription> subscriptions = byOwner.remove(owner);
        if (subscriptions != null) {
            subscriptions.forEach(this::unsubscribe);
        }
    }

    private void sortByType(Class<? extends Event> type) {
        List<Subscription> subscriptions = byType.get(type);
        if (subscriptions != null && subscriptions.size() > 1) {
            subscriptions.sort((left, right) -> Integer.compare(right.getPriority(), left.getPriority()));
        }
    }
}
