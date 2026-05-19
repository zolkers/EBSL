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

package fr.riege.ebsl.tools.pathfindersim.core;

import fr.riege.ebsl.tools.pathfindersim.replay.SimMetrics;

final class StuckTracker {
    private final int windowTicks;
    private final double epsilon;

    private double bestDistance = Double.POSITIVE_INFINITY;
    private int stagnantTicks;
    private int stuckTicks;
    private int stuckEvents;
    private int longestStuckStreak;
    private boolean currentlyStuck;

    StuckTracker(int windowTicks, double epsilon) {
        this.windowTicks = Math.max(1, windowTicks);
        this.epsilon = Math.max(0.0, epsilon);
    }

    boolean update(double distanceToGoal) {
        if (distanceToGoal < bestDistance - epsilon) {
            bestDistance = distanceToGoal;
            stagnantTicks = 0;
            currentlyStuck = false;
            return false;
        }

        stagnantTicks++;
        boolean stuck = stagnantTicks >= windowTicks;
        if (stuck) {
            stuckTicks++;
            longestStuckStreak = Math.max(longestStuckStreak, stagnantTicks - windowTicks + 1);
            if (!currentlyStuck) {
                stuckEvents++;
                currentlyStuck = true;
            }
        }
        return stuck;
    }

    SimMetrics metrics(int ticks, double finalDistance) {
        return new SimMetrics(
            ticks,
            stuckTicks,
            stuckEvents,
            longestStuckStreak,
            bestDistance,
            finalDistance);
    }
}
