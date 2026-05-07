package fr.riege.ebsl.common.core.event;

@FunctionalInterface
public interface EventHandler<T extends Event> {
    void handle(T event);
}
