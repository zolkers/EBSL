/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 *
 * Copyright (C) 2026 Victor Riegert and EBSL contributors
 *
 * This file is part of EBSL.
 *
 * EBSL is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * EBSL is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public
 * License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with EBSL. If not, see <https://www.gnu.org/licenses/>.
 */

package fr.riege.ebsl.common.pathfinding.execution;

import fr.riege.ebsl.common.pathfinding.settings.PathfinderSettings;

final class ParkourRecoveryProfile implements MovementRecoveryProfile {
    @Override
    public long hardStaleMs() {
        return PathfinderSettings.instance().parkourPathReplanHardStaleMs.value();
    }

    @Override
    public long pathRepairStaleMs() {
        return PathfinderSettings.instance().parkourPathReplanStaleMs.value();
    }

    @Override
    public long groundedNoProgressMs() {
        return PathfinderSettings.instance().parkourGroundedNoProgressReplanMs.value();
    }

    @Override
    public long deadlockMs() {
        return Math.max(
            PathfinderSettings.instance().stuckTimeMs.value() * 4L,
            PathfinderSettings.instance().parkourPathReplanHardStaleMs.value());
    }

    @Override
    public boolean allowBackup() {
        return false;
    }

    @Override
    public boolean allowRecoveryJump() {
        return false;
    }
}
