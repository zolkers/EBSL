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

import fr.riege.ebsl.common.feature.scripting.manager.EbslGraphConnection;
import fr.riege.ebsl.common.feature.scripting.manager.EbslGraphNode;
import fr.riege.ebsl.common.feature.scripting.manager.EbslGraphPort;
import fr.riege.ebsl.common.feature.scripting.manager.EbslGraphPortDirection;
import fr.riege.ebsl.common.feature.scripting.manager.EbslGraphDocument;

import java.util.Map;

final class EbslFlowWriter {
    private EbslFlowWriter() {
    }

    static String write(EbslGraphDocument document) {
        StringBuilder out = new StringBuilder("workflow main {\n");
        for (EbslGraphNode node : document.nodes().values()) {
            writeNode(out, node);
        }
        for (EbslGraphConnection connection : document.connections()) {
            out.append("  connect ")
                .append(endpoint(connection.fromKey(), connection.fromPort()))
                .append(" -> ")
                .append(endpoint(connection.toKey(), connection.toPort()))
                .append(";\n");
        }
        return out.append("}\n").toString();
    }

    private static void writeNode(StringBuilder out, EbslGraphNode node) {
        out.append("  node ").append(node.id()).append(' ').append(node.type()).append(" {\n");
        for (Map.Entry<String, String> field : node.fields().entrySet()) {
            out.append("    ").append(field.getKey()).append(" = ").append(quote(field.getValue())).append(";\n");
        }
        for (EbslGraphPort input : node.inputs()) {
            writePort(out, "input", input);
        }
        for (EbslGraphPort output : node.outputs()) {
            writePort(out, "output", output);
        }
        out.append("  }\n");
    }

    private static void writePort(StringBuilder out, String keyword, EbslGraphPort port) {
        if (isImplicitMain(keyword, port)) {
            return;
        }
        out.append("    ").append(keyword).append(' ').append(port.id()).append(";\n");
    }

    private static boolean isImplicitMain(String keyword, EbslGraphPort port) {
        return EbslGraphConnection.DEFAULT_FLOW_PORT.equals(port.id())
            && (("input".equals(keyword) && port.direction() == EbslGraphPortDirection.INPUT)
            || ("output".equals(keyword) && port.direction() == EbslGraphPortDirection.OUTPUT));
    }

    private static String endpoint(String node, String port) {
        return EbslGraphConnection.DEFAULT_FLOW_PORT.equals(port) ? node : node + "." + port;
    }

    private static String quote(String value) {
        return "\"" + value.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
    }
}
