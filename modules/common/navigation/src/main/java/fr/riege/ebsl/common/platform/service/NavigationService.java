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
package fr.riege.ebsl.common.platform.service;

import fr.riege.ebsl.common.math.Vec3d;
import fr.riege.ebsl.common.navigation.NavigationStatus;
import fr.riege.ebsl.common.pathfinding.Node;
import fr.riege.ebsl.common.pathfinding.goal.NavigationRequest;

/**
 * Provides the application-facing navigation control surface.
 *
 * <p>Commands, UI panels, and scripts use this service to start goals, stop execution, inspect movement state, and render navigation diagnostics.</p>
 */
public interface NavigationService {
    /**
     * Starts block goal behavior.
 *
     * @param x the block x coordinate
     * @param y the block y coordinate
     * @param z the block z coordinate
     */
    void startBlockGoal(int x, int y, int z);

    /**
     * Starts column goal behavior.
 *
     * @param x the block x coordinate
     * @param z the block z coordinate
     */
    void startColumnGoal(int x, int z);

    /**
     * Starts path test behavior.
 *
     * @param x the block x coordinate
     * @param y the block y coordinate
     * @param z the block z coordinate
     */
    void startPathTest(int x, int y, int z);

    /**
     * Starts path test xz behavior.
 *
     * @param x the block x coordinate
     * @param z the block z coordinate
     */
    void startPathTestXZ(int x, int z);

    /**
     * Stops the active operation and releases active controls when appropriate.
 *
     * @param announce whether the stop should be announced to the user
     */
    void stop(boolean announce);

    /**
     * Starts navigation behavior.
 *
     * @param request the navigation request to start
     */
    default void startNavigation(NavigationRequest request) {
    }

    /**
     * Returns whether navigating is true for the current state.
 *
     * @return true when the condition is satisfied; false otherwise
     */
    boolean isNavigating();

    /**
     * Returns the movement type currently being executed, or {@code null} when no movement is active.
 *
     * @return the value defined by this contract
     */
    Node.MoveType currentMoveType();

    /**
     * Returns whether walk sneak latched is true for the current state.
 *
     * @return true when the condition is satisfied; false otherwise
     */
    boolean isWalkSneakLatched();

    /**
     * Updates whether walking should keep sneak latched during navigation.
 *
     * @param value the value to apply
     */
    void setWalkSneakLatched(boolean value);

    /**
     * Returns the current high-level navigation status.
 *
     * @return the value defined by this contract
     */
    default NavigationStatus pathStatus() {
        return isNavigating() ? NavigationStatus.EXECUTING : NavigationStatus.IDLE;
    }

    /**
     * Returns the number of nodes in the last planned path.
 *
     * @return the value defined by this contract
     */
    default int lastPathNodeCount() {
        return 0;
    }

    /**
     * Advances this component by one runtime tick.
     */
    default void tick() {
    }

    /**
     * Renders this component for the active frame using the supplied runtime context.
     */
    default void renderCameraFrame() {
        renderWorld();
    }

    /**
     * Renders this component for the active frame using the supplied runtime context.
     */
    default void renderWorld() {
    }

    /**
     * Starts greenhouse walk behavior.
 *
     * @param target the target path position
     * @param onFinished the on finished value
     * @param isFirst the is first value
     */
    default void startGreenhouseWalk(Vec3d target, Runnable onFinished, boolean isFirst) {
        startBlockGoal((int) Math.floor(target.x()), (int) Math.floor(target.y()), (int) Math.floor(target.z()));
        if (onFinished != null) {
            onFinished.run();
        }
    }
}
