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

package fr.riege.ebsl.common.platform.layer;

import fr.riege.ebsl.common.core.event.*;

import java.util.function.Consumer;

/**
 * Exposes platform event hooks needed by shared modules.
 *
 * <p>The layer bridges loader-specific events into stable callbacks for ticks, rendering, input, and generic event dispatch.</p>
 */
public interface IEventBus {
    /**
     * Handles the tick lifecycle callback.
 *
     * @param handler the handler to register
     */
    void onTick(Consumer<TickEvent> handler);
    /**
     * Handles the render world lifecycle callback.
 *
     * @param handler the handler to register
     */
    void onRenderWorld(Consumer<RenderWorldEvent> handler);
    /**
     * Handles the render hud lifecycle callback.
 *
     * @param handler the handler to register
     */
    void onRenderHud(Consumer<RenderHudEvent> handler);
    /**
     * Handles the key press lifecycle callback.
 *
     * @param handler the handler to register
     */
    void onKeyPress(Consumer<KeyPressEvent> handler);
    /**
     * Handles the mouse button lifecycle callback.
 *
     * @param handler the handler to register
     */
    void onMouseButton(Consumer<MouseButtonEvent> handler);
    /**
     * Handles the char typed lifecycle callback.
 *
     * @param handler the handler to register
     */
    void onCharTyped(Consumer<CharTypedEvent> handler);
    /**
     * Subscribes a handler to this event source.
 *
     * @param type the movement or event type being evaluated
     * @param handler the handler to register
     */
    default <T extends Event> void subscribe(Class<T> type, EventHandler<T> handler) {}
    /**
     * Publishes an event through this event source.
 *
     * @param event the event being published or handled
     * @return the value defined by this contract
     */
    default <T extends Event> T post(T event) { return event; }
}
