package fr.riege.ebsl.common.platform.layer;

public interface IPhysicsLayer {
    default void lookAt(double x, double y, double z) {
    }

    default void setForward(boolean value) {
    }

    default void setBackward(boolean value) {
    }

    default void setLeft(boolean value) {
    }

    default void setRight(boolean value) {
    }

    default void setJump(boolean value) {
    }

    default void setSprint(boolean value) {
    }

    default void setRotation(float yaw, float pitch) {
    }

    default double rotationGcd() {
        double sensitivity = 0.5;
        double f = sensitivity * 0.6 + 0.2;
        return f * f * f * 1.2;
    }

    void setSneak(boolean value);
    void clearInputs();
}
