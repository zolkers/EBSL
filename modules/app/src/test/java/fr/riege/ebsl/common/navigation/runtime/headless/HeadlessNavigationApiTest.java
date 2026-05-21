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

import fr.riege.ebsl.common.domain.world.BlockId;
import fr.riege.ebsl.common.math.Vec3d;
import fr.riege.ebsl.common.navigation.PathPlannerOptions;
import fr.riege.ebsl.common.navigation.PathPlanningService;
import fr.riege.ebsl.common.navigation.runtime.entity.EntityNavigationService;
import fr.riege.ebsl.common.navigation.runtime.entity.MovementIntent;
import fr.riege.ebsl.common.pathfinding.wrapper.PathPosition;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
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
    void headlessNavigationFactoryCreatesOneIsolatedAgentPerEntity() {
        HeadlessWorldLayer world = HeadlessWorldLayer.flat(63);
        HeadlessActor firstActor = new HeadlessActor(new Vec3d(0.5, 64.0, 0.5));
        HeadlessActor secondActor = new HeadlessActor(new Vec3d(2.5, 64.0, 0.5));

        HeadlessNavigationAgent firstAgent = HeadlessNavigationFactory.create(world, firstActor);
        HeadlessNavigationAgent secondAgent = HeadlessNavigationFactory.create(world, secondActor);

        assertSame(firstActor, firstAgent.actor());
        assertSame(secondActor, secondAgent.actor());
        assertNotSame(firstAgent.planner(), secondAgent.planner());
        assertNotSame(firstAgent.motor(), secondAgent.motor());
        assertNotSame(firstAgent.navigation(), secondAgent.navigation());
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

    @Test
    void headlessMotorAcceleratesTowardRequestedVelocity() {
        HeadlessActor actor = new HeadlessActor(new Vec3d(0.5, 64.0, 0.5));
        HeadlessMotor motor = new HeadlessMotor(actor).world(HeadlessWorldLayer.flat(63));

        motor.apply(MovementIntent.builder()
            .velocity(new Vec3d(0.36, 0.0, 0.0))
            .sprint(true)
            .build());

        assertEquals(0.12, actor.velocity().x(), 1.0e-6);
    }

    @Test
    void headlessMotorUsesAirAccelerationWhenUngrounded() {
        HeadlessActor actor = new HeadlessActor(new Vec3d(0.5, 65.0, 0.5))
            .onGround(false);
        HeadlessMotor motor = new HeadlessMotor(actor).world(HeadlessWorldLayer.flat(63));

        motor.apply(MovementIntent.builder()
            .velocity(new Vec3d(0.36, 0.0, 0.0))
            .build());

        assertEquals(0.05, actor.velocity().x(), 1.0e-6);
    }

    @Test
    void entityNavigationServiceClimbsVerticalLadderWithoutJumpIntent() {
        HeadlessWorldLayer world = HeadlessWorldLayer.flat(63);
        HeadlessBlockState ladder = HeadlessBlockState.climbable(BlockId.of("minecraft:ladder"));
        world.fill(0, 64, 0, 0, 66, 0, ladder);
        HeadlessActor actor = new HeadlessActor(new Vec3d(0.5, 64.0, 0.5));
        HeadlessMotor motor = new HeadlessMotor(actor).world(world);
        EntityNavigationService service = new EntityNavigationService(
            new PathPlanningService(world),
            actor,
            motor);
        service.setPlannerOptions(PathPlannerOptions.defaults().toBuilder().async(false).build());

        service.startBlockGoal(0, 66, 0);
        service.tick();

        assertTrue(service.isNavigating(), "ladder route should be executable");
        assertTrue(motor.lastIntent().velocity().y() > 0.0, "climb should request vertical velocity");
        assertFalse(motor.lastIntent().jump(), "climb should not be driven by repeated jump intent");
    }

    @Test
    void trajectorySimulatorKeepsSourceActorUntouched() {
        HeadlessActor actor = new HeadlessActor(new Vec3d(0.5, 64.0, 0.5));
        List<HeadlessTrajectorySimulator.TrajectoryState> states = HeadlessTrajectorySimulator.simulate(
            actor,
            HeadlessWorldLayer.flat(63),
            List.of(
                MovementIntent.builder().velocity(new Vec3d(0.36, 0.0, 0.0)).build(),
                MovementIntent.builder().velocity(new Vec3d(0.36, 0.0, 0.0)).build()));

        assertEquals(2, states.size());
        assertEquals(0.5, actor.position().x(), 1.0e-6);
        assertTrue(states.getLast().position().x() > states.getFirst().position().x());
    }
}
