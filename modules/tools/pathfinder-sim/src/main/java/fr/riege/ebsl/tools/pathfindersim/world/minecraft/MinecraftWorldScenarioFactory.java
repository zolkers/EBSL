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

package fr.riege.ebsl.tools.pathfindersim.world.minecraft;

import fr.riege.ebsl.common.navigation.PathPlannerOptions;
import fr.riege.ebsl.common.navigation.runtime.entity.EntityFollowerOptions;
import fr.riege.ebsl.common.navigation.runtime.headless.HeadlessWorldLayer;
import fr.riege.ebsl.tools.pathfindersim.scenario.SimulationScenario;

import java.io.IOException;

public final class MinecraftWorldScenarioFactory {
    private MinecraftWorldScenarioFactory() {
    }

    public static SimulationScenario create(MinecraftWorldImportOptions options) throws IOException {
        HeadlessWorldLayer world = new AnvilWorldLoader().load(options);
        PathPlannerOptions plannerOptions = PathPlannerOptions.defaults().toBuilder()
            .async(false)
            .fallback(true)
            .processPath(true)
            .maxIterations(120_000)
            .maxLength(20_000)
            .maxCalculationTimeMs(0)
            .build();
        return new SimulationScenario(
            "minecraft_world",
            "imported Minecraft Anvil world around start and goal",
            world,
            options.start(),
            options.goalX(),
            options.goalY(),
            options.goalZ(),
            plannerOptions,
            EntityFollowerOptions.defaults());
    }
}
