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
