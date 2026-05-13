/*
 * This file is part of EBSL.
 *
 * EBSL is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * EBSL is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package fr.riege.ebsl.common.feature.scripting.manager;

import java.util.*;
import java.util.function.IntFunction;

public final class EbslGraphExecutionPlanner {
    private EbslGraphExecutionPlanner() {
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
}
