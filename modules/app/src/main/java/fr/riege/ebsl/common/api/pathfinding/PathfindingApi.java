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
package fr.riege.ebsl.common.api.pathfinding;

import fr.riege.ebsl.common.api.EbslApi;
import fr.riege.ebsl.common.api.core.annotation.EbslApiOperation;
import fr.riege.ebsl.common.api.core.annotation.EbslApiSurface;
import fr.riege.ebsl.common.api.navigation.NavigationSnapshot;
import fr.riege.ebsl.common.navigation.PathPlan;
import fr.riege.ebsl.common.navigation.PathPlannerOptions;
import fr.riege.ebsl.common.navigation.PathPlanningService;
import fr.riege.ebsl.common.pathfinding.wrapper.PathPosition;
import fr.riege.ebsl.common.world.layer.IWorldLayer;

import java.util.concurrent.CompletionStage;

@EbslApiSurface(EbslApiSurface.Domain.PATHFINDING)
public final class PathfindingApi {
    @EbslApiOperation("Read a pathfinding snapshot from the active navigation service.")
    public PathfindingSnapshot snapshot() {
        NavigationSnapshot snapshot = EbslApi.navigation().snapshot();
        return new PathfindingSnapshot(
            snapshot.status(),
            snapshot.navigating(),
            snapshot.currentMoveType(),
            snapshot.walkSneakLatched(),
            snapshot.pathNodeCount());
    }

    @EbslApiOperation("Create a reusable path planner for a world layer.")
    public PathPlanningService planner(IWorldLayer world) {
        return new PathPlanningService(world);
    }

    @EbslApiOperation("Plan a path against a world layer.")
    public CompletionStage<PathPlan> plan(IWorldLayer world, PathPosition start, PathPosition target, PathPlannerOptions options) {
        return planner(world).plan(start, target, options);
    }

    @EbslApiOperation("Stop active navigation.")
    public void stop() {
        EbslApi.navigation().stop();
    }
}
