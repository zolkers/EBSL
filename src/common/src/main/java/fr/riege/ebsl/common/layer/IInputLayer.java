package fr.riege.ebsl.common.layer;

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
}
