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

package fr.riege.ebsl.common.api;

import fr.riege.ebsl.common.math.Vec3d;
import fr.riege.ebsl.common.navigation.PathPlannerOptions;
import fr.riege.ebsl.common.navigation.runtime.headless.HeadlessBlockState;
import fr.riege.ebsl.common.pathfinding.wrapper.PathPosition;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EbslApiRuntimeTest {
    @Test
    void publicApiCreatesHeadlessRuntimeAndPlansPath() {
        var world = EbslApi.runtime().headlessWorld()
            .fill(-4, 63, -4, 8, 63, 4, HeadlessBlockState.STONE);
        var actor = EbslApi.runtime().headlessActor(new Vec3d(0.5, 64.0, 0.5));
        var service = EbslApi.runtime().headlessNavigation(world, actor);

        service.setPlannerOptions(PathPlannerOptions.defaults().toBuilder().async(false).build());
        service.startBlockGoal(4, 64, 0);
        service.tick();

        assertTrue(service.isNavigating(), "runtime API should create an executable headless service");
        assertNotEquals(0, service.lastPathNodeCount(), "runtime API should expose a populated path");
    }

    @Test
    void publicApiPlansAgainstWorldLayer() {
        var world = EbslApi.runtime().flatHeadlessWorld(63);
        var plan = EbslApi.pathfinding().plan(
            world,
            new PathPosition(0, 64, 0),
            new PathPosition(3, 64, 0),
            PathPlannerOptions.defaults().toBuilder().async(false).build()
        ).toCompletableFuture().join();

        assertTrue(plan.usable(), "pathfinding API should expose usable plans");
    }
}
