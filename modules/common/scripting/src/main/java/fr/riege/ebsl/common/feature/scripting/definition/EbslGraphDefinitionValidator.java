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
import fr.riege.ebsl.common.feature.scripting.manager.EbslGraphPortKind;
import fr.riege.ebsl.common.feature.scripting.manager.EbslGraphValidationIssue;
import fr.riege.ebsl.common.feature.scripting.manager.EbslGraphValidationSeverity;

import java.util.ArrayList;
import java.util.List;

public final class EbslGraphDefinitionValidator {
    private EbslGraphDefinitionValidator() {
    }

    public static List<EbslGraphValidationIssue> validate(EbslGraphDocument document,
                                                          EbslNodeDefinitionRegistry registry) {
        List<EbslGraphValidationIssue> issues = new ArrayList<>();
        if (document == null || registry == null) {
            return issues;
        }
        for (EbslGraphNode node : document.nodes().values()) {
            validateNode(registry, node, issues);
        }
        for (EbslGraphConnection connection : document.connections()) {
            validateConnection(document, registry, connection, issues);
        }
        return List.copyOf(issues);
    }

    private static void validateNode(EbslNodeDefinitionRegistry registry,
                                     EbslGraphNode node,
                                     List<EbslGraphValidationIssue> issues) {
        EbslNodeDefinition definition = registry.definition(node.type());
        if (definition == null) {
            issues.add(new EbslGraphValidationIssue(
                EbslGraphValidationSeverity.ERROR,
                node.id(),
                "Unknown node type: " + node.type()));
            return;
        }
        for (EbslNodeFieldDefinition field : definition.fields()) {
            String value = node.fields().get(field.id());
            if (field.required() && (value == null || value.isBlank())) {
                issues.add(new EbslGraphValidationIssue(
                    EbslGraphValidationSeverity.ERROR,
                    node.id(),
                    "Missing required field: " + field.id()));
            }
        }
        for (String field : node.fields().keySet()) {
            if (definition.field(field) == null) {
                issues.add(new EbslGraphValidationIssue(
                    EbslGraphValidationSeverity.WARNING,
                    node.id(),
                    "Unknown field: " + field));
            }
        }
    }

    private static void validateConnection(EbslGraphDocument document,
                                           EbslNodeDefinitionRegistry registry,
                                           EbslGraphConnection connection,
                                           List<EbslGraphValidationIssue> issues) {
        EbslGraphNode fromNode = document.nodes().get(connection.fromKey());
        EbslGraphNode toNode = document.nodes().get(connection.toKey());
        if (fromNode == null || toNode == null) {
            return;
        }
        EbslNodeDefinition fromDefinition = registry.definition(fromNode.type());
        EbslNodeDefinition toDefinition = registry.definition(toNode.type());
        if (fromDefinition == null || toDefinition == null) {
            return;
        }
        EbslNodePortDefinition output = fromDefinition.output(connection.fromPort());
        EbslNodePortDefinition input = toDefinition.input(connection.toPort());
        if (output == null || input == null) {
            return;
        }
        if (output.kind() != input.kind()) {
            issues.add(portIssue(connection, "Port kinds do not match"));
            return;
        }
        if (output.kind() == EbslGraphPortKind.DATA && !input.valueType().accepts(output.valueType())) {
            issues.add(portIssue(connection, "Data port types do not match"));
        }
    }

    private static EbslGraphValidationIssue portIssue(EbslGraphConnection connection, String message) {
        return new EbslGraphValidationIssue(
            EbslGraphValidationSeverity.ERROR,
            connection.id(),
            message + ": " + connection.fromKey() + "." + connection.fromPort()
                + " -> " + connection.toKey() + "." + connection.toPort());
    }
}
