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

import fr.riege.ebsl.common.feature.scripting.manager.EbslGraphDocument;
import fr.riege.ebsl.common.feature.scripting.runtime.EbslGraphRunner;
import fr.riege.ebsl.common.feature.scripting.runtime.EbslScriptEngine;
import fr.riege.ebsl.common.navigation.runtime.entity.EntityNavigationAgent;
import fr.riege.ebsl.common.platform.EbslPlatform;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class EntityBrainFactory {
    private EntityBrainFactory() {
    }

    public static Builder create(EntityNavigationAgent agent) {
        return new Builder(agent);
    }

    public static final class Builder {
        private final EntityNavigationAgent agent;
        private final List<EntityBrainProgram> programs = new ArrayList<>();
        private EntityBrainMemory memory = new EntityBrainMemory();
        private EbslGraphDocument graph;
        private EbslPlatform platform;

        private Builder(EntityNavigationAgent agent) {
            this.agent = Objects.requireNonNull(agent, "agent");
        }

        public Builder memory(EntityBrainMemory value) {
            memory = value == null ? new EntityBrainMemory() : value;
            return this;
        }

        public Builder program(EntityBrainProgram value) {
            if (value != null) {
                programs.add(value);
            }
            return this;
        }

        public Builder graph(EbslGraphDocument value, EbslPlatform graphPlatform) {
            graph = value;
            platform = graphPlatform;
            return this;
        }

        public EntityBrain build() {
            EbslGraphRunner runner = graph == null
                ? null
                : EbslScriptEngine.graphRunner(graph, platform, agent.navigation());
            return new EntityBrain(agent, memory, programs, runner);
        }
    }
}
