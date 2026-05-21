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

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.function.UnaryOperator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class EbslGraphModelTest {
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

    @Test
    void graphDocumentConveniencesReturnImmutableCopies() {
        EbslGraphNode node = EbslGraphNode.action("message-1", "message", Map.of("text", "hello"));
        EbslGraphConnection connection = new EbslGraphConnection("message-1", "message-2");

        EbslGraphDocument document = EbslGraphDocument.empty()
            .withNode(node)
            .withConnection(connection);

        assertEquals(Map.of(node.id(), node), document.nodes());
        assertEquals(List.of(connection), document.connections());
        assertThrows(UnsupportedOperationException.class, () -> document.connections().add(connection));
    }

    @Test
    void graphConnectionHelpersPreservePortAwareIdentity() {
        EbslGraphConnection connection = new EbslGraphConnection("source", "target")
            .withPorts("success", "trigger")
            .withMode(EbslGraphConnectionMode.EACH_INPUT)
            .withLabel("retry");
        UnaryOperator<String> remap = key -> "copy-" + key;

        EbslGraphConnection remapped = connection.remap(remap);

        assertEquals("success", remapped.fromPort());
        assertEquals("trigger", remapped.toPort());
        assertEquals(EbslGraphConnectionMode.EACH_INPUT, remapped.mode());
        assertEquals("retry", remapped.label());
        assertEquals("copy-source:success->copy-target:trigger", remapped.id());
    }

    @Test
    void graphNodeAndPortEnumsValidateInvalidDefinitions() {
        EbslGraphNode node = EbslGraphNode.action("message-1", "message", Map.of("text", "hello"));

        assertTrue(node.hasInput("main"));
        assertTrue(node.hasOutput("main"));
        assertEquals(null, node.input("missing"));
        assertEquals(null, node.output("missing"));
        assertThrows(IllegalArgumentException.class, () -> EbslGraphPortDirection.byId("sideways"));
        assertThrows(IllegalArgumentException.class, () -> EbslGraphPortKind.byId("signal"));
        assertEquals(EbslGraphConnectionMode.FLOW, EbslGraphConnectionMode.byId("teleport"));
    }

}
