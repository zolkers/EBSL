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

import fr.riege.ebsl.common.navigation.PathPlannerOptions;
import fr.riege.ebsl.common.navigation.PathPlanningService;
import fr.riege.ebsl.common.world.layer.IWorldLayer;

import java.util.Objects;
import java.util.function.Consumer;

public final class EntityNavigationFactory {
    private EntityNavigationFactory() {
    }

    public static Builder builder(IWorldLayer world, NavigationActor actor, NavigationMotor motor) {
        return new Builder(world, actor, motor);
    }

    public static EntityNavigationAgent create(IWorldLayer world, NavigationActor actor, NavigationMotor motor) {
        return create(world, actor, motor, EntityNavigationSettings.defaults(), Runnable::run);
    }

    public static EntityNavigationAgent create(IWorldLayer world,
                                               NavigationActor actor,
                                               NavigationMotor motor,
                                               EntityNavigationSettings settings) {
        return create(world, actor, motor, settings, Runnable::run);
    }

    public static EntityNavigationAgent create(IWorldLayer world,
                                               NavigationActor actor,
                                               NavigationMotor motor,
                                               EntityNavigationSettings settings,
                                               Consumer<Runnable> callbackThread) {
        EntityNavigationSettings checkedSettings = settings == null ? EntityNavigationSettings.defaults() : settings;
        return create(
            world,
            actor,
            motor,
            checkedSettings.followerOptions(),
            checkedSettings.plannerOptions(),
            callbackThread);
    }

    public static EntityNavigationAgent create(IWorldLayer world,
                                               NavigationActor actor,
                                               NavigationMotor motor,
                                               EntityFollowerOptions followerOptions,
                                               PathPlannerOptions plannerOptions,
                                               Consumer<Runnable> callbackThread) {
        PathPlanningService planner = new PathPlanningService(Objects.requireNonNull(world, "world"));
        EntityNavigationService navigation = new EntityNavigationService(
            planner,
            Objects.requireNonNull(actor, "actor"),
            Objects.requireNonNull(motor, "motor"),
            followerOptions,
            callbackThread);
        navigation.setPlannerOptions(plannerOptions);
        return new EntityNavigationAgent(planner, actor, motor, navigation, new EntityNavigationWorkflow(navigation));
    }

    public static final class Builder {
        private final IWorldLayer world;
        private final NavigationActor actor;
        private final NavigationMotor motor;
        private EntityNavigationSettings settings = EntityNavigationSettings.defaults();
        private Consumer<Runnable> callbackThread = Runnable::run;

        private Builder(IWorldLayer world, NavigationActor actor, NavigationMotor motor) {
            this.world = Objects.requireNonNull(world, "world");
            this.actor = Objects.requireNonNull(actor, "actor");
            this.motor = Objects.requireNonNull(motor, "motor");
        }

        public Builder settings(EntityNavigationSettings settings) {
            this.settings = settings == null ? EntityNavigationSettings.defaults() : settings;
            return this;
        }

        public Builder followerOptions(EntityFollowerOptions options) {
            settings = settings.withFollowerOptions(options);
            return this;
        }

        public Builder plannerOptions(PathPlannerOptions options) {
            settings = settings.withPlannerOptions(options);
            return this;
        }

        public Builder callbackThread(Consumer<Runnable> callbackThread) {
            this.callbackThread = callbackThread == null ? Runnable::run : callbackThread;
            return this;
        }

        public EntityNavigationAgent build() {
            return create(world, actor, motor, settings, callbackThread);
        }
    }
}
