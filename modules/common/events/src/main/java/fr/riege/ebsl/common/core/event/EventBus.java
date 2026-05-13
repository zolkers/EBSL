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

package fr.riege.ebsl.common.core.event;

/**
 * Coordinates publication and subscription for application events.
 *
 * <p>The bus supports owner-based cleanup, priority ordering, and phase-specific dispatch for gameplay and UI integrations.</p>
 */
public interface EventBus {
    /**
     * Publishes an event through this event source.
 *
     * @param event the event being published or handled
     */
    <T extends Event> void post(T event);

    /**
     * Publishes an event through this event source.
 *
     * @param event the event being published or handled
     * @param phase the event phase to target
     */
    <T extends Event> void post(T event, EventPhase phase);

    /**
     * Removes previously registered event subscriptions.
 *
     * @param subscription the subscription to remove
     */
    void unsubscribe(Subscription subscription);

    /**
     * Removes previously registered event subscriptions.
 *
     * @param owner the owner used for grouped subscription cleanup
     */
    void unsubscribeAll(Object owner);

    /**
     * Subscribes a handler to this event source.
 *
     * @param type the movement or event type being evaluated
     * @param handler the handler to register
     * @return the value defined by this contract
     */
    <T extends Event> Subscription subscribe(Class<T> type, EventHandler<T> handler);

    /**
     * Subscribes a handler to this event source.
 *
     * @param type the movement or event type being evaluated
     * @param owner the owner used for grouped subscription cleanup
     * @param handler the handler to register
     * @return the value defined by this contract
     */
    <T extends Event> Subscription subscribe(Class<T> type, Object owner, EventHandler<T> handler);

    /**
     * Subscribes a handler to this event source.
 *
     * @param type the movement or event type being evaluated
     * @param priority the dispatch priority
     * @param handler the handler to register
     * @return the value defined by this contract
     */
    <T extends Event> Subscription subscribe(Class<T> type, int priority, EventHandler<T> handler);

    /**
     * Subscribes a handler to this event source.
 *
     * @param type the movement or event type being evaluated
     * @param phase the event phase to target
     * @param handler the handler to register
     * @return the value defined by this contract
     */
    <T extends Event> Subscription subscribe(Class<T> type, EventPhase phase, EventHandler<T> handler);

    /**
     * Subscribes a handler to this event source.
 *
     * @param type the movement or event type being evaluated
     * @param owner the owner used for grouped subscription cleanup
     * @param priority the dispatch priority
     * @param handler the handler to register
     * @return the value defined by this contract
     */
    <T extends Event> Subscription subscribe(Class<T> type, Object owner, int priority, EventHandler<T> handler);

    /**
     * Subscribes a handler to this event source.
 *
     * @param type the movement or event type being evaluated
     * @param owner the owner used for grouped subscription cleanup
     * @param phase the event phase to target
     * @param handler the handler to register
     * @return the value defined by this contract
     */
    <T extends Event> Subscription subscribe(Class<T> type, Object owner, EventPhase phase, EventHandler<T> handler);

    /**
     * Subscribes a handler to this event source.
 *
     * @param type the movement or event type being evaluated
     * @param owner the owner used for grouped subscription cleanup
     * @param priority the dispatch priority
     * @param phase the event phase to target
     * @param handler the handler to register
     * @return the value defined by this contract
     */
    <T extends Event> Subscription subscribe(Class<T> type, Object owner, int priority, EventPhase phase, EventHandler<T> handler);
}
