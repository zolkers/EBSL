/*
 * This file is part of EBSL.
 *
 * EBSL is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * EBSL is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package fr.riege.ebsl.common.pathfinding.movement.types.execution;

import fr.riege.ebsl.common.math.Vec3d;
import fr.riege.ebsl.common.pathfinding.Node;

@SuppressWarnings("java:S107")
public final class MovementExecutionContext {
    private final Node waypoint;
    private final Vec3d playerPos;
    private final boolean partialSupportAscent;
    private final boolean inStairSequence;
    private final boolean playerOnGround;
    private final int jumpCooldown;
    private final long millisSinceProgress;
    private final double horizontalDistance;
    private final double verticalDelta;
    private final double stepUpTriggerDistance;
    private final double jumpTriggerDistance;
    private final double parkourTriggerDistance;
    private final int parkourDistanceBlocks;
    private final int jumpCooldownTicks;
    private final long stallJumpProgressMs;
    private boolean jumpPressed;
    private boolean jumpCooldownConsumed;

    public MovementExecutionContext(Node waypoint, Vec3d playerPos,
                                    boolean partialSupportAscent, boolean inStairSequence,
                                    boolean playerOnGround, int jumpCooldown, long millisSinceProgress,
                                    double horizontalDistance, double verticalDelta,
                                    double stepUpTriggerDistance, double jumpTriggerDistance,
                                    double parkourTriggerDistance, int parkourDistanceBlocks, int jumpCooldownTicks,
                                    long stallJumpProgressMs) {
        this.waypoint = waypoint;
        this.playerPos = playerPos;
        this.partialSupportAscent = partialSupportAscent;
        this.inStairSequence = inStairSequence;
        this.playerOnGround = playerOnGround;
        this.jumpCooldown = jumpCooldown;
        this.millisSinceProgress = millisSinceProgress;
        this.horizontalDistance = horizontalDistance;
        this.verticalDelta = verticalDelta;
        this.stepUpTriggerDistance = stepUpTriggerDistance;
        this.jumpTriggerDistance = jumpTriggerDistance;
        this.parkourTriggerDistance = parkourTriggerDistance;
        this.parkourDistanceBlocks = parkourDistanceBlocks;
        this.jumpCooldownTicks = jumpCooldownTicks;
        this.stallJumpProgressMs = stallJumpProgressMs;
    }

    public Node waypoint() { return waypoint; }
    public Vec3d playerPos() { return playerPos; }
    public boolean partialSupportAscent() { return partialSupportAscent; }
    public boolean inStairSequence() { return inStairSequence; }
    public boolean playerOnGround() { return playerOnGround; }
    public int jumpCooldown() { return jumpCooldown; }
    public long millisSinceProgress() { return millisSinceProgress; }
    public double horizontalDistance() { return horizontalDistance; }
    public double verticalDelta() { return verticalDelta; }
    public double stepUpTriggerDistance() { return stepUpTriggerDistance; }
    public double jumpTriggerDistance() { return jumpTriggerDistance; }
    public double parkourTriggerDistance() { return parkourTriggerDistance; }
    public int parkourDistanceBlocks() { return parkourDistanceBlocks; }
    public int jumpCooldownTicks() { return jumpCooldownTicks; }
    public long stallJumpProgressMs() { return stallJumpProgressMs; }

    public boolean canStartJump() {
        return playerOnGround && jumpCooldown == 0;
    }

    public boolean isStalled() {
        return millisSinceProgress > stallJumpProgressMs;
    }

    public void pressJump() {
        jumpPressed = true;
        jumpCooldownConsumed = true;
    }

    public void releaseJump() {
        jumpPressed = false;
    }

    public boolean jumpPressed() { return jumpPressed; }
    public boolean jumpCooldownConsumed() { return jumpCooldownConsumed; }
}
