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

import fr.riege.ebsl.common.feature.scripting.manager.EbslGraphNode;
import fr.riege.ebsl.common.feature.scripting.manager.EbslGraphPort;
import fr.riege.ebsl.common.feature.scripting.manager.EbslGraphPortDirection;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public record EbslNodeDefinition(
    String type,
    String label,
    String description,
    EbslNodeGroup group,
    List<EbslNodeFieldDefinition> fields,
    List<EbslNodePortDefinition> inputs,
    List<EbslNodePortDefinition> outputs,
    Map<String, String> metadata
) {
    public EbslNodeDefinition {
        type = requireText(type, "type");
        label = label == null || label.isBlank() ? type : label.trim();
        description = description == null ? "" : description.trim();
        group = group == null ? EbslNodeGroup.ACTION : group;
        fields = List.copyOf(fields == null ? List.of() : fields);
        inputs = normalizePorts(inputs, EbslGraphPortDirection.INPUT);
        outputs = normalizePorts(outputs, EbslGraphPortDirection.OUTPUT);
        metadata = Map.copyOf(metadata == null ? Map.of() : metadata);
    }

    public static EbslNodeDefinition action(String type, String label, List<EbslNodeFieldDefinition> fields) {
        return new EbslNodeDefinition(
            type,
            label,
            "",
            EbslNodeGroup.ACTION,
            fields,
            List.of(EbslNodePortDefinition.flowInput("main", "In")),
            List.of(EbslNodePortDefinition.flowOutput("main", "Out")),
            Map.of());
    }

    public EbslGraphNode createNode(String id, Map<String, String> fieldValues) {
        Map<String, String> values = new LinkedHashMap<>();
        for (EbslNodeFieldDefinition field : fields) {
            if (!field.defaultValue().isBlank()) {
                values.put(field.id(), field.defaultValue());
            }
        }
        if (fieldValues != null) {
            values.putAll(fieldValues);
        }
        List<EbslGraphPort> graphInputs = inputs.stream().map(EbslNodePortDefinition::toGraphPort).toList();
        List<EbslGraphPort> graphOutputs = outputs.stream().map(EbslNodePortDefinition::toGraphPort).toList();
        return new EbslGraphNode(id, type, values, graphInputs, graphOutputs);
    }

    public EbslNodeFieldDefinition field(String id) {
        return fields.stream().filter(field -> field.id().equals(id)).findFirst().orElse(null);
    }

    public EbslNodePortDefinition input(String id) {
        return inputs.stream().filter(port -> port.id().equals(id)).findFirst().orElse(null);
    }

    public EbslNodePortDefinition output(String id) {
        return outputs.stream().filter(port -> port.id().equals(id)).findFirst().orElse(null);
    }

    public EbslNodeDefinition withDescription(String nextDescription) {
        return new EbslNodeDefinition(type, label, nextDescription, group, fields, inputs, outputs, metadata);
    }

    public EbslNodeDefinition withGroup(EbslNodeGroup nextGroup) {
        return new EbslNodeDefinition(type, label, description, nextGroup, fields, inputs, outputs, metadata);
    }

    public EbslNodeDefinition withPorts(List<EbslNodePortDefinition> nextInputs, List<EbslNodePortDefinition> nextOutputs) {
        return new EbslNodeDefinition(type, label, description, group, fields, nextInputs, nextOutputs, metadata);
    }

    public EbslNodeDefinition withMetadata(Map<String, String> nextMetadata) {
        return new EbslNodeDefinition(type, label, description, group, fields, inputs, outputs, nextMetadata);
    }

    private static List<EbslNodePortDefinition> normalizePorts(List<EbslNodePortDefinition> ports,
                                                               EbslGraphPortDirection direction) {
        List<EbslNodePortDefinition> defaults = direction == EbslGraphPortDirection.INPUT
            ? List.of(EbslNodePortDefinition.flowInput("main", "In"))
            : List.of(EbslNodePortDefinition.flowOutput("main", "Out"));
        List<EbslNodePortDefinition> normalized = ports == null || ports.isEmpty() ? defaults : ports;
        for (EbslNodePortDefinition port : normalized) {
            if (port.direction() != direction) {
                throw new IllegalArgumentException("Port " + port.id() + " must be " + direction.id());
            }
        }
        return List.copyOf(normalized);
    }

    private static String requireText(String value, String name) {
        String checked = Objects.requireNonNull(value, name).trim();
        if (checked.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return checked;
    }
}
