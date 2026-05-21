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

package fr.riege.ebsl.common.feature.scripting.definition;

import fr.riege.ebsl.common.feature.scripting.manager.EbslGraphConnection;
import fr.riege.ebsl.common.feature.scripting.manager.EbslGraphDocument;
import fr.riege.ebsl.common.feature.scripting.manager.EbslGraphNode;
import fr.riege.ebsl.common.feature.scripting.manager.EbslGraphPortDirection;
import fr.riege.ebsl.common.feature.scripting.manager.EbslGraphPortKind;
import fr.riege.ebsl.common.feature.scripting.manager.EbslGraphValidationIssue;
import fr.riege.ebsl.common.feature.scripting.manager.EbslGraphValidationSeverity;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class EbslNodeDefinitionRegistryTest {
    @Test
    void createsGraphNodesFromRichDefinitions() {
        EbslNodeDefinition message = EbslNodeDefinition.action(
            "message",
            "Message",
            List.of(EbslNodeFieldDefinition.required("text", EbslValueType.STRING).withLabel("Text")));

        EbslGraphNode node = message.createNode("message-1", Map.of("text", "hello"));

        assertEquals("message", node.type());
        assertEquals("hello", node.fields().get("text"));
        assertEquals("main", node.inputs().getFirst().id());
        assertEquals("main", node.outputs().getFirst().id());
    }

    @Test
    void groupsDefinitionsForEditorPalettes() {
        EbslNodeDefinition trigger = EbslNodeDefinition.action("start", "Start", List.of())
            .withGroup(EbslNodeGroup.TRIGGER);
        EbslNodeDefinition action = EbslNodeDefinition.action("message", "Message", List.of());
        EbslNodeDefinitionRegistry registry = EbslNodeDefinitionRegistry.of(action, trigger);

        assertEquals(List.of(trigger), registry.byGroup(EbslNodeGroup.TRIGGER));
        assertEquals(List.of(action, trigger), registry.all());
    }

    @Test
    void rejectsDuplicateNodeTypes() {
        EbslNodeDefinition first = EbslNodeDefinition.action("message", "Message", List.of());
        EbslNodeDefinition second = EbslNodeDefinition.action("message", "Message copy", List.of());

        assertThrows(IllegalArgumentException.class, () -> EbslNodeDefinitionRegistry.of(first, second));
    }

    @Test
    void validatesRequiredFieldsUnknownFieldsAndTypedDataPorts() {
        EbslNodeDefinition sensor = new EbslNodeDefinition(
            "health",
            "Health",
            "",
            EbslNodeGroup.SENSOR,
            List.of(),
            List.of(EbslNodePortDefinition.flowInput("main", "In")),
            List.of(EbslNodePortDefinition.output("value", EbslValueType.NUMBER)),
            Map.of());
        EbslNodeDefinition message = new EbslNodeDefinition(
            "message",
            "Message",
            "",
            EbslNodeGroup.ACTION,
            List.of(EbslNodeFieldDefinition.required("text", EbslValueType.STRING)),
            List.of(
                EbslNodePortDefinition.flowInput("main", "In"),
                new EbslNodePortDefinition("text", "Text", EbslGraphPortDirection.INPUT, EbslGraphPortKind.DATA, EbslValueType.STRING, false)),
            List.of(EbslNodePortDefinition.flowOutput("main", "Out")),
            Map.of());
        EbslNodeDefinitionRegistry registry = EbslNodeDefinitionRegistry.of(sensor, message);
        EbslGraphNode healthNode = sensor.createNode("health-1", Map.of());
        EbslGraphNode messageNode = message.createNode("message-1", Map.of("extra", "unused"));
        EbslGraphDocument document = new EbslGraphDocument(
            Map.of(),
            List.of(new EbslGraphConnection("", "health-1", "value", "message-1", "text", null, "")),
            Map.of(healthNode.id(), healthNode, messageNode.id(), messageNode));

        List<EbslGraphValidationIssue> issues = EbslGraphDefinitionValidator.validate(document, registry);

        assertTrue(issues.stream().anyMatch(issue -> issue.severity() == EbslGraphValidationSeverity.ERROR
            && issue.message().contains("Missing required field")));
        assertTrue(issues.stream().anyMatch(issue -> issue.severity() == EbslGraphValidationSeverity.WARNING
            && issue.message().contains("Unknown field")));
        assertTrue(issues.stream().anyMatch(issue -> issue.severity() == EbslGraphValidationSeverity.ERROR
            && issue.message().contains("Data port types do not match")));
    }
}
