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

import fr.riege.ebsl.common.feature.scripting.runtime.EbslGraphRunner;
import fr.riege.ebsl.common.feature.scripting.runtime.EbslRunner;
import fr.riege.ebsl.common.navigation.runtime.entity.EntityNavigationAgent;

import java.util.List;
import java.util.Objects;

public final class EntityBrain {
    private final EntityNavigationAgent agent;
    private final EntityBrainMemory memory;
    private final List<EntityBrainProgram> programs;
    private final EbslGraphRunner graphRunner;
    private final EbslRunner scriptRunner;
    private long tick;

    EntityBrain(EntityNavigationAgent agent,
                EntityBrainMemory memory,
                List<EntityBrainProgram> programs,
                EbslGraphRunner graphRunner,
                EbslRunner scriptRunner) {
        this.agent = Objects.requireNonNull(agent, "agent");
        this.memory = memory == null ? new EntityBrainMemory() : memory;
        this.programs = programs == null ? List.of() : List.copyOf(programs);
        this.graphRunner = graphRunner;
        this.scriptRunner = scriptRunner;
    }

    public EntityNavigationAgent agent() {
        return agent;
    }

    public EntityBrainMemory memory() {
        return memory;
    }

    public long tickCount() {
        return tick;
    }

    public String graphStatus() {
        return graphRunner == null ? "none" : graphRunner.status();
    }

    public String scriptStatus() {
        return scriptRunner == null ? "none" : scriptRunner.status();
    }

    public void tick() {
        tick++;
        EntityBrainContext context = new EntityBrainContext(agent, memory, tick);
        for (EntityBrainProgram program : programs) {
            program.tick(context);
        }
        if (graphRunner != null && !graphRunner.done()) {
            graphRunner.tick();
        }
        if (scriptRunner != null && !scriptRunner.done()) {
            scriptRunner.tick();
        }
        agent.workflow().tick();
    }

    public void stop() {
        if (graphRunner != null) {
            graphRunner.stop();
        }
        if (scriptRunner != null) {
            scriptRunner.stop();
        }
        agent.workflow().stop();
    }
}
