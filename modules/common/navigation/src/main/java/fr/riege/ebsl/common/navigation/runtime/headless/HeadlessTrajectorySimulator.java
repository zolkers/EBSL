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

import java.util.ArrayList;
import java.util.List;

public final class HeadlessTrajectorySimulator {
    private HeadlessTrajectorySimulator() {
    }

    public static List<TrajectoryState> simulate(HeadlessActor source,
                                                 HeadlessWorldLayer world,
                                                 List<MovementIntent> intents) {
        if (source == null || intents == null || intents.isEmpty()) {
            return List.of();
        }
        HeadlessActor actor = copy(source);
        HeadlessMotor motor = new HeadlessMotor(actor).world(world);
        List<TrajectoryState> states = new ArrayList<>(intents.size());
        for (int i = 0; i < intents.size(); i++) {
            motor.apply(intents.get(i));
            actor.tick(world);
            states.add(TrajectoryState.capture(i + 1, actor, motor.lastIntent()));
        }
        return List.copyOf(states);
    }

    private static HeadlessActor copy(HeadlessActor source) {
        return new HeadlessActor(source.position())
            .velocity(source.velocity())
            .onGround(source.onGround())
            .alive(source.isAlive())
            .size(source.width(), source.height());
    }

    public record TrajectoryState(
        int tick,
        Vec3d position,
        Vec3d velocity,
        boolean onGround,
        MovementIntent intent
    ) {
        static TrajectoryState capture(int tick, HeadlessActor actor, MovementIntent intent) {
            return new TrajectoryState(tick, actor.position(), actor.velocity(), actor.onGround(), intent);
        }
    }
}
