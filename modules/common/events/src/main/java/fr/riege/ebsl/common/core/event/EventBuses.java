package fr.riege.ebsl.common.core.event;

/**
 * Creates event bus instances behind the {@link EventBus} contract.
 */
public final class EventBuses {
    private EventBuses() {
    }

    /**
     * Creates the default in-memory event bus.
     *
     * @return a new event bus instance
     */
    public static EventBus create() {
        return new EventBusImpl();
    }
}
