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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public record EbslGraphDocument(
    Map<String, EbslGraphNodePosition> positions,
    List<EbslGraphConnection> connections,
    Map<String, EbslGraphNode> nodes
) {
    public EbslGraphDocument(Map<String, EbslGraphNodePosition> positions,
                             List<EbslGraphConnection> connections) {
        this(positions, connections, Map.of());
    }

    public EbslGraphDocument {
        positions = Map.copyOf(positions == null ? Map.of() : positions);
        connections = List.copyOf(connections == null ? List.of() : connections);
        nodes = Map.copyOf(nodes == null ? Map.of() : nodes);
    }

    public static EbslGraphDocument empty() {
        return new EbslGraphDocument(Map.of(), List.of(), Map.of());
    }

    public EbslGraphDocument withNode(EbslGraphNode node) {
        Map<String, EbslGraphNode> updated = new LinkedHashMap<>(nodes);
        updated.put(node.id(), node);
        return new EbslGraphDocument(positions, connections, updated);
    }

    public EbslGraphDocument withConnection(EbslGraphConnection connection) {
        return new EbslGraphDocument(positions, append(connections, connection), nodes);
    }

    private static List<EbslGraphConnection> append(List<EbslGraphConnection> values, EbslGraphConnection value) {
        ArrayList<EbslGraphConnection> updated = new ArrayList<>(values);
        updated.add(value);
        return List.copyOf(updated);
    }
}
