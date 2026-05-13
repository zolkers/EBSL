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

public final class Subscription {
    private final Class<? extends Event> eventType;
    private final EventHandler<?> handler;
    private final int priority;
    private final EventPhase phase;
    private volatile boolean active;

    public Subscription(Class<? extends Event> eventType,
                        EventHandler<?> handler,
                        int priority,
                        EventPhase phase) {
        this.eventType = eventType;
        this.handler = handler;
        this.priority = priority;
        this.phase = phase;
        this.active = true;
    }

    public Class<? extends Event> getEventType() {
        return eventType;
    }

    @SuppressWarnings("java:S1452")
    public EventHandler<?> getHandler() {
        return handler;
    }

    public int getPriority() {
        return priority;
    }

    public EventPhase getPhase() {
        return phase;
    }

    public boolean isActive() {
        return active;
    }

    public void deactivate() {
        this.active = false;
    }
}
