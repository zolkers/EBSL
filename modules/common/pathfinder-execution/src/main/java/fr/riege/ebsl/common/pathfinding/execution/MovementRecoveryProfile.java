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

package fr.riege.ebsl.common.pathfinding.execution;

import fr.riege.ebsl.common.pathfinding.settings.PathfinderSettings;

/**
 * Defines timing and action thresholds for movement recovery.
 *
 * <p>Profiles tune stale-path detection, repair timing, backup behavior, and recovery jumps per movement family.</p>
 */
interface MovementRecoveryProfile {
    /**
     * Returns the hard stale timeout in milliseconds before recovery escalates.
 *
     * @return the value defined by this contract
     */
    long hardStaleMs();

    /**
     * Returns the stale timeout in milliseconds before path repair is requested.
 *
     * @return the value defined by this contract
     */
    long pathRepairStaleMs();

    /**
     * Returns the timeout in milliseconds for grounded no-progress detection.
 *
     * @return the value defined by this contract
     */
    long groundedNoProgressMs();

    /**
     * Returns the timeout in milliseconds before the movement is treated as deadlocked.
 *
     * @return the value defined by this contract
     */
    default long deadlockMs() {
        return PathfinderSettings.instance().stuckTimeMs.value() * 2L;
    }

    /**
     * Returns whether backup movement is allowed during recovery.
 *
     * @return true when the condition is satisfied; false otherwise
     */
    boolean allowBackup();

    /**
     * Returns whether a recovery jump is allowed for this profile.
 *
     * @return true when the condition is satisfied; false otherwise
     */
    boolean allowRecoveryJump();
}
