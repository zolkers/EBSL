package fr.riege.ebsl.api.events;

import fr.riege.ebsl.EbslMod;
import fr.riege.ebsl.api.annotation.EbslApiOperation;
import fr.riege.ebsl.api.annotation.EbslApiSurface;
import fr.riege.ebsl.event.Event;
import fr.riege.ebsl.event.EventBus;
import fr.riege.ebsl.event.EventRegistry;

import java.util.List;

@EbslApiSurface(EbslApiSurface.Domain.EVENTS)
public final class EventsApi {
    public EventsApi() {
    }

    @EbslApiOperation("Read the active client event bus.")
    public EventBus bus() {
        return EbslMod.events();
    }

    @EbslApiOperation("Post a client event.")
    public <T extends Event> T post(T event) {
        return EbslMod.postClientEvent(event);
    }

    @EbslApiOperation("Read registered event metadata.")
    public List<EventRegistry.Entry> registeredEvents() {
        return EbslMod.registeredEvents();
    }
}
