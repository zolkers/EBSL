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
