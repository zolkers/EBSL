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

package fr.riege.ebsl.common.feature.scripting.manager;

import fr.riege.ebsl.common.platform.layer.IStorageLayer;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class EbslGraphModelTest {
    @Test
    void persistsGraphFirstNodesWithFieldsAndNamedPorts() {
        MemoryStorage storage = new MemoryStorage();
        EbslScriptManager manager = new EbslScriptManager(storage);
        EbslGraphNode switchNode = EbslGraphNode.switchNode(
            "switch-1",
            Map.of("expression", "$health < 10"),
            List.of(
                EbslGraphPort.output("true", "True"),
                EbslGraphPort.output("false", "False")));
        EbslGraphNode healNode = EbslGraphNode.action("heal-1", "use_item", Map.of("item", "minecraft:golden_apple"));
        EbslGraphConnection connection = new EbslGraphConnection(
            "edge-true",
            "switch-1",
            "true",
            "heal-1",
            "main",
            EbslGraphConnectionMode.FLOW,
            "low health");
        EbslGraphDocument document = new EbslGraphDocument(
            Map.of("switch-1", new EbslGraphNodePosition(10.0f, 12.0f)),
            List.of(connection),
            Map.of(switchNode.id(), switchNode, healNode.id(), healNode));

        manager.saveGraphDocument("main.ebsl", document);
        EbslGraphDocument loaded = manager.loadGraphDocument("main.ebsl");

        assertEquals(document.nodes(), loaded.nodes());
        assertEquals(document.connections(), loaded.connections());
        assertEquals(document.positions(), loaded.positions());
    }

    @Test
    void validatesMultiOutputSwitchAndRejectsDuplicateSingleInput() {
        EbslGraphNode switchNode = EbslGraphNode.switchNode(
            "switch-1",
            Map.of(),
            List.of(EbslGraphPort.output("left", "Left"), EbslGraphPort.output("right", "Right")));
        EbslGraphNode target = EbslGraphNode.action("target-1", "message", Map.of("text", "done"));
        EbslGraphDocument duplicateInput = new EbslGraphDocument(
            Map.of(),
            List.of(
                new EbslGraphConnection("a", "switch-1", "left", "target-1", "main", EbslGraphConnectionMode.FLOW, ""),
                new EbslGraphConnection("b", "switch-1", "right", "target-1", "main", EbslGraphConnectionMode.FLOW, "")),
            Map.of(switchNode.id(), switchNode, target.id(), target));

        List<EbslGraphValidationIssue> issues = EbslGraphValidator.validate(duplicateInput);

        assertEquals(1, issues.size());
        assertTrue(issues.getFirst().message().contains("only one connection"));
    }

    @Test
    void buildsGraphFirstExecutionPlanWithPortAwareBranches() {
        EbslGraphNode start = new EbslGraphNode(
            "start-1",
            "start",
            Map.of(),
            List.of(EbslGraphPort.input("trigger", "Trigger")),
            List.of(EbslGraphPort.output("main", "Out")));
        EbslGraphNode switchNode = EbslGraphNode.switchNode(
            "switch-1",
            Map.of("expression", "$mode"),
            List.of(EbslGraphPort.output("mine", "Mine"), EbslGraphPort.output("fight", "Fight")));
        EbslGraphNode mine = EbslGraphNode.action("mine-1", "break_block", Map.of("block", "stone"));
        EbslGraphNode fight = EbslGraphNode.action("fight-1", "attack", Map.of());
        EbslGraphDocument document = new EbslGraphDocument(
            Map.of(),
            List.of(
                new EbslGraphConnection("start-switch", "start-1", "main", "switch-1", "main", EbslGraphConnectionMode.FLOW, ""),
                new EbslGraphConnection("switch-mine", "switch-1", "mine", "mine-1", "main", EbslGraphConnectionMode.FLOW, ""),
                new EbslGraphConnection("switch-fight", "switch-1", "fight", "fight-1", "main", EbslGraphConnectionMode.FLOW, "")),
            Map.of(start.id(), start, switchNode.id(), switchNode, mine.id(), mine, fight.id(), fight));

        EbslGraphExecutionPlan plan = EbslGraphExecutionPlanner.plan(document);

        assertTrue(plan.executable());
        assertEquals(List.of("start-1"), plan.roots());
        assertEquals(List.of("switch-mine"), plan.next("switch-1", "mine").stream().map(EbslGraphConnection::id).toList());
        assertEquals(List.of("switch-fight"), plan.next("switch-1", "fight").stream().map(EbslGraphConnection::id).toList());
    }

    @Test
    void reportsCyclesBeforeRuntimeExecution() {
        EbslGraphNode first = EbslGraphNode.action("first", "message", Map.of());
        EbslGraphNode second = EbslGraphNode.action("second", "message", Map.of());
        EbslGraphDocument document = new EbslGraphDocument(
            Map.of(),
            List.of(
                new EbslGraphConnection("a", "first", "main", "second", "main", EbslGraphConnectionMode.FLOW, ""),
                new EbslGraphConnection("b", "second", "main", "first", "main", EbslGraphConnectionMode.FLOW, "")),
            Map.of(first.id(), first, second.id(), second));

        EbslGraphExecutionPlan plan = EbslGraphExecutionPlanner.plan(document);

        assertEquals(false, plan.executable());
        assertTrue(plan.issues().stream().anyMatch(issue -> issue.message().contains("cycle")));
    }

    private static final class MemoryStorage implements IStorageLayer {
        private final Map<String, String> text = new HashMap<>();

        @Override public void saveJson(String key, String json) {
            text.put(key, json);
        }

        @Override public Optional<String> loadJson(String key) {
            return Optional.ofNullable(text.get(key));
        }

        @Override public void saveText(String path, String text) {
            this.text.put(path, text);
        }

        @Override public Optional<String> loadText(String path) {
            return Optional.ofNullable(text.get(path));
        }
    }
}
