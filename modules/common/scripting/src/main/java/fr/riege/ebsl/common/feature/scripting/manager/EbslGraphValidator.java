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

import fr.riege.ebsl.common.feature.scripting.definition.EbslNodeDefinition;
import fr.riege.ebsl.common.feature.scripting.definition.EbslNodeDefinitionRegistry;
import fr.riege.ebsl.common.feature.scripting.definition.EbslNodeFieldDefinition;
import fr.riege.ebsl.common.feature.scripting.definition.EbslNodePortDefinition;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class EbslGraphValidator {
    private EbslGraphValidator() {
    }

    public static List<EbslGraphValidationIssue> validate(EbslGraphDocument document) {
        return validate(document, null);
    }

    public static List<EbslGraphValidationIssue> validate(EbslGraphDocument document,
                                                          EbslNodeDefinitionRegistry registry) {
        if (document.nodes().isEmpty()) {
            return List.of();
        }
        List<EbslGraphValidationIssue> issues = new ArrayList<>();
        if (registry != null) {
            validateNodes(document, registry, issues);
        }
        Map<String, Integer> inputUseCounts = new HashMap<>();
        for (EbslGraphConnection connection : document.connections()) {
            validateConnection(document, registry, connection, inputUseCounts, issues);
        }
        return List.copyOf(issues);
    }

    private static void validateNodes(EbslGraphDocument document,
                                      EbslNodeDefinitionRegistry registry,
                                      List<EbslGraphValidationIssue> issues) {
        for (EbslGraphNode node : document.nodes().values()) {
            EbslNodeDefinition definition = registry.definition(node.type());
            if (definition == null) {
                add(issues, node.id(), "Node type '" + node.type() + "' is not registered.");
                continue;
            }
            for (String fieldId : node.fields().keySet()) {
                EbslNodeFieldDefinition field = definition.field(fieldId);
                if (field == null) {
                    add(issues, node.id(), "Node field '" + fieldId + "' is not registered for type '" + node.type() + "'.");
                }
            }
            for (EbslNodeFieldDefinition field : definition.fields()) {
                if (field.required() && node.fields().getOrDefault(field.id(), "").isBlank()) {
                    add(issues, node.id(), "Required field '" + field.id() + "' is missing.");
                }
            }
        }
    }

    private static void validateConnection(EbslGraphDocument document,
                                           EbslNodeDefinitionRegistry registry,
                                           EbslGraphConnection connection,
                                           Map<String, Integer> inputUseCounts,
                                           List<EbslGraphValidationIssue> issues) {
        EbslGraphNode from = document.nodes().get(connection.fromKey());
        EbslGraphNode to = document.nodes().get(connection.toKey());
        if (from == null || to == null) {
            add(issues, connection.id(), "Connection references a missing node.");
            return;
        }
        EbslGraphPort output = from.output(connection.fromPort());
        EbslGraphPort input = to.input(connection.toPort());
        if (output == null || input == null) {
            add(issues, connection.id(), "Connection references a missing port.");
            return;
        }
        if (output.kind() != input.kind()) {
            add(issues, connection.id(), "Connection mixes incompatible port kinds.");
        }
        if (registry != null) {
            validateTypedPorts(registry, connection, from, to, issues);
        }
        String inputKey = connection.toKey() + ":" + connection.toPort();
        int uses = inputUseCounts.merge(inputKey, 1, Integer::sum);
        if (uses > 1 && !input.multiple()) {
            add(issues, connection.id(), "Input port accepts only one connection.");
        }
    }

    private static void validateTypedPorts(EbslNodeDefinitionRegistry registry,
                                           EbslGraphConnection connection,
                                           EbslGraphNode from,
                                           EbslGraphNode to,
                                           List<EbslGraphValidationIssue> issues) {
        EbslNodeDefinition fromDefinition = registry.definition(from.type());
        EbslNodeDefinition toDefinition = registry.definition(to.type());
        if (fromDefinition == null || toDefinition == null) {
            return;
        }
        EbslNodePortDefinition output = fromDefinition.output(connection.fromPort());
        EbslNodePortDefinition input = toDefinition.input(connection.toPort());
        if (output == null || input == null) {
            add(issues, connection.id(), "Connection references a port missing from the node registry.");
            return;
        }
        if (!input.valueType().accepts(output.valueType())) {
            add(issues, connection.id(),
                "Data port type mismatch: " + output.valueType().id() + " cannot feed " + input.valueType().id() + ".");
        }
    }

    private static void add(List<EbslGraphValidationIssue> issues, String elementId, String message) {
        issues.add(new EbslGraphValidationIssue(EbslGraphValidationSeverity.ERROR, elementId, message));
    }
}
