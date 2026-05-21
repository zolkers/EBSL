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
            "--regression-max-stuck-events=2"
        });

        assertEquals(12, options.repeatRuns());
        assertTrue(options.failOnRegression());
        assertEquals(0.75, options.regressionMaxFinalDistance());
        assertEquals(2, options.regressionMaxStuckEvents());
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
}
