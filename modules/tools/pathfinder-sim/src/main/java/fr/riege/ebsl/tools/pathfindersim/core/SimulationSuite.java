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

import fr.riege.ebsl.tools.pathfindersim.cli.SimCliOptions;
import fr.riege.ebsl.tools.pathfindersim.replay.SimulationResult;
import fr.riege.ebsl.tools.pathfindersim.scenario.SimulationScenario;

import java.util.ArrayList;
import java.util.List;

public final class SimulationSuite {
    private final List<SimulationScenario> scenarios;

    public SimulationSuite(List<SimulationScenario> scenarios) {
        this.scenarios = List.copyOf(scenarios);
    }

    public List<SimulationResult> run(SimCliOptions options) {
        SimCliOptions effectiveOptions = options == null ? SimCliOptions.parse(new String[0]) : options;
        List<SimulationResult> results = new ArrayList<>();
        SimulationRunner runner = new SimulationRunner();
        for (SimulationScenario scenario : scenarios) {
            if (effectiveOptions.accepts(scenario)) {
                results.add(runner.run(scenario, effectiveOptions));
            }
        }
        return List.copyOf(results);
    }
}
