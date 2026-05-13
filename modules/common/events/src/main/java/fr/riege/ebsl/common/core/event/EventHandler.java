package fr.riege.ebsl.common.core.event;

/**
 * Consumes one strongly typed event dispatch.
 *
 * <p>Handlers should complete quickly and avoid mutating unrelated event state unless the event contract explicitly allows it.</p>
 */
@FunctionalInterface
public interface EventHandler<T extends Event> {
    /**
     * Handles the supplied event or context.
 *
     * @param event the event being published or handled
     */
    void handle(T event);
}
