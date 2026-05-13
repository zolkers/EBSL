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
package fr.riege.ebsl.common.navigation.runtime.headless;

import fr.riege.ebsl.common.math.Vec3d;
import fr.riege.ebsl.common.navigation.runtime.entity.MovementIntent;
import fr.riege.ebsl.common.navigation.runtime.entity.NavigationMotor;

public final class HeadlessMotor implements NavigationMotor {
    private final HeadlessActor actor;
    private MovementIntent lastIntent = MovementIntent.stop();

    public HeadlessMotor(HeadlessActor actor) {
        this.actor = actor;
    }

    public MovementIntent lastIntent() {
        return lastIntent;
    }

    @Override public void apply(MovementIntent intent) {
        lastIntent = intent == null ? MovementIntent.stop() : intent;
        Vec3d velocity = lastIntent.velocity();
        if (lastIntent.jump() && actor.onGround()) {
            velocity = new Vec3d(velocity.x(), Math.max(velocity.y(), 0.42), velocity.z());
        }
        actor.velocity(velocity);
    }
}
