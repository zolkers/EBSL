package fr.riege.ebsl.common.core.event;

public final class ScaledMousePosEvent extends Event {
    public enum Axis { X, Y }

    private final double rawPos;
    private double scaledPos;
    private final Axis axis;

    public ScaledMousePosEvent(double rawPos, double scaledPos, Axis axis) {
        this.rawPos = rawPos;
        this.scaledPos = scaledPos;
        this.axis = axis;
    }

    public double rawPos() { return rawPos; }
    public double scaledPos() { return scaledPos; }
    public void setScaledPos(double scaledPos) { this.scaledPos = scaledPos; }
    public Axis axis() { return axis; }
}
