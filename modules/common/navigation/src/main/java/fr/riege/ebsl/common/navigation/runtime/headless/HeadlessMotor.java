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
        Vec3d velocity = lastIntent.velocity();
        if (lastIntent.jump() && actor.onGround()) {
            HeadlessPhysicsProfile profile = jumpProfile();
            velocity = new Vec3d(velocity.x(), Math.max(velocity.y(), profile.jumpVelocity()), velocity.z());
        }
        actor.velocity(velocity);
    }

    private HeadlessPhysicsProfile jumpProfile() {
        if (world == null) {
            return HeadlessPhysicsProfile.DEFAULT;
        }
        int x = (int) Math.floor(actor.position().x());
        int y = (int) Math.floor(actor.position().y()) - 1;
        int z = (int) Math.floor(actor.position().z());
        return world.physicsAt(x, y, z);
    }
}
