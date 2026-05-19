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

package fr.riege.ebsl.tools.pathfindersim;

import fr.riege.ebsl.common.math.Vec3d;
import fr.riege.ebsl.common.navigation.NavigationStatus;
import fr.riege.ebsl.common.navigation.PathPlan;
import fr.riege.ebsl.common.navigation.PathPlanningService;
import fr.riege.ebsl.common.navigation.runtime.entity.EntityNavigationService;
import fr.riege.ebsl.common.navigation.runtime.headless.HeadlessActor;
import fr.riege.ebsl.common.navigation.runtime.headless.HeadlessMotor;

import java.util.ArrayList;
import java.util.List;

public final class SimulationRunner {
    public SimulationResult run(SimulationScenario scenario, SimCliOptions options) {
        long startNanos = System.nanoTime();
        HeadlessActor actor = new HeadlessActor(scenario.start());
        HeadlessMotor motor = new HeadlessMotor(actor);
        PathPlanningService planner = new PathPlanningService(scenario.world());
        EntityNavigationService service = new EntityNavigationService(
            planner,
            actor,
            motor,
            scenario.followerOptions(),
            Runnable::run);
        service.setPlannerOptions(scenario.plannerOptions());
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
            samples.add(SimulationTick.capture(ticks, actor, motor, service, distance, stuck));
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
}
