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

package fr.riege.ebsl.common.api.navigation.runtime;

import fr.riege.ebsl.common.api.core.annotation.EbslApiOperation;
import fr.riege.ebsl.common.api.core.annotation.EbslApiSurface;
import fr.riege.ebsl.common.math.Vec3d;
import fr.riege.ebsl.common.navigation.runtime.entity.EntityFollowerOptions;
import fr.riege.ebsl.common.navigation.runtime.entity.EntityNavigationAgent;
import fr.riege.ebsl.common.navigation.runtime.entity.EntityNavigationFactory;
import fr.riege.ebsl.common.navigation.runtime.entity.EntityNavigationService;
import fr.riege.ebsl.common.navigation.runtime.entity.NavigationActor;
import fr.riege.ebsl.common.navigation.runtime.entity.NavigationMotor;
import fr.riege.ebsl.common.navigation.runtime.headless.HeadlessActor;
import fr.riege.ebsl.common.navigation.runtime.headless.HeadlessNavigationAgent;
import fr.riege.ebsl.common.navigation.runtime.headless.HeadlessNavigationFactory;
import fr.riege.ebsl.common.navigation.runtime.headless.HeadlessMotor;
import fr.riege.ebsl.common.navigation.runtime.headless.HeadlessNavigationService;
import fr.riege.ebsl.common.navigation.runtime.headless.HeadlessWorldLayer;
import fr.riege.ebsl.common.navigation.runtime.server.ServerNavigationRuntime;
import fr.riege.ebsl.common.world.layer.IWorldLayer;

import java.util.concurrent.Executor;
import java.util.function.Consumer;

@EbslApiSurface(EbslApiSurface.Domain.RUNTIME)
public final class RuntimeApi {
    @EbslApiOperation("Create a mutable headless world.")
    public HeadlessWorldLayer headlessWorld() {
        return new HeadlessWorldLayer();
    }

    @EbslApiOperation("Create a flat headless world with a solid floor.")
    public HeadlessWorldLayer flatHeadlessWorld(int floorY) {
        return HeadlessWorldLayer.flat(floorY);
    }

    @EbslApiOperation("Create a headless navigation actor.")
    public HeadlessActor headlessActor(Vec3d position) {
        return new HeadlessActor(position);
    }

    @EbslApiOperation("Create a headless motor for an actor.")
    public HeadlessMotor headlessMotor(HeadlessActor actor) {
        return new HeadlessMotor(actor);
    }

    @EbslApiOperation("Create a complete headless navigation service.")
    public HeadlessNavigationService headlessNavigation(HeadlessWorldLayer world, HeadlessActor actor) {
        return new HeadlessNavigationService(world, actor);
    }

    @EbslApiOperation("Create a complete headless navigation agent with isolated planner, motor and service.")
    public HeadlessNavigationAgent headlessNavigationAgent(HeadlessWorldLayer world, HeadlessActor actor) {
        return HeadlessNavigationFactory.create(world, actor);
    }

    @EbslApiOperation("Create a server/entity navigation service over custom adapters.")
    public EntityNavigationService entityNavigation(IWorldLayer world, NavigationActor actor, NavigationMotor motor) {
        return entityNavigationAgent(world, actor, motor).navigation();
    }

    @EbslApiOperation("Create a server/entity navigation service with follower options.")
    public EntityNavigationService entityNavigation(IWorldLayer world,
                                                    NavigationActor actor,
                                                    NavigationMotor motor,
                                                    EntityFollowerOptions options) {
        return EntityNavigationFactory.create(
            world,
            actor,
            motor,
            options,
            null,
            Runnable::run).navigation();
    }

    @EbslApiOperation("Create a server/entity navigation agent over custom adapters.")
    public EntityNavigationAgent entityNavigationAgent(IWorldLayer world, NavigationActor actor, NavigationMotor motor) {
        return EntityNavigationFactory.create(world, actor, motor);
    }

    @EbslApiOperation("Create a server-only navigation runtime without terminal, rendering or client input.")
    public ServerNavigationRuntime serverRuntime(IWorldLayer world) {
        return ServerNavigationRuntime.direct(world);
    }

    @EbslApiOperation("Create a server-only navigation runtime whose callbacks are scheduled on a server thread.")
    public ServerNavigationRuntime serverRuntime(IWorldLayer world, Consumer<Runnable> scheduler) {
        return ServerNavigationRuntime.scheduled(world, scheduler);
    }

    @EbslApiOperation("Create a server-only navigation runtime backed by an executor.")
    public ServerNavigationRuntime serverRuntime(IWorldLayer world, Executor executor) {
        return ServerNavigationRuntime.scheduled(world, executor);
    }
}
