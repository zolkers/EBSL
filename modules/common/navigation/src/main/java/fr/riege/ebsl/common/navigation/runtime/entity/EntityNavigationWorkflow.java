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

import fr.riege.ebsl.common.navigation.NavigationStatus;
import fr.riege.ebsl.common.navigation.PathPlan;
import fr.riege.ebsl.common.pathfinding.goal.Goal;
import fr.riege.ebsl.common.pathfinding.goal.GoalBlock;
import fr.riege.ebsl.common.pathfinding.goal.GoalNear;
import fr.riege.ebsl.common.pathfinding.goal.GoalXZ;
import fr.riege.ebsl.common.pathfinding.goal.NavigationRequest;

import java.util.Objects;

public final class EntityNavigationWorkflow {
    private static final double DEFAULT_PRECISE_TOLERANCE = 0.15;

    private final EntityNavigationService navigation;

    public EntityNavigationWorkflow(EntityNavigationService navigation) {
        this.navigation = Objects.requireNonNull(navigation, "navigation");
    }

    public EntityNavigationService navigation() {
        return navigation;
    }

    public void start(NavigationRequest request) {
        navigation.startNavigation(Objects.requireNonNull(request, "request"));
    }

    public void start(Goal goal) {
        start(NavigationRequest.builder(goal).build());
    }

    public void walkToBlock(int x, int y, int z) {
        start(new GoalBlock(x, y, z));
    }

    public void walkToColumn(int x, int z) {
        start(new GoalXZ(x, z));
    }

    public void walkNear(int x, int y, int z, double radius) {
        start(new GoalNear(x, y, z, radius));
    }

    public void preciseWalkToBlock(int x, int y, int z) {
        start(NavigationRequest.builder(new GoalNear(x, y, z, DEFAULT_PRECISE_TOLERANCE))
            .preciseGoalTolerance(DEFAULT_PRECISE_TOLERANCE)
            .build());
    }

    public void tick() {
        navigation.tick();
    }

    public void stop() {
        navigation.stop(false);
    }

    public boolean active() {
        return navigation.isNavigating();
    }

    public NavigationStatus status() {
        return navigation.pathStatus();
    }

    public PathPlan lastPlan() {
        return navigation.lastPlan();
    }
}
