package fr.riege.ebsl.common.platform.layer;

/**
 * Defines the contract for {@code IPhysicsLayer} implementations.
 */
public interface IPhysicsLayer {
    default void setRotation(float yaw, float pitch) {
    }

    default double rotationGcd() {
        double sensitivity = 0.5;
        double f = sensitivity * 0.6 + 0.2;
        return f * f * f * 1.2;
    }
}
