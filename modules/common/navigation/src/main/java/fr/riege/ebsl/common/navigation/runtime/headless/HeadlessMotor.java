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
import fr.riege.ebsl.common.navigation.runtime.entity.MovementIntent;
import fr.riege.ebsl.common.navigation.runtime.entity.NavigationMotor;

public final class HeadlessMotor implements NavigationMotor {
    private final HeadlessActor actor;
    private HeadlessWorldLayer world;
    private MovementIntent lastIntent = MovementIntent.stop();

    public HeadlessMotor(HeadlessActor actor) {
        this.actor = actor;
    }

    public MovementIntent lastIntent() {
        return lastIntent;
    }

    public HeadlessMotor world(HeadlessWorldLayer world) {
        this.world = world;
        return this;
    }

    @Override public void apply(MovementIntent intent) {
        lastIntent = intent == null ? MovementIntent.stop() : intent;
        HeadlessPhysicsProfile profile = movementProfile();
        Vec3d requestedVelocity = lastIntent.sneak()
            ? scaleHorizontal(lastIntent.velocity(), profile.sneakVelocityScale())
            : lastIntent.velocity();
        Vec3d velocity = accelerate(actor.velocity(), requestedVelocity, acceleration(profile));
        if (lastIntent.jump() && actor.onGround()) {
            velocity = new Vec3d(velocity.x(), Math.max(velocity.y(), profile.jumpVelocity()), velocity.z());
        }
        actor.velocity(velocity);
    }

    private HeadlessPhysicsProfile movementProfile() {
        if (world == null) {
            return HeadlessPhysicsProfile.DEFAULT;
        }
        int x = (int) Math.floor(actor.position().x());
        int y = (int) Math.floor(actor.position().y()) + (actor.onGround() ? -1 : 0);
        int z = (int) Math.floor(actor.position().z());
        return world.physicsAt(x, y, z);
    }

    private double acceleration(HeadlessPhysicsProfile profile) {
        if (world != null && isInFluid()) {
            return profile.fluidAcceleration();
        }
        return actor.onGround() ? profile.groundAcceleration() : profile.airAcceleration();
    }

    private boolean isInFluid() {
        int x = (int) Math.floor(actor.position().x());
        int y = (int) Math.floor(actor.position().y());
        int z = (int) Math.floor(actor.position().z());
        return world.isWater(x, y, z) || world.isLava(x, y, z);
    }

    private static Vec3d scaleHorizontal(Vec3d value, double scale) {
        return new Vec3d(value.x() * scale, value.y(), value.z() * scale);
    }

    private static Vec3d accelerate(Vec3d current, Vec3d target, double maxDelta) {
        return new Vec3d(
            approach(current.x(), target.x(), maxDelta),
            target.y(),
            approach(current.z(), target.z(), maxDelta));
    }

    private static double approach(double current, double target, double maxDelta) {
        double delta = target - current;
        if (Math.abs(delta) <= maxDelta) {
            return target;
        }
        return current + Math.signum(delta) * maxDelta;
    }
}
