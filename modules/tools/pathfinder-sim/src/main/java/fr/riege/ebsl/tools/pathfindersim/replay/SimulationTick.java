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

package fr.riege.ebsl.tools.pathfindersim.replay;

import fr.riege.ebsl.common.math.Vec3d;
import fr.riege.ebsl.common.navigation.NavigationStatus;
import fr.riege.ebsl.common.navigation.runtime.entity.EntityNavigationService;
import fr.riege.ebsl.common.navigation.runtime.entity.MovementIntent;
import fr.riege.ebsl.common.navigation.runtime.headless.HeadlessActor;
import fr.riege.ebsl.common.navigation.runtime.headless.HeadlessMotor;
import fr.riege.ebsl.common.pathfinding.Node;

public record SimulationTick(
    int tick,
    Vec3d position,
    Vec3d velocity,
    NavigationStatus status,
    Node.MoveType moveType,
    double distanceToGoal,
    boolean stuck,
    boolean jump,
    boolean sprint,
    boolean sneak
) {
    public static SimulationTick capture(int tick, HeadlessActor actor, HeadlessMotor motor,
                                         EntityNavigationService service, double distanceToGoal, boolean stuck) {
        MovementIntent intent = motor.lastIntent();
        return new SimulationTick(
            tick,
            actor.position(),
            actor.velocity(),
            service.pathStatus(),
            service.currentMoveType(),
            distanceToGoal,
            stuck,
            intent.jump(),
            intent.sprint(),
            intent.sneak());
    }
}
