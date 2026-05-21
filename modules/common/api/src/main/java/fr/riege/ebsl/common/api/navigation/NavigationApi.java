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

package fr.riege.ebsl.common.api.navigation;

import fr.riege.ebsl.common.api.core.annotation.EbslApiOperation;
import fr.riege.ebsl.common.api.core.annotation.EbslApiSurface;
import fr.riege.ebsl.common.math.Vec3d;
import fr.riege.ebsl.common.pathfinding.Node;
import fr.riege.ebsl.common.pathfinding.goal.NavigationRequest;
import fr.riege.ebsl.common.platform.service.EbslServices;
import fr.riege.ebsl.common.platform.service.NavigationService;

@EbslApiSurface(EbslApiSurface.Domain.NAVIGATION)
public final class NavigationApi {
    @EbslApiOperation("Read the current navigation service.")
    public NavigationService service() {
        return EbslServices.navigation();
    }

    @EbslApiOperation("Read the current navigation snapshot.")
    public NavigationSnapshot snapshot() {
        return NavigationSnapshot.capture(service());
    }

    @EbslApiOperation("Start navigation to a block goal.")
    public void startBlockGoal(int x, int y, int z) {
        service().startBlockGoal(x, y, z);
    }

    @EbslApiOperation("Start long-range navigation to an X/Z column.")
    public void startColumnGoal(int x, int z) {
        service().startColumnGoal(x, z);
    }

    @EbslApiOperation("Start a path test without executing the path.")
    public void startPathTest(int x, int y, int z) {
        service().startPathTest(x, y, z);
    }

    @EbslApiOperation("Start a path test to an X/Z column.")
    public void startPathTestXZ(int x, int z) {
        service().startPathTestXZ(x, z);
    }

    @EbslApiOperation("Start navigation from a structured request.")
    public void start(NavigationRequest request) {
        service().startNavigation(request);
    }

    @EbslApiOperation("Stop active navigation.")
    public void stop() {
        service().stop(true);
    }

    @EbslApiOperation("Check whether navigation is active.")
    public boolean isNavigating() {
        return service().isNavigating();
    }

    @EbslApiOperation("Read the currently executed move type.")
    public Node.MoveType currentMoveType() {
        return service().currentMoveType();
    }

    @EbslApiOperation("Start a direct walk used by bot tasks.")
    public void startGreenhouseWalk(Vec3d target, Runnable onFinished, boolean isFirst) {
        service().startGreenhouseWalk(target, onFinished, isFirst);
    }
}
