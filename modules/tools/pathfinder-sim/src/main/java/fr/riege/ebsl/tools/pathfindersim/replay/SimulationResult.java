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

package fr.riege.ebsl.tools.pathfindersim.replay;

import fr.riege.ebsl.common.navigation.NavigationStatus;

import java.util.List;

public record SimulationResult(
    String scenarioId,
    String description,
    NavigationStatus status,
    boolean reached,
    int ticks,
    long elapsedNanos,
    int navigationNodes,
    int rawNodes,
    boolean completePlan,
    SimMetrics metrics,
    List<ReplayBlock> terrain,
    List<SimulationTick> ticksTrace
) {
    public SimulationResult {
        terrain = terrain == null ? List.of() : List.copyOf(terrain);
        ticksTrace = ticksTrace == null ? List.of() : List.copyOf(ticksTrace);
    }

    public SimulationResult withScenarioId(String newScenarioId) {
        return new SimulationResult(
            newScenarioId,
            description,
            status,
            reached,
            ticks,
            elapsedNanos,
            navigationNodes,
            rawNodes,
            completePlan,
            metrics,
            terrain,
            ticksTrace);
    }
}
