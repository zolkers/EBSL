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

import fr.riege.ebsl.common.math.Vec3d;
import fr.riege.ebsl.common.navigation.NavigationStatus;
import fr.riege.ebsl.common.navigation.PathPlan;
import fr.riege.ebsl.common.navigation.runtime.entity.EntityNavigationService;
import fr.riege.ebsl.common.navigation.runtime.headless.HeadlessNavigationAgent;
import fr.riege.ebsl.common.navigation.runtime.headless.HeadlessBlockState;
import fr.riege.ebsl.common.navigation.runtime.headless.HeadlessActor;
import fr.riege.ebsl.common.navigation.runtime.headless.HeadlessNavigationFactory;
import fr.riege.ebsl.tools.pathfindersim.cli.SimCliOptions;
import fr.riege.ebsl.tools.pathfindersim.replay.ReplayBlock;
import fr.riege.ebsl.tools.pathfindersim.replay.ReplayBlockKind;
import fr.riege.ebsl.tools.pathfindersim.replay.SimMetrics;
import fr.riege.ebsl.tools.pathfindersim.replay.SimulationResult;
import fr.riege.ebsl.tools.pathfindersim.replay.SimulationTick;
import fr.riege.ebsl.tools.pathfindersim.scenario.SimulationScenario;

import java.util.ArrayList;
import java.util.List;

public final class SimulationRunner {
    public SimulationResult run(SimulationScenario scenario, SimCliOptions options) {
        long startNanos = System.nanoTime();
        HeadlessActor actor = new HeadlessActor(scenario.start());
        HeadlessNavigationAgent agent = HeadlessNavigationFactory.create(
            scenario.world(),
            actor,
            scenario.followerOptions(),
            scenario.plannerOptions());
        EntityNavigationService service = agent.navigation();
        service.startBlockGoal(scenario.goalX(), scenario.goalY(), scenario.goalZ());
        PathPlan plan = service.lastPlan();
        List<SimulationTick> samples = new ArrayList<>();
        StuckTracker stuckTracker = new StuckTracker(options.stuckWindowTicks(), options.stuckEpsilon());
        int ticks = 0;

        while (ticks < options.maxTicks() && isActive(service.pathStatus())) {
            service.tick();
            actor.tick(scenario.world());
            ticks++;
            double distance = distanceToGoal(actor.position(), scenario);
            boolean stuck = stuckTracker.update(distance);
            samples.add(SimulationTick.capture(ticks, actor, agent.motor(), service, distance, stuck));
        }

        NavigationStatus status = service.pathStatus();
        boolean reached = status == NavigationStatus.FOUND && distanceToGoal(actor.position(), scenario) <= 1.25;
        SimMetrics metrics = stuckTracker.metrics(ticks, distanceToGoal(actor.position(), scenario));
        long elapsedNanos = System.nanoTime() - startNanos;
        return new SimulationResult(
            scenario.id(),
            scenario.description(),
            status,
            reached,
            ticks,
            elapsedNanos,
            plan == null ? 0 : plan.navigationNodes().size(),
            plan == null ? 0 : plan.rawNodes().size(),
            plan != null && plan.complete(),
            metrics,
            terrainSnapshot(scenario, samples),
            samples);
    }

    private static boolean isActive(NavigationStatus status) {
        return status == NavigationStatus.CALCULATING || status == NavigationStatus.EXECUTING;
    }

    private static double distanceToGoal(Vec3d position, SimulationScenario scenario) {
        double dx = scenario.goalX() + 0.5 - position.x();
        double dy = scenario.goalY() - position.y();
        double dz = scenario.goalZ() + 0.5 - position.z();
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    private static List<ReplayBlock> terrainSnapshot(SimulationScenario scenario, List<SimulationTick> samples) {
        if (samples.isEmpty()) {
            return List.of();
        }
        int minX = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int minZ = Integer.MAX_VALUE;
        int maxZ = Integer.MIN_VALUE;
        int minY = Integer.MAX_VALUE;
        int maxY = Integer.MIN_VALUE;
        for (SimulationTick sample : samples) {
            minX = Math.min(minX, (int) Math.floor(sample.position().x()));
            maxX = Math.max(maxX, (int) Math.floor(sample.position().x()));
            minZ = Math.min(minZ, (int) Math.floor(sample.position().z()));
            maxZ = Math.max(maxZ, (int) Math.floor(sample.position().z()));
            minY = Math.min(minY, (int) Math.floor(sample.position().y()) - 3);
            maxY = Math.max(maxY, (int) Math.floor(sample.position().y()) + 3);
        }
        minX = Math.min(minX, Math.min((int) Math.floor(scenario.start().x()), scenario.goalX())) - 6;
        maxX = Math.max(maxX, Math.max((int) Math.floor(scenario.start().x()), scenario.goalX())) + 6;
        minZ = Math.min(minZ, Math.min((int) Math.floor(scenario.start().z()), scenario.goalZ())) - 6;
        maxZ = Math.max(maxZ, Math.max((int) Math.floor(scenario.start().z()), scenario.goalZ())) + 6;
        minY = Math.min(minY, Math.min((int) Math.floor(scenario.start().y()), scenario.goalY()) - 4);
        maxY = Math.max(maxY, Math.max((int) Math.floor(scenario.start().y()), scenario.goalY()) + 4);
        if ((maxX - minX) * (maxZ - minZ) > 16_384) {
            return List.of();
        }
        List<ReplayBlock> blocks = new ArrayList<>();
        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                for (int y = minY; y <= maxY; y++) {
                    HeadlessBlockState state = scenario.world().stateAt(x, y, z);
                    if (!state.isAir()) {
                        blocks.add(new ReplayBlock(x, y, z, ReplayBlockKind.classify(state)));
                    }
                }
            }
        }
        return List.copyOf(blocks);
    }
}
