package fr.riege.ebsl.event.events.render;

import fr.riege.ebsl.event.Event;
import org.joml.Matrix4f;

public final class RenderWorldEvent extends Event {
    private final Matrix4f projection;
    private final double camX;
    private final double camY;
    private final double camZ;

    public RenderWorldEvent(Matrix4f projection, double camX, double camY, double camZ) {
        this.projection = projection;
        this.camX = camX;
        this.camY = camY;
        this.camZ = camZ;
    }

    public Matrix4f getProjection() {
        return projection;
    }

    public double getCamX() {
        return camX;
    }

    public double getCamY() {
        return camY;
    }

    public double getCamZ() {
        return camZ;
    }
}
