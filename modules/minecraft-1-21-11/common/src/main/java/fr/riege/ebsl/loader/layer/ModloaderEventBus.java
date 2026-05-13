/*
 * This file is part of EBSL.
 *
 * EBSL is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * EBSL is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package fr.riege.ebsl.loader.layer;

import fr.riege.ebsl.common.core.event.*;
import fr.riege.ebsl.common.platform.layer.IEventBus;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public final class ModloaderEventBus implements IEventBus {
    private final EventBus typedBus = EventBuses.create();
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
