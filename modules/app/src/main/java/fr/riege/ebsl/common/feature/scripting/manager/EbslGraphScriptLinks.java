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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class EbslGraphScriptLinks {
    private static final String HEADER = "# Graph links";
    private static final String DIRECTIVE = "# @link ";
    private static final Pattern LINK_PATTERN = Pattern.compile(
        "^#\\s*@link\\s+(\\d+)\\s*->\\s*(\\d+)(?:\\s+mode=([a-zA-Z_-]+))?(?:\\s+label=\"((?:\\\\.|[^\"])*)\")?\\s*$"
    );

    private EbslGraphScriptLinks() {
    }

    static List<EbslGraphConnection> parse(String fileName, String source) {
        String normalized = EbslScriptManager.normalizeFileName(fileName);
        List<EbslGraphConnection> connections = new ArrayList<>();
        for (String line : (source == null ? "" : source).split("\\R")) {
            Matcher matcher = LINK_PATTERN.matcher(line.trim());
            if (!matcher.matches()) {
                continue;
            }
            String from = normalized + ":" + matcher.group(1);
            String to = normalized + ":" + matcher.group(2);
            if (from.equals(to)) {
                continue;
            }
            EbslGraphConnectionMode mode = EbslGraphConnectionMode.byId(matcher.group(3));
            String label = unescape(matcher.group(4));
            connections.add(new EbslGraphConnection(from, to, mode, label));
        }
        return connections;
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
            if (trimmed.equals(HEADER) || trimmed.startsWith(DIRECTIVE)) {
                continue;
            }
            kept.add(line);
        }
        return String.join("\n", kept).stripTrailing();
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
}
