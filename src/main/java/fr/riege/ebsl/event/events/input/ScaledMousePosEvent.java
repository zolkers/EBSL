package fr.riege.ebsl.event.events.input;

import com.mojang.blaze3d.platform.Window;
import fr.riege.ebsl.event.Event;

public final class ScaledMousePosEvent extends Event {
    public enum Axis { X, Y }

    private final Window window;
    private final double rawPos;
    private double scaledPos;
    private final Axis axis;

    public ScaledMousePosEvent(Window window, double rawPos, double scaledPos, Axis axis) {
        this.window = window;
        this.rawPos = rawPos;
        this.scaledPos = scaledPos;
        this.axis = axis;
    }

    public Window getWindow() { return window; }
    public double getRawPos() { return rawPos; }
    public double getScaledPos() { return scaledPos; }
    public void setScaledPos(double scaledPos) { this.scaledPos = scaledPos; }
    public Axis getAxis() { return axis; }
}
