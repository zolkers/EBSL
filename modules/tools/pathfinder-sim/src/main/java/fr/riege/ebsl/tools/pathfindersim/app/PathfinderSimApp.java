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

package fr.riege.ebsl.tools.pathfindersim.app;

import fr.riege.ebsl.tools.pathfindersim.cli.SimCliOptions;
import fr.riege.ebsl.tools.pathfindersim.core.SimulationSuite;
import fr.riege.ebsl.tools.pathfindersim.replay.SimulationReport;
import fr.riege.ebsl.tools.pathfindersim.replay.SimulationResult;
import fr.riege.ebsl.tools.pathfindersim.scenario.ScenarioCatalog;
import fr.riege.ebsl.tools.pathfindersim.scenario.SimulationScenario;
import fr.riege.ebsl.tools.pathfindersim.ui.SimulationFrame;
import fr.riege.ebsl.tools.pathfindersim.world.minecraft.MinecraftWorldImportOptions;
import fr.riege.ebsl.tools.pathfindersim.world.minecraft.MinecraftWorldScenarioFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class PathfinderSimApp {
    private PathfinderSimApp() {
    }

    public static void main(String[] args) throws IOException {
        SimCliOptions options = SimCliOptions.parse(args);
        List<SimulationScenario> scenarios = new ArrayList<>(ScenarioCatalog.defaultScenarios());
        MinecraftWorldImportOptions importOptions = options.minecraftWorldImportOptions();
        if (importOptions != null) {
            scenarios.add(MinecraftWorldScenarioFactory.create(importOptions));
        }
        SimulationSuite suite = new SimulationSuite(scenarios);
        List<SimulationResult> results = suite.run(options);
        String report = SimulationReport.render(results);
        System.out.println(report);
        if (options.ui()) {
            SimulationFrame.show(results);
        }
        if (options.jsonOutput() != null) {
            Path output = options.jsonOutput();
            Path parent = output.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            Files.writeString(output, SimulationReport.toJson(results));
            System.out.println("json=" + output.toAbsolutePath());
        }
    }
}
