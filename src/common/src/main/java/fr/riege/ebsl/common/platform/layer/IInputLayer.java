package fr.riege.ebsl.common.platform.layer;

public interface IInputLayer {
    default void registerUnfocusKeybind(Runnable action) {
    }

    default void releaseMouse() {
    }

    default boolean isMouseGrabbed() {
        return true;
    }

    default void releaseGameplayKeys() {
    }

    default boolean forwardDown() { return false; }
    default boolean backwardDown() { return false; }
    default boolean leftDown() { return false; }
    default boolean rightDown() { return false; }
    default boolean jumpDown() { return false; }
    default boolean sneakDown() { return false; }
}
