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

package fr.riege.ebsl.common.feature.registry;

import fr.riege.ebsl.common.core.event.CoreRegistries;
import fr.riege.ebsl.common.domain.world.BlockId;
import fr.riege.ebsl.common.domain.world.WorldRegistries;
import fr.riege.ebsl.common.feature.scripting.EbslNode;
import fr.riege.ebsl.common.feature.scripting.highlight.EbslTokenKind;
import fr.riege.ebsl.common.feature.terminal.CommandResult;
import fr.riege.ebsl.common.feature.terminal.CommandSpec;
import fr.riege.ebsl.common.pathfinding.Node;
import fr.riege.ebsl.common.pathfinding.pathing.processing.NodeProcessorType;
import fr.riege.ebsl.common.pathfinding.registry.PathfindingRegistries;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

final class ProjectRegistryCatalogTest {
    @Test
    void coreAndWorldCatalogsExposeRegisteredBehavior() {
        CoreRegistries.events().register(RegistryProbeEvent.class);

        assertTrue(CoreRegistries.events().all().stream()
            .anyMatch(entry -> entry.name().equals("RegistryProbe")));
        assertTrue(WorldRegistries.blockGroups().isLeaf(BlockId.of("minecraft:oak_leaves")));
        assertTrue(WorldRegistries.blockGroups().isWood(BlockId.of("minecraft:oak_log")));
        assertTrue(WorldRegistries.blockSelectors().matches(BlockId.of("minecraft:oak_leaves"), "leaf"));

        WorldRegistries.blockSelectors().register("catalog_probe", id -> id.path().equals("probe_block"));

        assertTrue(WorldRegistries.blockSelectors().matches(BlockId.of("mod:probe_block"), "catalog_probe"));
        assertFalse(WorldRegistries.blockSelectors().matches(BlockId.of("mod:other"), "catalog_probe"));
    }

    @Test
    void pathfindingCatalogsExposePlannersWithoutConcreteRegistryAccess() {
        assertFalse(PathfindingRegistries.nodeProcessors().standardProcessors().isEmpty());
        assertFalse(PathfindingRegistries.nodeProcessors().processors(NodeProcessorType.LAYER).isEmpty());
        assertNotNull(PathfindingRegistries.movementEvaluators().get(Node.MoveType.WALK));
        assertNotNull(PathfindingRegistries.movementEvaluators().get(Node.MoveType.PARKOUR));
    }

    @Test
    void featureCatalogsRouteCommandsAndScripting() {
        FeatureRegistries.commands().register(CommandSpec.named("catalog_probe")
            .description("Catalog probe")
            .usage("catalog_probe")
            .bothScopes()
            .executes(ctx -> CommandResult.ok("catalog ok"))
            .build());

        CommandResult result = FeatureRegistries.commands().dispatch("catalog_probe");
        EbslNode node = FeatureRegistries.scripting().nodes().get("walk");

        assertTrue(result.success());
        assertEquals("catalog ok", result.lines().getFirst());
        assertNotNull(FeatureRegistries.commands().handler("catalog_probe"));
        assertTrue(FeatureRegistries.commands().allMeta().stream().anyMatch(meta -> meta.name().equals("catalog_probe")));
        assertFalse(FeatureRegistries.commands().suggest("catalog_pr").isEmpty());
        assertNotNull(node);
        assertNotSame(node, FeatureRegistries.scripting().nodes().create("walk"));
        assertFalse(FeatureRegistries.scripting().nodes().canonical().isEmpty());
        assertNotNull(FeatureRegistries.scripting().sensors().definition("sensor_health_below"));
        assertFalse(FeatureRegistries.scripting().sensors().definitions().isEmpty());
        assertTrue(FeatureRegistries.scripting().conditions().evaluate("=", null, "same", "same"));
        assertEquals(EbslTokenKind.COMMAND, FeatureRegistries.scripting().syntax().classify("walk", true));
        assertNotNull(FeatureRegistries.scripting().syntax().style(EbslTokenKind.COMMAND));
    }

    private static final class RegistryProbeEvent {
    }
}
