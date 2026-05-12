package fr.riege.ebsl.common.feature.scripting.manager;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
        Map<String, List<String>> outgoing = new HashMap<>();
        Map<String, Integer> incomingCounts = new HashMap<>();
        for (String key : lines.keySet()) {
            outgoing.put(key, new ArrayList<>());
            incomingCounts.put(key, 0);
        }
        boolean usedGraph = false;
        for (EbslGraphConnection connection : connections) {
            if (connection.mode() != EbslGraphConnectionMode.FLOW
                || !lines.containsKey(connection.fromKey())
                || !lines.containsKey(connection.toKey())) {
                continue;
            }
            if (outgoing.get(connection.fromKey()).contains(connection.toKey())) {
                continue;
            }
            outgoing.get(connection.fromKey()).add(connection.toKey());
            incomingCounts.compute(connection.toKey(), (ignored, count) -> count == null ? 1 : count + 1);
            usedGraph = true;
        }
        if (!usedGraph) {
            return GraphSort.unused();
        }

        Comparator<String> bySourceLine = Comparator.comparingInt(key -> lines.get(key).lineNumber());
        for (List<String> targets : outgoing.values()) {
            targets.sort(bySourceLine);
        }
        ArrayDeque<String> queue = new ArrayDeque<>();
        for (String key : lines.keySet()) {
            if (incomingCounts.getOrDefault(key, 0) == 0 && !outgoing.get(key).isEmpty()) {
                queue.add(key);
            }
        }
        List<GraphLine> ordered = new ArrayList<>();
        Set<String> visited = new HashSet<>();
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

        if (ordered.isEmpty()) {
            return GraphSort.unused();
        }
        return new GraphSort(true, ordered, visited);
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
                String label = connection.label().isBlank() ? "" : " " + connection.label();
                expanded.add(new GraphLine(
                    target.lineNumber(),
                    target.key() + "#" + connection.id(),
                    "# graph edge " + connection.mode().id() + label
                ));
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
}
