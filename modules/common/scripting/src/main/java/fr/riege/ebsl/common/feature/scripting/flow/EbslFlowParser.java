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

package fr.riege.ebsl.common.feature.scripting.flow;

import fr.riege.ebsl.common.feature.scripting.manager.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

final class EbslFlowParser {
    private static final String INPUT_KEYWORD = "input";
    private static final String OUTPUT_KEYWORD = "output";

    private final List<Token> tokens;
    private int cursor;

    EbslFlowParser(String source) {
        this.tokens = new EbslFlowTokenizer(source).tokens();
    }

    EbslGraphDocument parse() {
        Map<String, EbslGraphNode> nodes = new LinkedHashMap<>();
        List<EbslGraphConnection> connections = new ArrayList<>();
        while (!peek(TokenType.EOF)) {
            if (match("workflow")) {
                parseWorkflowBody(nodes, connections);
            } else if (match("node")) {
                addNode(nodes, parseNode());
            } else if (match("connect")) {
                connections.add(parseConnection());
            } else {
                throw error("Expected workflow, node, or connect");
            }
        }
        return new EbslGraphDocument(Map.of(), connections, nodes);
    }

    private void parseWorkflowBody(Map<String, EbslGraphNode> nodes, List<EbslGraphConnection> connections) {
        requireIdentifier("workflow name");
        requireSymbol("{");
        while (!match("}")) {
            if (match("node")) {
                addNode(nodes, parseNode());
            } else if (match("connect")) {
                connections.add(parseConnection());
            } else {
                throw error("Expected node, connect, or workflow end");
            }
        }
    }

    private EbslGraphNode parseNode() {
        String id = requireIdentifier("node id");
        String type = requireIdentifier("node type");
        NodeParts parts = parseNodeBody();
        return new EbslGraphNode(id, type, parts.fields(), parts.inputs(), parts.outputs());
    }

    private NodeParts parseNodeBody() {
        Map<String, String> fields = new LinkedHashMap<>();
        List<EbslGraphPort> inputs = new ArrayList<>();
        List<EbslGraphPort> outputs = new ArrayList<>();
        if (!match("{")) {
            return NodeParts.defaults(fields);
        }
        while (!match("}")) {
            if (isFieldAssignment()) {
                parseField(fields);
            } else if (match(INPUT_KEYWORD) || match("in")) {
                inputs.add(parsePort(EbslGraphPortDirection.INPUT));
            } else if (match(OUTPUT_KEYWORD) || match("out")) {
                outputs.add(parsePort(EbslGraphPortDirection.OUTPUT));
            } else {
                parseField(fields);
            }
        }
        return new NodeParts(fields, inputs, outputs);
    }

    private void parseField(Map<String, String> fields) {
        String field = requireIdentifier("field name");
        requireSymbol("=");
        fields.put(field, requireValue());
        optionalSymbol(";");
    }

    private EbslGraphPort parsePort(EbslGraphPortDirection direction) {
        String id = requireIdentifier("port id");
        boolean multiple = direction == EbslGraphPortDirection.OUTPUT || match("multiple");
        optionalSymbol(";");
        return new EbslGraphPort(id, label(id), direction, EbslGraphPortKind.FLOW, multiple);
    }

    private EbslGraphConnection parseConnection() {
        Endpoint from = parseEndpoint();
        requireSymbol("->");
        Endpoint to = parseEndpoint();
        optionalSymbol(";");
        return new EbslGraphConnection("", from.node(), from.port(), to.node(), to.port(), EbslGraphConnectionMode.FLOW, "");
    }

    private Endpoint parseEndpoint() {
        String node = requireIdentifier("connection node");
        String port = EbslGraphConnection.DEFAULT_FLOW_PORT;
        if (optionalSymbol(".")) {
            port = requireIdentifier("connection port");
        }
        return new Endpoint(node, port);
    }

    private void addNode(Map<String, EbslGraphNode> nodes, EbslGraphNode node) {
        if (nodes.putIfAbsent(node.id(), node) != null) {
            throw error("Duplicate node id: " + node.id());
        }
    }

    private boolean match(String value) {
        if (peek(value)) {
            cursor++;
            return true;
        }
        return false;
    }

    private boolean optionalSymbol(String value) {
        return match(value);
    }

    private boolean peek(String value) {
        return current().value().equals(value);
    }

    private boolean peek(TokenType type) {
        return current().type() == type;
    }

    private boolean isFieldAssignment() {
        return current().type() == TokenType.IDENTIFIER
            && cursor + 1 < tokens.size()
            && "=".equals(tokens.get(cursor + 1).value());
    }

    private void requireSymbol(String value) {
        if (!match(value)) {
            throw error("Expected '" + value + "'");
        }
    }

    private String requireIdentifier(String label) {
        Token token = current();
        if (token.type() != TokenType.IDENTIFIER) {
            throw error("Expected " + label);
        }
        cursor++;
        return token.value();
    }

    private String requireValue() {
        Token token = current();
        if (token.type() != TokenType.IDENTIFIER && token.type() != TokenType.STRING) {
            throw error("Expected field value");
        }
        cursor++;
        return token.value();
    }

    private Token current() {
        return tokens.get(cursor);
    }

    private IllegalArgumentException error(String message) {
        Token token = current();
        return new IllegalArgumentException(message + " at line " + token.line());
    }

    private static String label(String id) {
        return id.substring(0, 1).toUpperCase() + id.substring(1).replace('_', ' ');
    }

    private record NodeParts(Map<String, String> fields, List<EbslGraphPort> inputs, List<EbslGraphPort> outputs) {
        private static NodeParts defaults(Map<String, String> fields) {
            return new NodeParts(fields, List.of(), List.of());
        }
    }

    private record Endpoint(String node, String port) {
    }
}
