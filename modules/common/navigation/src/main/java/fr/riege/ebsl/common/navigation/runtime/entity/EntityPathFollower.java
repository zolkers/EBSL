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
import fr.riege.ebsl.common.navigation.NavigationStatus;
import fr.riege.ebsl.common.navigation.PathPlan;
import fr.riege.ebsl.common.pathfinding.Node;

import java.util.List;
import java.util.Objects;

public final class EntityPathFollower {
    private static final double PARKOUR_JUMP_TRIGGER_DISTANCE = 4.35;
    private static final double PARKOUR_TAKEOFF_PREP_DISTANCE = 0.85;

    private final NavigationActor actor;
    private final NavigationMotor motor;
    private final EntityFollowerOptions options;

    private List<Node> path = List.of();
    private int waypointIndex;
    private NavigationStatus status = NavigationStatus.IDLE;
    private Node.MoveType currentMoveType = Node.MoveType.WALK;
    private Runnable onFinished;
    private Runnable onFailed;

    public EntityPathFollower(NavigationActor actor, NavigationMotor motor) {
        this(actor, motor, EntityFollowerOptions.defaults());
    }

    public EntityPathFollower(NavigationActor actor, NavigationMotor motor, EntityFollowerOptions options) {
        this.actor = Objects.requireNonNull(actor, "actor");
        this.motor = Objects.requireNonNull(motor, "motor");
        this.options = options == null ? EntityFollowerOptions.defaults() : options;
    }

    public void start(PathPlan plan, Runnable onFinished, Runnable onFailed) {
        this.onFinished = onFinished;
        this.onFailed = onFailed;
        this.path = plan == null ? List.of() : plan.navigationNodes();
        this.waypointIndex = path.size() > 1 ? 1 : 0;
        this.currentMoveType = Node.MoveType.WALK;
        if (path.isEmpty()) {
            fail();
            return;
        }
        this.status = NavigationStatus.EXECUTING;
    }

    public void stop() {
        path = List.of();
        waypointIndex = 0;
        status = NavigationStatus.IDLE;
        onFinished = null;
        onFailed = null;
        motor.stop();
    }

    public void tick() {
        if (status != NavigationStatus.EXECUTING) {
            return;
        }
        if (!actor.isAlive()) {
            fail();
            return;
        }
        if (path.isEmpty() || waypointIndex >= path.size()) {
            finish();
            return;
        }

        Vec3d position = actor.position();
        advanceReachedWaypoints(position);
        if (waypointIndex >= path.size()) {
            finish();
            return;
        }

        Node waypoint = path.get(waypointIndex);
        currentMoveType = waypoint.moveType() == null ? Node.MoveType.WALK : waypoint.moveType();
        Vec3d target = targetCenter(waypoint);
        double dx = target.x() - position.x();
        double dy = target.y() - position.y();
        double dz = target.z() - position.z();
        double horizontal = Math.sqrt(dx * dx + dz * dz);
        double speed = speedFor(currentMoveType);
        Vec3d velocity = horizontal <= 1.0e-6
            ? new Vec3d(0.0, verticalVelocity(dy, horizontal, currentMoveType), 0.0)
            : new Vec3d(dx / horizontal * speed, verticalVelocity(dy, horizontal, currentMoveType), dz / horizontal * speed);

        motor.apply(MovementIntent.builder()
            .velocity(velocity)
            .lookTarget(options.lookAtWaypoint() ? target : null)
            .jump(shouldJump(dy, horizontal, currentMoveType))
            .sprint(options.sprint() && currentMoveType != Node.MoveType.SWIM && currentMoveType != Node.MoveType.CLIMB)
            .sneak(false)
            .moveType(currentMoveType)
            .build());
    }

    public NavigationStatus status() {
        return status;
    }

    public Node.MoveType currentMoveType() {
        return currentMoveType;
    }

    public int waypointIndex() {
        return waypointIndex;
    }

    public List<Node> pathSnapshot() {
        return List.copyOf(path);
    }

    private void advanceReachedWaypoints(Vec3d position) {
        while (waypointIndex < path.size() && reached(position, path.get(waypointIndex), waypointIndex == path.size() - 1)) {
            waypointIndex++;
        }
    }

    private boolean reached(Vec3d position, Node waypoint, boolean finalWaypoint) {
        Vec3d target = targetCenter(waypoint);
        double dx = target.x() - position.x();
        double dy = target.y() - position.y();
        double dz = target.z() - position.z();
        double horizontal = Math.sqrt(dx * dx + dz * dz);
        double reach = finalWaypoint ? options.finalReachDistance() : options.waypointReachDistance();
        return horizontal <= reach && Math.abs(dy) <= options.verticalReachDistance();
    }

    private double speedFor(Node.MoveType moveType) {
        if (moveType == Node.MoveType.SWIM) {
            return options.swimSpeed();
        }
        return options.sprint() ? options.sprintSpeed() : options.walkSpeed();
    }

    private double verticalVelocity(double dy, double horizontal, Node.MoveType moveType) {
        if (moveType == Node.MoveType.SWIM || moveType == Node.MoveType.CLIMB) {
            return Math.clamp(dy * 0.25, -speedFor(moveType), speedFor(moveType));
        }
        if (shouldJump(dy, horizontal, moveType)) {
            return options.jumpVelocity();
        }
        return actor.velocity().y();
    }

    private boolean shouldJump(double dy, double horizontal, Node.MoveType moveType) {
        if (!actor.onGround()) {
            return false;
        }
        if (moveType == Node.MoveType.PARKOUR) {
            return horizontal <= PARKOUR_JUMP_TRIGGER_DISTANCE;
        }
        if (nextMoveType() == Node.MoveType.PARKOUR && horizontal <= PARKOUR_TAKEOFF_PREP_DISTANCE) {
            return true;
        }
        return dy > 0.15
            && (moveType == Node.MoveType.STEP_UP
                || moveType == Node.MoveType.JUMP);
    }

    private Node.MoveType nextMoveType() {
        int nextIndex = waypointIndex + 1;
        if (nextIndex >= path.size()) {
            return Node.MoveType.WALK;
        }
        Node.MoveType moveType = path.get(nextIndex).moveType();
        return moveType == null ? Node.MoveType.WALK : moveType;
    }

    private static Vec3d targetCenter(Node node) {
        return new Vec3d(node.position.centeredX(), node.position.flooredY(), node.position.centeredZ());
    }

    private void finish() {
        status = NavigationStatus.FOUND;
        motor.stop();
        Runnable finished = onFinished;
        onFinished = null;
        onFailed = null;
        if (finished != null) finished.run();
    }

    private void fail() {
        status = NavigationStatus.FAILED;
        motor.stop();
        Runnable failed = onFailed;
        onFinished = null;
        onFailed = null;
        if (failed != null) failed.run();
    }
}
