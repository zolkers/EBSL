package fr.riege.ebsl.loader.layer;

import fr.riege.ebsl.common.event.CharTypedEvent;
import fr.riege.ebsl.common.event.Event;
import fr.riege.ebsl.common.event.EventBus;
import fr.riege.ebsl.common.event.EventBusImpl;
import fr.riege.ebsl.common.event.EventHandler;
import fr.riege.ebsl.common.event.KeyPressEvent;
import fr.riege.ebsl.common.event.MouseButtonEvent;
import fr.riege.ebsl.common.event.RenderHudEvent;
import fr.riege.ebsl.common.event.RenderWorldEvent;
import fr.riege.ebsl.common.event.TickEvent;
import fr.riege.ebsl.common.layer.IEventBus;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public final class ModloaderEventBus implements IEventBus {
    private final EventBus typedBus = new EventBusImpl();
    private final List<Consumer<TickEvent>> tickHandlers = new CopyOnWriteArrayList<>();
    private final List<Consumer<RenderWorldEvent>> renderWorldHandlers = new CopyOnWriteArrayList<>();
    private final List<Consumer<RenderHudEvent>> renderHudHandlers = new CopyOnWriteArrayList<>();
    private final List<Consumer<KeyPressEvent>> keyPressHandlers = new CopyOnWriteArrayList<>();
    private final List<Consumer<MouseButtonEvent>> mouseButtonHandlers = new CopyOnWriteArrayList<>();
    private final List<Consumer<CharTypedEvent>> charTypedHandlers = new CopyOnWriteArrayList<>();

    @Override public void onTick(Consumer<TickEvent> handler) { tickHandlers.add(handler); }
    @Override public void onRenderWorld(Consumer<RenderWorldEvent> handler) { renderWorldHandlers.add(handler); }
    @Override public void onRenderHud(Consumer<RenderHudEvent> handler) { renderHudHandlers.add(handler); }
    @Override public void onKeyPress(Consumer<KeyPressEvent> handler) { keyPressHandlers.add(handler); }
    @Override public void onMouseButton(Consumer<MouseButtonEvent> handler) { mouseButtonHandlers.add(handler); }
    @Override public void onCharTyped(Consumer<CharTypedEvent> handler) { charTypedHandlers.add(handler); }

    @Override public <T extends Event> void subscribe(Class<T> type, EventHandler<T> handler) {
        typedBus.subscribe(type, handler);
    }

    @Override public <T extends Event> T post(T event) {
        typedBus.post(event);
        return event;
    }

    public void fireTick(TickEvent event) {
        tickHandlers.forEach(handler -> handler.accept(event));
    }
    public void fireRenderWorld(RenderWorldEvent event) {
        renderWorldHandlers.forEach(handler -> handler.accept(event));
    }
    public void fireRenderHud(RenderHudEvent event) {
        renderHudHandlers.forEach(handler -> handler.accept(event));
    }
    public KeyPressEvent fireKeyPress(KeyPressEvent event) {
        keyPressHandlers.forEach(handler -> handler.accept(event));
        typedBus.post(event);
        return event;
    }
    public MouseButtonEvent fireMouseButton(MouseButtonEvent event) {
        mouseButtonHandlers.forEach(handler -> handler.accept(event));
        typedBus.post(event);
        return event;
    }
    public CharTypedEvent fireCharTyped(CharTypedEvent event) {
        charTypedHandlers.forEach(handler -> handler.accept(event));
        typedBus.post(event);
        return event;
    }
}
