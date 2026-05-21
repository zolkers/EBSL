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

import fr.riege.ebsl.common.navigation.PathPlannerOptions;
import fr.riege.ebsl.common.navigation.runtime.entity.EntityFollowerOptions;
import fr.riege.ebsl.common.navigation.runtime.entity.EntityNavigationAgent;
import fr.riege.ebsl.common.navigation.runtime.entity.EntityNavigationFactory;

import java.util.Objects;

public final class HeadlessNavigationFactory {
    private HeadlessNavigationFactory() {
    }

    public static HeadlessNavigationAgent create(HeadlessWorldLayer world, HeadlessActor actor) {
        return create(world, actor, new HeadlessMotor(actor), EntityFollowerOptions.defaults(), PathPlannerOptions.defaults());
    }

    public static HeadlessNavigationAgent create(HeadlessWorldLayer world,
                                                HeadlessActor actor,
                                                EntityFollowerOptions followerOptions,
                                                PathPlannerOptions plannerOptions) {
        return create(world, actor, new HeadlessMotor(actor), followerOptions, plannerOptions);
    }

    public static HeadlessNavigationAgent create(HeadlessWorldLayer world,
                                                HeadlessActor actor,
                                                HeadlessMotor motor,
                                                EntityFollowerOptions followerOptions,
                                                PathPlannerOptions plannerOptions) {
        HeadlessWorldLayer checkedWorld = Objects.requireNonNull(world, "world");
        HeadlessActor checkedActor = Objects.requireNonNull(actor, "actor");
        HeadlessMotor checkedMotor = Objects.requireNonNull(motor, "motor").world(checkedWorld);
        EntityNavigationAgent agent = EntityNavigationFactory.create(
            checkedWorld,
            checkedActor,
            checkedMotor,
            followerOptions,
            plannerOptions,
            Runnable::run);
        return new HeadlessNavigationAgent(agent.planner(), checkedWorld, checkedActor, checkedMotor, agent.navigation());
    }
}
