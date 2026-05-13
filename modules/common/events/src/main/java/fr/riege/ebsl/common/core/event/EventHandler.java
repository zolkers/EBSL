package fr.riege.ebsl.common.core.event;

/**
 * Defines the contract for {@code EventHandler} implementations.
 */
@FunctionalInterface
public interface EventHandler<T extends Event> {
    void handle(T event);
}
