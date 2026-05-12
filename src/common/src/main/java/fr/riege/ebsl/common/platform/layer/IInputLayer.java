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

    default void releaseMovementKeys() {
        setForwardDown(false);
        setBackwardDown(false);
        setLeftDown(false);
        setRightDown(false);
        setJumpDown(false);
        setSneakDown(false);
        setSprintDown(false);
    }

    default boolean forwardDown() { return false; }
    default boolean backwardDown() { return false; }
    default boolean leftDown() { return false; }
    default boolean rightDown() { return false; }
    default boolean jumpDown() { return false; }
    default boolean sneakDown() { return false; }

    default void setForwardDown(boolean down) {}
    default void setBackwardDown(boolean down) {}
    default void setLeftDown(boolean down) {}
    default void setRightDown(boolean down) {}
    default void setJumpDown(boolean down) {}
    default void setSneakDown(boolean down) {}
    default void setSprintDown(boolean down) {}
    default void setAttackDown(boolean down) {}
    default void setUseDown(boolean down) {}
    default boolean attackTargetedBlock() { return false; }
}
