package fr.riege.ebsl.event.events.input;

import fr.riege.ebsl.event.Event;

public final class GrabMouseEvent extends Event {
    private final double xpos;
    private final double ypos;

    public GrabMouseEvent(double xpos, double ypos) {
        this.xpos = xpos;
        this.ypos = ypos;
    }

    public double getXpos() { return xpos; }
    public double getYpos() { return ypos; }
}
