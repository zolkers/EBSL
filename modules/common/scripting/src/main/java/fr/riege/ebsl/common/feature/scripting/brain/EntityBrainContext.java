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

package fr.riege.ebsl.common.feature.scripting.brain;

import fr.riege.ebsl.common.math.Vec3d;
import fr.riege.ebsl.common.navigation.NavigationStatus;
import fr.riege.ebsl.common.navigation.runtime.entity.EntityNavigationAgent;
import fr.riege.ebsl.common.navigation.runtime.entity.EntityNavigationWorkflow;
import fr.riege.ebsl.common.navigation.runtime.entity.NavigationActor;

import java.util.Objects;

public record EntityBrainContext(EntityNavigationAgent agent, EntityBrainMemory memory, long tick) {
    public EntityBrainContext {
        Objects.requireNonNull(agent, "agent");
        Objects.requireNonNull(memory, "memory");
    }

    public NavigationActor actor() {
        return agent.actor();
    }

    public EntityNavigationWorkflow workflow() {
        return agent.workflow();
    }

    public Vec3d position() {
        return actor().position();
    }

    public NavigationStatus navigationStatus() {
        return workflow().status();
    }

    public boolean navigationActive() {
        return workflow().active();
    }

    public double distanceTo(double x, double y, double z) {
        Vec3d position = position();
        double dx = x - position.x();
        double dy = y - position.y();
        double dz = z - position.z();
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }
}
