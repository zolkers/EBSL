package fr.riege.ebsl.event.events.render;

import com.mojang.blaze3d.pipeline.RenderTarget;
import fr.riege.ebsl.event.Event;

public final class BlitToScreenEvent extends Event {
    private final RenderTarget target;

    public BlitToScreenEvent(RenderTarget target) {
        this.target = target;
    }

    public RenderTarget getTarget() { return target; }
}
