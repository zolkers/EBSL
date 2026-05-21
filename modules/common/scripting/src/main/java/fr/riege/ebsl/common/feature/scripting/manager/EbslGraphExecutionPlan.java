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

public record EbslGraphExecutionPlan(
    List<String> roots,
    List<String> orderedNodes,
    Map<String, List<EbslGraphConnection>> outgoingByNode,
    Map<String, List<EbslGraphConnection>> outgoingByPort,
    List<EbslGraphValidationIssue> issues
) {
    public EbslGraphExecutionPlan {
        roots = List.copyOf(roots);
        orderedNodes = List.copyOf(orderedNodes);
        outgoingByNode = Map.copyOf(outgoingByNode);
        outgoingByPort = Map.copyOf(outgoingByPort);
        issues = List.copyOf(issues);
    }

    public boolean executable() {
        return issues.stream().noneMatch(issue -> issue.severity() == EbslGraphValidationSeverity.ERROR);
    }

    public List<EbslGraphConnection> next(String nodeId) {
        return outgoingByNode.getOrDefault(nodeId, List.of());
    }

    public List<EbslGraphConnection> next(String nodeId, String outputPort) {
        return outgoingByPort.getOrDefault(portKey(nodeId, outputPort), List.of());
    }

    static String portKey(String nodeId, String portId) {
        String normalizedPort = portId == null || portId.isBlank() ? EbslGraphConnection.DEFAULT_FLOW_PORT : portId.trim();
        return nodeId + ":" + normalizedPort;
    }
}
