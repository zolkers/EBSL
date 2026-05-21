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

import java.util.*;
import java.util.function.IntFunction;

public final class EbslGraphExecutionPlanner {
    private EbslGraphExecutionPlanner() {
    }

    public static EbslGraphExecutionPlan plan(EbslGraphDocument document) {
        if (document.nodes().isEmpty()) {
            return new EbslGraphExecutionPlan(List.of(), List.of(), Map.of(), Map.of(), List.of());
        }
        List<EbslGraphValidationIssue> issues = new ArrayList<>(EbslGraphValidator.validate(document));
        GraphPlanEdges edges = buildGraphPlanEdges(document);
        List<String> ordered = topologicalOrder(document, edges.incomingCounts(), edges.outgoingByNode());
        if (ordered.size() != document.nodes().size()) {
            issues.add(new EbslGraphValidationIssue(EbslGraphValidationSeverity.ERROR, "", "Graph contains a flow cycle."));
        }
        return new EbslGraphExecutionPlan(
            roots(document, edges.incomingCounts()),
            ordered,
            edges.outgoingByNode(),
            edges.outgoingByPort(),
            issues);
    }

    public static String plan(String fileName, String source, EbslGraphDocument document) {
        if (source == null || source.isBlank() || document.connections().isEmpty() || hasBlockSyntax(source)) {
            return source == null ? "" : source;
        }
        List<GraphLine> lines = graphLines(source, line -> fileName + ":" + line);
        if (lines.isEmpty()) {
            return source;
        }
        Map<String, GraphLine> lineByKey = new LinkedHashMap<>();
        for (GraphLine line : lines) {
            lineByKey.put(line.key(), line);
        }

        GraphSort sort = sort(lineByKey, document.connections());
        if (!sort.usedGraph() && !hasEachInputEdges(lineByKey, document.connections())) {
            return source;
        }
        List<GraphLine> ordered = new ArrayList<>(sort.usedGraph() ? sort.ordered() : List.of());
        for (GraphLine line : lines) {
            if (!sort.visited().contains(line.key())) {
                ordered.add(line);
            }
        }
        return joinLines(materializeEachInputEdges(ordered, lineByKey, document.connections()));
    }

    private static GraphPlanEdges buildGraphPlanEdges(EbslGraphDocument document) {
        Map<String, List<EbslGraphConnection>> outgoingByNode = new LinkedHashMap<>();
        Map<String, List<EbslGraphConnection>> outgoingByPort = new LinkedHashMap<>();
        Map<String, Integer> incomingCounts = new LinkedHashMap<>();
        for (String nodeId : document.nodes().keySet()) {
            outgoingByNode.put(nodeId, new ArrayList<>());
            incomingCounts.put(nodeId, 0);
        }
        for (EbslGraphConnection connection : document.connections()) {
            if (!document.nodes().containsKey(connection.fromKey()) || !document.nodes().containsKey(connection.toKey())) {
                continue;
            }
            outgoingByNode.computeIfAbsent(connection.fromKey(), ignored -> new ArrayList<>()).add(connection);
            outgoingByPort.computeIfAbsent(
                EbslGraphExecutionPlan.portKey(connection.fromKey(), connection.fromPort()),
                ignored -> new ArrayList<>()).add(connection);
            incomingCounts.compute(connection.toKey(), (ignored, count) -> count == null ? 1 : count + 1);
        }
        return new GraphPlanEdges(copyLists(outgoingByNode), copyLists(outgoingByPort), incomingCounts);
    }

    private static List<String> roots(EbslGraphDocument document, Map<String, Integer> incomingCounts) {
        return document.nodes().keySet().stream()
            .filter(nodeId -> incomingCounts.getOrDefault(nodeId, 0) == 0)
            .toList();
    }

    private static List<String> topologicalOrder(EbslGraphDocument document,
                                                 Map<String, Integer> incomingCounts,
                                                 Map<String, List<EbslGraphConnection>> outgoingByNode) {
        Map<String, Integer> remainingInputs = new LinkedHashMap<>(incomingCounts);
        ArrayDeque<String> queue = new ArrayDeque<>(roots(document, remainingInputs));
        List<String> ordered = new ArrayList<>();
        while (!queue.isEmpty()) {
            String nodeId = queue.removeFirst();
            ordered.add(nodeId);
            for (EbslGraphConnection connection : outgoingByNode.getOrDefault(nodeId, List.of())) {
                int count = remainingInputs.compute(
                    connection.toKey(),
                    (ignored, value) -> Math.max(0, (value == null ? 0 : value) - 1));
                if (count == 0) {
                    queue.add(connection.toKey());
                }
            }
        }
        return ordered;
    }

    private static Map<String, List<EbslGraphConnection>> copyLists(Map<String, List<EbslGraphConnection>> values) {
        Map<String, List<EbslGraphConnection>> copy = new LinkedHashMap<>();
        for (Map.Entry<String, List<EbslGraphConnection>> entry : values.entrySet()) {
            copy.put(entry.getKey(), List.copyOf(entry.getValue()));
        }
        return copy;
    }

    private static boolean hasEachInputEdges(Map<String, GraphLine> lines, List<EbslGraphConnection> connections) {
        return connections.stream().anyMatch(connection -> connection.mode() == EbslGraphConnectionMode.EACH_INPUT
            && lines.containsKey(connection.fromKey())
            && lines.containsKey(connection.toKey()));
    }

