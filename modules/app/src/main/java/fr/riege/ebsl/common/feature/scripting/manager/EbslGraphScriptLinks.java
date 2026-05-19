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
import java.util.List;

final class EbslGraphScriptLinks {
    private static final String HEADER = "# Graph links";
    private static final String DIRECTIVE = "# @link ";

    private EbslGraphScriptLinks() {
    }

    static List<EbslGraphConnection> parse(String fileName, String source) {
        String normalized = EbslScriptManager.normalizeFileName(fileName);
        List<EbslGraphConnection> connections = new ArrayList<>();
        for (String line : (source == null ? "" : source).split("\\R")) {
            LinkDirective directive = parseDirective(line);
            if (directive != null) {
                addConnection(connections, normalized, directive);
            }
        }
        return connections;
    }

    private static void addConnection(List<EbslGraphConnection> connections,
                                      String normalizedFileName,
                                      LinkDirective directive) {
        String from = normalizedFileName + ":" + directive.fromLine();
        String to = normalizedFileName + ":" + directive.toLine();
        if (!from.equals(to)) {
            connections.add(new EbslGraphConnection(
                from,
                to,
                EbslGraphConnectionMode.byId(directive.mode()),
                unescape(directive.label())));
        }
    }

    static String sync(String fileName, String source, List<EbslGraphConnection> connections) {
        String withoutDirectives = removeDirectives(source == null ? "" : source);
        if (connections == null || connections.isEmpty()) {
            return withoutDirectives;
        }
        StringBuilder builder = new StringBuilder(withoutDirectives.stripTrailing());
        if (!builder.isEmpty()) {
            builder.append('\n').append('\n');
        }
        builder.append(HEADER).append('\n');
        for (EbslGraphConnection connection : connections) {
            int fromLine = lineNumber(fileName, connection.fromKey());
            int toLine = lineNumber(fileName, connection.toKey());
            if (fromLine < 1 || toLine < 1 || fromLine == toLine) {
                continue;
            }
            builder.append(DIRECTIVE)
                .append(fromLine)
                .append(" -> ")
                .append(toLine)
                .append(" mode=")
                .append(connection.mode().id());
            if (!connection.label().isBlank()) {
                builder.append(" label=\"").append(escape(connection.label())).append('"');
            }
            builder.append('\n');
        }
        return builder.toString().stripTrailing();
    }

    private static String removeDirectives(String source) {
        List<String> kept = new ArrayList<>();
        for (String line : source.split("\\R", -1)) {
            String trimmed = line.trim();
            if (!trimmed.equals(HEADER) && !trimmed.startsWith(DIRECTIVE)) {
                kept.add(line);
            }
        }
        return String.join("\n", kept).stripTrailing();
    }

    private static LinkDirective parseDirective(String line) {
        String trimmed = line == null ? "" : line.trim();
        if (!trimmed.startsWith(DIRECTIVE)) {
            return null;
        }
        String rest = trimmed.substring(DIRECTIVE.length()).trim();
        int arrow = rest.indexOf("->");
        if (arrow < 0) {
            return null;
        }
        Integer from = parseLineNumber(rest.substring(0, arrow));
        String tail = rest.substring(arrow + 2).trim();
        Integer to = parseLineNumber(firstToken(tail));
        if (from == null || to == null) {
            return null;
        }
        return new LinkDirective(from, to, parseMode(tail), parseLabel(tail));
    }

    private static String firstToken(String value) {
        int space = value.indexOf(' ');
        return space < 0 ? value : value.substring(0, space);
    }

    private static String parseMode(String tail) {
        String marker = " mode=";
        int start = tail.indexOf(marker);
        if (start < 0) {
            return null;
        }
        String value = tail.substring(start + marker.length()).trim();
        int end = value.indexOf(' ');
        return end < 0 ? value : value.substring(0, end);
    }

    private static String parseLabel(String tail) {
        String marker = " label=\"";
        int start = tail.indexOf(marker);
        if (start < 0) {
            return null;
        }
        int valueStart = start + marker.length();
        StringBuilder label = new StringBuilder();
        boolean escaped = false;
        for (int i = valueStart; i < tail.length(); i++) {
            char c = tail.charAt(i);
            if (escaped) {
                label.append('\\').append(c);
                escaped = false;
            } else if (c == '\\') {
                escaped = true;
            } else if (c == '"') {
                return label.toString();
            } else {
                label.append(c);
            }
        }
        return null;
    }

    private static Integer parseLineNumber(String value) {
        try {
            int parsed = Integer.parseInt(value.trim());
            return parsed < 1 ? null : parsed;
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private static int lineNumber(String fileName, String key) {
        String prefix = EbslScriptManager.normalizeFileName(fileName) + ":";
        if (!key.startsWith(prefix)) {
            return -1;
        }
        try {
            return Integer.parseInt(key.substring(prefix.length()));
        } catch (NumberFormatException exception) {
            return -1;
        }
    }

    private static String escape(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private static String unescape(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        StringBuilder builder = new StringBuilder(value.length());
        boolean escaped = false;
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (escaped) {
                builder.append(c);
                escaped = false;
            } else if (c == '\\') {
                escaped = true;
            } else {
                builder.append(c);
            }
        }
        if (escaped) {
            builder.append('\\');
        }
        return builder.toString().trim();
    }

    private record LinkDirective(int fromLine, int toLine, String mode, String label) {
    }
}
