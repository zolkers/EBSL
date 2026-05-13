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
package fr.riege.ebsl.common.api.core.events;

import fr.riege.ebsl.common.api.core.annotation.EbslApiOperation;
import fr.riege.ebsl.common.api.core.annotation.EbslApiSurface;
import fr.riege.ebsl.common.core.event.Event;
import fr.riege.ebsl.common.core.event.EventRegistry;
import fr.riege.ebsl.common.platform.layer.IEventBus;
import fr.riege.ebsl.common.platform.service.EbslServices;

import java.util.List;

@EbslApiSurface(EbslApiSurface.Domain.EVENTS)
public final class EventsApi {
    @EbslApiOperation("Read the active platform event bus.")
    public IEventBus bus() {
        return EbslServices.platform().events();
    }

    @EbslApiOperation("Post an event to the active platform event bus.")
    public <T extends Event> T post(T event) {
        return bus().post(event);
    }

    @EbslApiOperation("Read registered event metadata.")
    public List<EventRegistry.Entry> registeredEvents() {
        return EventRegistry.all();
    }
}
