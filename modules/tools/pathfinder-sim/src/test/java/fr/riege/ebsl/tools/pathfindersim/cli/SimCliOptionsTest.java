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

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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

    @Test
    void parsesReplayAndModeOptions() {
        SimCliOptions options = SimCliOptions.parse(new String[] {
            "--scenario=Parkour",
            "--max-ticks=42",
            "--stuck-window=9",
            "--stuck-epsilon=0.05",
            "--json=out/replay.json",
            "--replay-dir=out/replays",
            "--no-replay-save",
            "--headless",
            "--ui"
        });

        assertEquals("parkour", options.scenarioFilter());
        assertEquals(42, options.maxTicks());
        assertEquals(9, options.stuckWindowTicks());
        assertEquals(0.05, options.stuckEpsilon());
        assertEquals(Path.of("out/replay.json"), options.jsonOutput());
        assertEquals(Path.of("out/replays"), options.replayDirectory());
        assertFalse(options.replaySaveEnabled());
        assertFalse(options.headless());
    }

    @Test
    void parsesMinecraftWorldOptions() {
        SimCliOptions options = SimCliOptions.parse(new String[] {
            "--mc-world=run/saves/demo",
            "--mc-start=1.5,65.0,2.5",
            "--mc-goal=10,66,12",
            "--mc-radius=3",
            "--mc-goal-search=48",
            "--mc-stress-grid=4,2,5,3",
            "--mc-diagnostics"
        });

        assertEquals(Path.of("run/saves/demo"), options.minecraftWorldImportOptions().worldDirectory());
        assertEquals(1.5, options.minecraftWorldImportOptions().start().x());
        assertEquals(66, options.minecraftWorldImportOptions().goalY());
        assertEquals(3, options.minecraftWorldImportOptions().radiusChunks());
        assertEquals(48, options.minecraftWorldImportOptions().goalSearchBlocks());
        assertEquals(4, options.minecraftStressGrid().radiusX());
        assertEquals(2, options.minecraftStressGrid().radiusY());
        assertEquals(5, options.minecraftStressGrid().radiusZ());
        assertEquals(3, options.minecraftStressGrid().step());
        assertTrue(options.minecraftWorldImportOptions().diagnostics());
    }
}
