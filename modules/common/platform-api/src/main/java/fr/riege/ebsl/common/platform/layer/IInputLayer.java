/*
 * This file is part of EBSL.
 *
 * EBSL is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * EBSL is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package fr.riege.ebsl.common.platform.layer;

/**
 * Controls and observes gameplay input state.
 *
 * <p>Navigation, scripting, and UI code use this layer to release focus, press movement keys, and query current key state safely.</p>
 */
public interface IInputLayer {
    /**
     * Registers an action invoked when the platform-specific unfocus key binding is triggered.
 *
     * @param action the callback to run when the binding is triggered
     */
    default void registerUnfocusKeybind(Runnable action) {
    }

    /**
     * Releases exclusive mouse capture when the platform currently owns it.
     */
    default void releaseMouse() {
    }

    /**
     * Returns whether the game currently has exclusive mouse capture.
 *
     * @return true when the condition is satisfied; false otherwise
     */
    default boolean isMouseGrabbed() {
        return true;
    }

    /**
     * Releases all gameplay inputs that may remain held by automation code.
     */
    default void releaseGameplayKeys() {
    }

    /**
     * Releases movement-related inputs while leaving non-movement actions untouched.
     */
    default void releaseMovementKeys() {
        setForwardDown(false);
        setBackwardDown(false);
        setLeftDown(false);
        setRightDown(false);
        setJumpDown(false);
        setSneakDown(false);
        setSprintDown(false);
    }

    /**
     * Returns whether the forward movement key is currently held.
 *
     * @return true when the condition is satisfied; false otherwise
     */
    default boolean forwardDown() { return false; }
    /**
     * Returns whether the backward movement key is currently held.
 *
     * @return true when the condition is satisfied; false otherwise
     */
    default boolean backwardDown() { return false; }
    /**
     * Returns whether the left movement key is currently held.
 *
     * @return true when the condition is satisfied; false otherwise
     */
    default boolean leftDown() { return false; }
    /**
     * Returns whether the right movement key is currently held.
 *
     * @return true when the condition is satisfied; false otherwise
     */
    default boolean rightDown() { return false; }
    /**
     * Returns whether the jump key is currently held.
 *
     * @return true when the condition is satisfied; false otherwise
     */
    default boolean jumpDown() { return false; }
    /**
     * Returns whether the sneak key is currently held.
 *
     * @return true when the condition is satisfied; false otherwise
     */
    default boolean sneakDown() { return false; }

    /**
     * Updates the held state of the forward movement key.
 *
     * @param down whether the input should be held down
     */
    default void setForwardDown(boolean down) {}
    /**
     * Updates the held state of the backward movement key.
 *
     * @param down whether the input should be held down
     */
    default void setBackwardDown(boolean down) {}
    /**
     * Updates the held state of the left movement key.
 *
     * @param down whether the input should be held down
     */
    default void setLeftDown(boolean down) {}
    /**
     * Updates the held state of the right movement key.
 *
     * @param down whether the input should be held down
     */
    default void setRightDown(boolean down) {}
    /**
     * Updates the held state of the jump key.
 *
     * @param down whether the input should be held down
     */
    default void setJumpDown(boolean down) {}
    /**
     * Updates the held state of the sneak key.
 *
     * @param down whether the input should be held down
     */
    default void setSneakDown(boolean down) {}
    /**
     * Updates the held state of the sprint key.
 *
     * @param down whether the input should be held down
     */
    default void setSprintDown(boolean down) {}
    /**
     * Updates the held state of the attack key.
 *
     * @param down whether the input should be held down
     */
    default void setAttackDown(boolean down) {}
    /**
     * Updates the held state of the use key.
 *
     * @param down whether the input should be held down
     */
    default void setUseDown(boolean down) {}
    /**
     * Attempts to attack the currently targeted block through the platform input layer.
 *
     * @return true when the condition is satisfied; false otherwise
     */
    default boolean attackTargetedBlock() { return false; }
}
