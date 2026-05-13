package fr.riege.ebsl.common.platform.layer;

/**
 * Applies low-level player physics adjustments exposed by the platform.
 *
 * <p>Shared code uses this layer for rotation control and mouse-step quantization without depending on Minecraft internals.</p>
 */
public interface IPhysicsLayer {
    /**
     * Applies the supplied yaw and pitch to the controlled player or camera.
 *
     * @param yaw the yaw value
     * @param pitch the pitch value
     */
    default void setRotation(float yaw, float pitch) {
    }

    /**
     * Returns the platform rotation quantum used to mimic valid mouse movement increments.
 *
     * @return the value defined by this contract
     */
    default double rotationGcd() {
        double sensitivity = 0.5;
        double f = sensitivity * 0.6 + 0.2;
        return f * f * f * 1.2;
    }
}
