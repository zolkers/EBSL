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
import fr.riege.ebsl.common.navigation.runtime.entity.NavigationActor;

public final class HeadlessActor implements NavigationActor {
    private static final double MIN_STEP_HORIZONTAL_SPEED = 1.0e-5;

    private Vec3d position;
    private Vec3d velocity = new Vec3d(0.0, 0.0, 0.0);
    private boolean onGround = true;
    private boolean alive = true;
    private double width = 0.6;
    private double height = 1.8;

    public HeadlessActor(Vec3d position) {
        this.position = position;
    }

    @Override public Vec3d position() {
        return position;
    }

    @Override public Vec3d velocity() {
        return velocity;
    }

    @Override public boolean onGround() {
        return onGround;
    }

    @Override public boolean isAlive() {
        return alive;
    }

    @Override public double width() {
        return width;
    }

    @Override public double height() {
        return height;
    }

    public HeadlessActor position(Vec3d position) {
        this.position = position;
        return this;
    }

    public HeadlessActor velocity(Vec3d velocity) {
        this.velocity = velocity;
        return this;
    }

    public HeadlessActor onGround(boolean onGround) {
        this.onGround = onGround;
        return this;
    }

    public HeadlessActor alive(boolean alive) {
        this.alive = alive;
        return this;
    }

    public HeadlessActor size(double width, double height) {
        this.width = Math.max(0.0, width);
        this.height = Math.max(0.0, height);
        return this;
    }

    public void tick(HeadlessWorldLayer world) {
        Vec3d next = position.add(velocity.x(), velocity.y(), velocity.z());
        int footX = (int) Math.floor(next.x());
        int footY = (int) Math.floor(next.y());
        int footZ = (int) Math.floor(next.z());
        if (world != null && world.isSolid(footX, footY, footZ)) {
            if (tryStepUp(world, next, footX, footY, footZ)) {
                return;
            }
            velocity = new Vec3d(0.0, 0.0, 0.0);
            onGround = true;
            return;
        }
        position = next;
        if (world != null && world.isSolid(footX, footY - 1, footZ) && velocity.y() <= 0.0) {
            onGround = true;
            HeadlessPhysicsProfile profile = world.physicsAt(footX, footY - 1, footZ);
            double groundDrag = profile.slipperiness() * profile.horizontalDrag();
            velocity = new Vec3d(velocity.x() * groundDrag, 0.0, velocity.z() * groundDrag);
        } else {
            onGround = false;
            HeadlessPhysicsProfile profile = world == null
                ? HeadlessPhysicsProfile.DEFAULT
                : world.physicsAt(footX, footY, footZ);
            double velocityY = (velocity.y() - profile.gravity()) * profile.verticalDrag();
            if (world != null && world.isClimbable(footX, footY, footZ)) {
                velocityY = Math.clamp(velocityY, -profile.maxClimbVelocity(), profile.maxClimbVelocity());
            }
            velocity = new Vec3d(
                velocity.x() * profile.horizontalDrag(),
                velocityY,
                velocity.z() * profile.horizontalDrag());
        }
    }

    private boolean tryStepUp(HeadlessWorldLayer world, Vec3d blockedPosition, int footX, int footY, int footZ) {
        double horizontalSpeed = Math.sqrt(velocity.x() * velocity.x() + velocity.z() * velocity.z());
        if (horizontalSpeed <= MIN_STEP_HORIZONTAL_SPEED || velocity.y() < -0.2) {
            return false;
        }

        HeadlessPhysicsProfile profile = world.physicsAt(footX, footY, footZ);
        int steppedY = footY + (profile.stepHeight() >= 1.0 ? 1 : 0);
        if (steppedY == footY) {
            return false;
        }
        if (world.isSolid(footX, steppedY, footZ) || world.isSolid(footX, steppedY + 1, footZ)) {
            return false;
        }

        position = new Vec3d(blockedPosition.x(), steppedY, blockedPosition.z());
        velocity = new Vec3d(velocity.x(), 0.0, velocity.z());
        onGround = true;
        return true;
    }
}
