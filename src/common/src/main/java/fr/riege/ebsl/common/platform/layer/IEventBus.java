package fr.riege.ebsl.common.platform.layer;

import fr.riege.ebsl.common.core.event.*;

import java.util.function.Consumer;

public interface IEventBus {
    void onTick(Consumer<TickEvent> handler);
    void onRenderWorld(Consumer<RenderWorldEvent> handler);
    void onRenderHud(Consumer<RenderHudEvent> handler);
    void onKeyPress(Consumer<KeyPressEvent> handler);
    void onMouseButton(Consumer<MouseButtonEvent> handler);
    void onCharTyped(Consumer<CharTypedEvent> handler);
    default <T extends Event> void subscribe(Class<T> type, EventHandler<T> handler) {}
    default <T extends Event> T post(T event) { return event; }
}
