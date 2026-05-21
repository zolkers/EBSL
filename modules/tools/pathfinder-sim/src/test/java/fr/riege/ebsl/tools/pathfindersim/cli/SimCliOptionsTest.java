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

package fr.riege.ebsl.tools.pathfindersim.cli;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SimCliOptionsTest {
    @Test
    void parsesRegressionOptions() {
        SimCliOptions options = SimCliOptions.parse(new String[] {
            "--repeat=12",
            "--fail-on-regression",
            "--regression-max-final-distance=0.75",
            "--regression-max-stuck-events=2",
            "--regression-max-recovery-attempts=3",
            "--regression-max-backward-ticks=24",
            "--regression-max-average-lateral-error=0.12"
        });

        assertEquals(12, options.repeatRuns());
        assertTrue(options.failOnRegression());
        assertEquals(0.75, options.regressionMaxFinalDistance());
        assertEquals(2, options.regressionMaxStuckEvents());
        assertEquals(3, options.regressionMaxRecoveryAttempts());
        assertEquals(24, options.regressionMaxBackwardTicks());
        assertEquals(0.12, options.regressionMaxAverageLateralError());
    }

    @Test
    void clampsRegressionCountsToValidValues() {
        SimCliOptions options = SimCliOptions.parse(new String[] {
            "--repeat=0",
            "--regression-max-stuck-events=-7"
        });

        assertEquals(1, options.repeatRuns());
        assertEquals(0, options.regressionMaxStuckEvents());
    }

    @Test
    void parsesMinecraftRouteMatrix() {
        SimCliOptions options = SimCliOptions.parse(new String[] {
            "--mc-route=crash_386|386,61,42|500,61,40",
            "--mc-route=nearby high@384,63,43@500,62,40"
        });

        assertEquals(2, options.minecraftRoutes().size());
        assertEquals("crash_386", options.minecraftRoutes().getFirst().id());
        assertEquals(386.0, options.minecraftRoutes().getFirst().start().x());
        assertEquals(61, options.minecraftRoutes().getFirst().goalY());
        assertEquals("nearby_high", options.minecraftRoutes().get(1).id());
    }
}
