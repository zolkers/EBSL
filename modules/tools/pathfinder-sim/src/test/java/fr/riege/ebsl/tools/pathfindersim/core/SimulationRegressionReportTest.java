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

import fr.riege.ebsl.common.navigation.NavigationStatus;
import fr.riege.ebsl.tools.pathfindersim.cli.SimCliOptions;
import fr.riege.ebsl.tools.pathfindersim.replay.SimMetrics;
import fr.riege.ebsl.tools.pathfindersim.replay.SimulationResult;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SimulationRegressionReportTest {
    @Test
    void passesStableReachedSimulation() {
        SimulationRegressionReport report = SimulationRegressionReport.evaluate(
            List.of(result(true, 0, 0.25)),
            SimCliOptions.parse(new String[] { "--fail-on-regression" }));

        assertTrue(report.passed());
    }

    @Test
    void failsUnreachedOrStuckSimulation() {
        SimulationRegressionReport report = SimulationRegressionReport.evaluate(
            List.of(result(false, 8, 4.0)),
            SimCliOptions.parse(new String[] {
                "--fail-on-regression",
                "--regression-max-stuck-events=2",
                "--regression-max-final-distance=1.0"
            }));

        assertFalse(report.passed());
        assertTrue(report.render().contains("goal was not reached"));
        assertTrue(report.render().contains("stuck events"));
    }

    private static SimulationResult result(boolean reached, int stuckEvents, double finalDistance) {
        return new SimulationResult(
            "case",
            "test case",
            reached ? NavigationStatus.FOUND : NavigationStatus.FAILED,
            reached,
            20,
            1_000_000L,
            10,
            12,
            true,
            new SimMetrics(20, stuckEvents, stuckEvents, stuckEvents, 0, 0, 0.0, 0.0, 0.0, 0.0,
                finalDistance, finalDistance),
            List.of(),
            List.of());
    }
}
