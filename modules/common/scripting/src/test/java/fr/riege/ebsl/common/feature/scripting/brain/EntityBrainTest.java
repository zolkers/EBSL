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

import fr.riege.ebsl.common.domain.world.BlockId;
import fr.riege.ebsl.common.math.Vec3d;
import fr.riege.ebsl.common.navigation.PathPlannerOptions;
import fr.riege.ebsl.common.navigation.runtime.entity.EntityNavigationAgent;
import fr.riege.ebsl.common.navigation.runtime.entity.EntityNavigationFactory;
import fr.riege.ebsl.common.navigation.runtime.entity.EntityNavigationSettings;
import fr.riege.ebsl.common.navigation.runtime.entity.MovementIntent;
import fr.riege.ebsl.common.navigation.runtime.entity.NavigationActor;
import fr.riege.ebsl.common.navigation.runtime.entity.NavigationMotor;
import fr.riege.ebsl.common.feature.scripting.manager.EbslGraphDocument;
import fr.riege.ebsl.common.feature.scripting.manager.EbslGraphNode;
import fr.riege.ebsl.common.world.layer.IWorldLayer;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EntityBrainTest {
    @Test
    void ticksAttachedProgramsWithEntityContext() {
        EntityNavigationAgent agent = agent();
        EntityBrain brain = EntityBrainFactory.create(agent)
            .program(context -> context.memory().set("lastX", context.position().x()))
            .program(context -> context.workflow().walkToBlock(4, 64, 0))
            .build();

        brain.tick();

        assertEquals(1L, brain.tickCount());
        assertEquals(0.5, brain.memory().get("lastX"));
        assertTrue(agent.navigation().isNavigating());
    }

    @Test
    void routesGraphNavigationNodesThroughTheAttachedEntity() {
        EntityNavigationAgent agent = agent();
        EbslGraphDocument graph = new EbslGraphDocument(
            Map.of(),
            List.of(),
            Map.of("goto", EbslGraphNode.action("goto", "goto", Map.of("x", "5", "y", "64", "z", "0"))));
        EntityBrain brain = EntityBrainFactory.create(agent).graph(graph, null).build();

        brain.tick();

        assertTrue(agent.navigation().isNavigating());
        assertTrue(agent.navigation().lastPathNodeCount() > 0);
    }

    @Test
    void routesInlineEbslScriptsThroughTheAttachedEntity() {
        EntityNavigationAgent agent = agent();
        EntityBrain brain = EntityBrainFactory.create(agent)
            .script("goto 5 64 0", null)
            .build();

        brain.tick();

        assertTrue(agent.navigation().isNavigating());
        assertTrue(agent.navigation().lastPathNodeCount() > 0);
        assertEquals("running", brain.scriptStatus());
    }

    private static EntityNavigationAgent agent() {
        PathPlannerOptions plannerOptions = PathPlannerOptions.defaults().toBuilder()
            .async(false)
            .fallback(false)
            .processPath(true)
            .build();
        EntityNavigationSettings settings = EntityNavigationSettings.defaults().withPlannerOptions(plannerOptions);
        return EntityNavigationFactory.create(new FlatWorld(), new TestActor(), new TestMotor(), settings);
    }

    private static final class TestActor implements NavigationActor {
        @Override public Vec3d position() {
            return new Vec3d(0.5, 64.0, 0.5);
        }
    }

    private static final class TestMotor implements NavigationMotor {
        @Override public void apply(MovementIntent intent) {
            // Brain tests assert command routing, not physical movement.
        }
    }

    private static final class FlatWorld implements IWorldLayer {
        @Override public BlockId getBlock(int x, int y, int z) {
            return y < 64 ? BlockId.of("minecraft:stone") : BlockId.of("minecraft:air");
        }

        @Override public boolean isAir(int x, int y, int z) {
            return y >= 64;
        }

        @Override public boolean isSolid(int x, int y, int z) {
            return y < 64;
        }

        @Override public boolean isWater(int x, int y, int z) {
            return false;
        }

        @Override public boolean isLava(int x, int y, int z) {
            return false;
        }

        @Override public boolean isLoaded(int x, int y, int z) {
            return true;
        }

        @Override public int getTopSolidY(int x, int z) {
            return 63;
        }

        @Override public double getBlockHeight(int x, int y, int z) {
            return isSolid(x, y, z) ? 1.0 : 0.0;
        }
    }
}
