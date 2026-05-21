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

import java.util.List;
import java.util.Map;
import java.util.Objects;

public record EbslGraphNode(
    String id,
    String type,
    Map<String, String> fields,
    List<EbslGraphPort> inputs,
    List<EbslGraphPort> outputs
) {
    public EbslGraphNode {
        id = requireText(id, "id");
        type = requireText(type, "type");
        fields = Map.copyOf(fields == null ? Map.of() : fields);
        inputs = List.copyOf(inputs == null || inputs.isEmpty() ? List.of(EbslGraphPort.input("main", "In")) : inputs);
        outputs = List.copyOf(outputs == null || outputs.isEmpty() ? List.of(EbslGraphPort.output("main", "Out")) : outputs);
    }

    public static EbslGraphNode action(String id, String type, Map<String, String> fields) {
        return new EbslGraphNode(id, type, fields, List.of(EbslGraphPort.input("main", "In")), List.of(EbslGraphPort.output("main", "Out")));
    }

    public static EbslGraphNode switchNode(String id, Map<String, String> fields, List<EbslGraphPort> outputs) {
        return new EbslGraphNode(id, "switch", fields, List.of(EbslGraphPort.input("main", "In")), outputs);
    }

    public boolean hasInput(String portId) {
        return inputs.stream().anyMatch(port -> port.id().equals(portId));
    }

    public boolean hasOutput(String portId) {
        return outputs.stream().anyMatch(port -> port.id().equals(portId));
    }

    public EbslGraphPort input(String portId) {
        return inputs.stream().filter(port -> port.id().equals(portId)).findFirst().orElse(null);
    }

    public EbslGraphPort output(String portId) {
        return outputs.stream().filter(port -> port.id().equals(portId)).findFirst().orElse(null);
    }

    private static String requireText(String value, String name) {
        String checked = Objects.requireNonNull(value, name).trim();
        if (checked.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return checked;
    }
}
