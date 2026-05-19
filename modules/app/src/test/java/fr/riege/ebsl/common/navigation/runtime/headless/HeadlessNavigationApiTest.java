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

package fr.riege.ebsl.common.navigation.runtime.headless;

import fr.riege.ebsl.common.math.Vec3d;
import fr.riege.ebsl.common.navigation.PathPlannerOptions;
import fr.riege.ebsl.common.navigation.PathPlanningService;
import fr.riege.ebsl.common.navigation.runtime.entity.EntityNavigationService;
import fr.riege.ebsl.common.pathfinding.wrapper.PathPosition;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HeadlessNavigationApiTest {
    @Test
    void plannerBuildsUsablePathFromHeadlessWorld() {
        HeadlessWorldLayer world = HeadlessWorldLayer.flat(63);
        PathPlanningService planner = new PathPlanningService(world);

        var plan = planner.plan(
            planner.positionFromEntity(0.5, 64.0, 0.5),
            planner.resolveTarget(new PathPosition(4, 64, 0)),
            PathPlannerOptions.defaults().toBuilder().async(false).build()
        ).toCompletableFuture().join();

        assertTrue(plan.usable(), "headless planner should expose a usable path");
        assertFalse(plan.navigationNodes().isEmpty(), "processed navigation nodes should be populated");
    }

    @Test
    void entityNavigationServiceEmitsMovementIntentForServerAdapters() {
        HeadlessWorldLayer world = HeadlessWorldLayer.flat(63);
        HeadlessActor actor = new HeadlessActor(new Vec3d(0.5, 64.0, 0.5));
        HeadlessMotor motor = new HeadlessMotor(actor);
        EntityNavigationService service = new EntityNavigationService(
            new PathPlanningService(world),
            actor,
            motor);
        service.setPlannerOptions(PathPlannerOptions.defaults().toBuilder().async(false).build());

        service.startBlockGoal(4, 64, 0);
        service.tick();

        assertTrue(service.isNavigating(), "service should be executing after a usable path is found");
        assertTrue(motor.lastIntent().velocity().x() > 0.0, "server motor should receive a positive X velocity");
    }

    @Test
    void headlessActorStepsOntoOneBlockLedgeWhenBodySpaceIsClear() {
        HeadlessWorldLayer world = HeadlessWorldLayer.flat(63)
            .set(1, 64, 0, HeadlessBlockState.STONE);
        HeadlessActor actor = new HeadlessActor(new Vec3d(0.8, 64.0, 0.5))
            .velocity(new Vec3d(0.35, 0.0, 0.0));

        actor.tick(world);

        assertEquals(65.0, actor.position().y(), 1.0e-6);
        assertTrue(actor.onGround(), "stepping onto a ledge should keep the actor grounded");
        assertTrue(actor.velocity().x() > 0.0, "horizontal intent should survive the step-up");
    }
}
