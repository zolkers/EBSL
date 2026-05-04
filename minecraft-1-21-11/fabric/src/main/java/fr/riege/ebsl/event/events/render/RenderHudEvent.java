package fr.riege.ebsl.event.events.render;

import fr.riege.ebsl.event.Event;
import net.minecraft.client.gui.GuiGraphics;

public final class RenderHudEvent extends Event {
    private final GuiGraphics graphics;
    private final float tickDelta;

    public RenderHudEvent(GuiGraphics graphics, float tickDelta) {
        this.graphics = graphics;
        this.tickDelta = tickDelta;
    }

    public GuiGraphics getGraphics() {
        return graphics;
    }

    public float getTickDelta() {
        return tickDelta;
    }
}
