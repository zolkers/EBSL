package fr.riege.ebsl.event.events.render;

import fr.riege.ebsl.event.Event;
import fr.riege.ebsl.ui.layout.UiRect;
import imgui.ImDrawList;

public final class RenderGameViewportEvent extends Event {
    private final ImDrawList drawList;
    private final UiRect viewport;

    public RenderGameViewportEvent(ImDrawList drawList, UiRect viewport) {
        this.drawList = drawList;
        this.viewport = viewport;
    }

    public ImDrawList getDrawList() {
        return drawList;
    }

    public UiRect getViewport() {
        return viewport;
    }
}
