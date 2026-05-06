package fr.riege.ebsl.loader.layer;

import fr.riege.ebsl.common.event.CharTypedEvent;
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

    public void fireTick(TickEvent event) { tickHandlers.forEach(handler -> handler.accept(event)); }
    public void fireRenderWorld(RenderWorldEvent event) { renderWorldHandlers.forEach(handler -> handler.accept(event)); }
    public void fireRenderHud(RenderHudEvent event) { renderHudHandlers.forEach(handler -> handler.accept(event)); }
    public void fireKeyPress(KeyPressEvent event) { keyPressHandlers.forEach(handler -> handler.accept(event)); }
    public void fireMouseButton(MouseButtonEvent event) { mouseButtonHandlers.forEach(handler -> handler.accept(event)); }
    public void fireCharTyped(CharTypedEvent event) { charTypedHandlers.forEach(handler -> handler.accept(event)); }
}