    private static GraphSort sort(Map<String, GraphLine> lines, List<EbslGraphConnection> connections) {
        GraphEdges edges = buildFlowEdges(lines, connections);
        if (!edges.usedGraph()) {
            return GraphSort.unused();
        }

        sortOutgoingEdges(lines, edges.outgoing());
        ArrayDeque<String> queue = initialSortQueue(lines, edges.outgoing(), edges.incomingCounts());
        List<GraphLine> ordered = new ArrayList<>();
        Set<String> visited = new HashSet<>();
        drainSortQueue(lines, edges.outgoing(), edges.incomingCounts(), queue, ordered, visited);

        if (ordered.isEmpty()) {
            return GraphSort.unused();
        }
        return new GraphSort(true, ordered, visited);
    }

    private static GraphEdges buildFlowEdges(Map<String, GraphLine> lines, List<EbslGraphConnection> connections) {
        Map<String, List<String>> outgoing = new HashMap<>();
        Map<String, Integer> incomingCounts = new HashMap<>();
        for (String key : lines.keySet()) {
            outgoing.put(key, new ArrayList<>());
            incomingCounts.put(key, 0);
        }
        boolean usedGraph = false;
        for (EbslGraphConnection connection : connections) {
            if (isUsableFlowEdge(lines, connection) && !outgoing.get(connection.fromKey()).contains(connection.toKey())) {
                outgoing.get(connection.fromKey()).add(connection.toKey());
                incomingCounts.compute(connection.toKey(), (ignored, count) -> count == null ? 1 : count + 1);
                usedGraph = true;
            }
        }
        return new GraphEdges(outgoing, incomingCounts, usedGraph);
    }

    private static boolean isUsableFlowEdge(Map<String, GraphLine> lines, EbslGraphConnection connection) {
        return connection.mode() == EbslGraphConnectionMode.FLOW
            && lines.containsKey(connection.fromKey())
            && lines.containsKey(connection.toKey());
    }

    private static void sortOutgoingEdges(Map<String, GraphLine> lines, Map<String, List<String>> outgoing) {
        Comparator<String> bySourceLine = Comparator.comparingInt(key -> lines.get(key).lineNumber());
        for (List<String> targets : outgoing.values()) {
            targets.sort(bySourceLine);
        }
    }

    private static ArrayDeque<String> initialSortQueue(Map<String, GraphLine> lines,
                                                       Map<String, List<String>> outgoing,
                                                       Map<String, Integer> incomingCounts) {
        ArrayDeque<String> queue = new ArrayDeque<>();
        for (String key : lines.keySet()) {
            if (incomingCounts.getOrDefault(key, 0) == 0 && !outgoing.get(key).isEmpty()) {
                queue.add(key);
            }
        }
        return queue;
    }

    private static void drainSortQueue(Map<String, GraphLine> lines,
                                       Map<String, List<String>> outgoing,
                                       Map<String, Integer> incomingCounts,
                                       ArrayDeque<String> queue,
                                       List<GraphLine> ordered,
                                       Set<String> visited) {
        while (!queue.isEmpty()) {
            String key = queue.removeFirst();
            if (!visited.add(key)) {
                continue;
            }
            ordered.add(lines.get(key));
            for (String target : outgoing.get(key)) {
                int count = incomingCounts.compute(target, (ignored, value) -> Math.max(0, (value == null ? 0 : value) - 1));
                if (count == 0) {
                    queue.add(target);
                }
            }
        }
    }

    private static List<GraphLine> materializeEachInputEdges(List<GraphLine> ordered,
                                                             Map<String, GraphLine> lines,
                                                             List<EbslGraphConnection> connections) {
        Map<String, List<EbslGraphConnection>> eachInputByFrom = new HashMap<>();
        for (EbslGraphConnection connection : connections) {
            if (connection.mode() == EbslGraphConnectionMode.EACH_INPUT
                && lines.containsKey(connection.fromKey())
                && lines.containsKey(connection.toKey())) {
                eachInputByFrom.computeIfAbsent(connection.fromKey(), ignored -> new ArrayList<>()).add(connection);
            }
        }
        if (eachInputByFrom.isEmpty()) {
            return ordered;
        }
        List<GraphLine> expanded = new ArrayList<>();
        for (GraphLine line : ordered) {
            expanded.add(line);
            for (EbslGraphConnection connection : eachInputByFrom.getOrDefault(line.key(), List.of())) {
                GraphLine target = lines.get(connection.toKey());
                expanded.add(target);
            }
        }
        return expanded;
    }

    private static List<GraphLine> graphLines(String source, IntFunction<String> keyFactory) {
        List<GraphLine> lines = new ArrayList<>();
        String[] rawLines = source.split("\\R");
        for (int i = 0; i < rawLines.length; i++) {
            String line = rawLines[i] == null ? "" : rawLines[i].trim();
            if (line.isBlank() || line.startsWith("#")) {
                continue;
            }
            int lineNumber = i + 1;
            lines.add(new GraphLine(lineNumber, keyFactory.apply(lineNumber), line));
        }
        return lines;
    }

    private static boolean hasBlockSyntax(String source) {
        return source.indexOf('{') >= 0 || source.indexOf('}') >= 0;
    }

    private static String joinLines(List<GraphLine> lines) {
        List<String> values = lines.stream().map(GraphLine::line).toList();
        return String.join("\n", values) + "\n";
    }

    private record GraphLine(int lineNumber, String key, String line) {
    }

    private record GraphSort(boolean usedGraph, List<GraphLine> ordered, Set<String> visited) {
        private static GraphSort unused() {
            return new GraphSort(false, List.of(), Set.of());
        }
    }

    private record GraphEdges(
        Map<String, List<String>> outgoing,
        Map<String, Integer> incomingCounts,
        boolean usedGraph
    ) {
    }

    private record GraphPlanEdges(
        Map<String, List<EbslGraphConnection>> outgoingByNode,
        Map<String, List<EbslGraphConnection>> outgoingByPort,
        Map<String, Integer> incomingCounts
    ) {
    }
}
