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

package fr.riege.ebsl.common.feature.scripting.builder;

import fr.riege.ebsl.common.feature.scripting.manager.EbslGraphConnection;
import fr.riege.ebsl.common.feature.scripting.manager.EbslGraphDocument;
import fr.riege.ebsl.common.feature.scripting.manager.EbslGraphNode;
import fr.riege.ebsl.common.feature.scripting.manager.EbslGraphPort;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class EbslGraphScriptBuilder {
    private final Map<String, EbslGraphNode> nodes = new LinkedHashMap<>();
    private final ListBuilder<EbslGraphConnection> connections = new ListBuilder<>();

    private EbslGraphScriptBuilder() {
    }

    public static EbslGraphScriptBuilder graph() {
        return new EbslGraphScriptBuilder();
    }

    public NodeBuilder node(String id, String type) {
        return new NodeBuilder(this, id, type);
    }

    public EbslGraphScriptBuilder connect(String from, String to) {
        connections.add(new EbslGraphConnection(from, to));
        return this;
    }

    public EbslGraphScriptBuilder connect(String from, String fromPort, String to, String toPort) {
        connections.add(new EbslGraphConnection(from, to).withPorts(fromPort, toPort));
        return this;
    }

    public EbslGraphDocument build() {
        return new EbslGraphDocument(Map.of(), connections.values(), nodes);
    }

    private EbslGraphScriptBuilder add(EbslGraphNode node) {
        if (nodes.putIfAbsent(node.id(), node) != null) {
            throw new IllegalArgumentException("Duplicate graph node id: " + node.id());
        }
        return this;
    }

    public static final class NodeBuilder {
        private final EbslGraphScriptBuilder graph;
        private final String id;
        private final String type;
        private final Map<String, String> fields = new LinkedHashMap<>();
        private List<EbslGraphPort> inputs = List.of();
        private List<EbslGraphPort> outputs = List.of();

        private NodeBuilder(EbslGraphScriptBuilder graph, String id, String type) {
            this.graph = Objects.requireNonNull(graph, "graph");
            this.id = id;
            this.type = type;
        }

        public NodeBuilder field(String key, Object value) {
            fields.put(key, Objects.toString(value, ""));
            return this;
        }

        public NodeBuilder inputs(EbslGraphPort... ports) {
            inputs = ports == null ? List.of() : List.of(ports);
            return this;
        }

        public NodeBuilder outputs(EbslGraphPort... ports) {
            outputs = ports == null ? List.of() : List.of(ports);
            return this;
        }

        public EbslGraphScriptBuilder add() {
            return graph.add(new EbslGraphNode(id, type, fields, inputs, outputs));
        }
    }

    private static final class ListBuilder<T> {
        private final ArrayList<T> values = new ArrayList<>();

        void add(T value) {
            values.add(value);
        }

        List<T> values() {
            return List.copyOf(values);
        }
    }
}
