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

package fr.riege.ebsl.common.navigation.runtime.entity;

import fr.riege.ebsl.common.math.Vec3d;
import fr.riege.ebsl.common.navigation.PathPlan;
import fr.riege.ebsl.common.pathfinding.Node;
import fr.riege.ebsl.common.pathfinding.wrapper.PathPosition;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EntityPathFollowerTest {
    @Test
    void looksAheadAcrossWalkSegmentsForSmootherCorners() {
        TestActor actor = new TestActor(new Vec3d(3.8, 64.0, 0.5));
        CapturingMotor motor = new CapturingMotor();
        EntityPathFollower follower = new EntityPathFollower(actor, motor);

        follower.start(plan(
            node(0, 64, 0, Node.MoveType.WALK),
            node(4, 64, 0, Node.MoveType.WALK),
            node(4, 64, 4, Node.MoveType.WALK)), null, null);
        follower.tick();

        assertTrue(motor.intent.velocity().z() > 0.05);
    }

    @Test
    void keepsParkourTakeoffPrecise() {
        TestActor actor = new TestActor(new Vec3d(3.8, 64.0, 0.5));
        CapturingMotor motor = new CapturingMotor();
        EntityPathFollower follower = new EntityPathFollower(actor, motor);

        follower.start(plan(
            node(0, 64, 0, Node.MoveType.WALK),
            node(4, 64, 0, Node.MoveType.PARKOUR),
            node(4, 64, 4, Node.MoveType.WALK)), null, null);
        follower.tick();

        assertEquals(0.0, motor.intent.velocity().z(), 0.0001);
        assertTrue(motor.intent.jump());
    }

    @Test
    void slowsDownBeforeSharpCorners() {
        TestActor actor = new TestActor(new Vec3d(3.8, 64.0, 0.5));
        CapturingMotor motor = new CapturingMotor();
        EntityPathFollower follower = new EntityPathFollower(actor, motor);

        follower.start(plan(
            node(0, 64, 0, Node.MoveType.WALK),
            node(4, 64, 0, Node.MoveType.WALK),
            node(4, 64, 4, Node.MoveType.WALK)), null, null);
        follower.tick();

        double horizontalSpeed = Math.sqrt(
            motor.intent.velocity().x() * motor.intent.velocity().x()
                + motor.intent.velocity().z() * motor.intent.velocity().z());
        assertTrue(horizontalSpeed < EntityFollowerOptions.defaults().sprintSpeed());
    }

    @Test
    void advancesPastWalkWaypointInsteadOfSteeringBackwards() {
        TestActor actor = new TestActor(new Vec3d(4.22, 64.0, 0.74));
        CapturingMotor motor = new CapturingMotor();
        EntityPathFollower follower = new EntityPathFollower(actor, motor);

        follower.start(plan(
            node(0, 64, 0, Node.MoveType.WALK),
            node(4, 64, 0, Node.MoveType.WALK),
            node(8, 64, 0, Node.MoveType.WALK)), null, null);
        follower.tick();

        assertEquals(2, follower.waypointIndex());
        assertTrue(motor.intent.velocity().x() > 0.0);
    }

    private static PathPlan plan(Node... nodes) {
        List<Node> path = List.of(nodes);
        return new PathPlan(null, null, List.of(), path, path, 0.0, null);
    }

    private static Node node(int x, int y, int z, Node.MoveType moveType) {
        Node node = new Node(new PathPosition(x, y, z));
        node.setMoveType(moveType);
        return node;
    }

    private static final class TestActor implements NavigationActor {
        private final Vec3d position;

        private TestActor(Vec3d position) {
            this.position = position;
        }

        @Override public Vec3d position() {
            return position;
        }
    }

    private static final class CapturingMotor implements NavigationMotor {
        private MovementIntent intent = MovementIntent.stop();

        @Override public void apply(MovementIntent intent) {
            this.intent = intent;
        }
    }
}
