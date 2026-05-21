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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class EbslGraphValidator {
    private EbslGraphValidator() {
    }

    public static List<EbslGraphValidationIssue> validate(EbslGraphDocument document) {
        if (document.nodes().isEmpty()) {
            return List.of();
        }
        List<EbslGraphValidationIssue> issues = new ArrayList<>();
        Map<String, Integer> inputUseCounts = new HashMap<>();
        for (EbslGraphConnection connection : document.connections()) {
            validateConnection(document, connection, inputUseCounts, issues);
        }
        return List.copyOf(issues);
    }

    private static void validateConnection(EbslGraphDocument document,
                                           EbslGraphConnection connection,
                                           Map<String, Integer> inputUseCounts,
                                           List<EbslGraphValidationIssue> issues) {
        EbslGraphNode from = document.nodes().get(connection.fromKey());
        EbslGraphNode to = document.nodes().get(connection.toKey());
        if (from == null || to == null) {
            add(issues, connection.id(), "Connection references a missing node.");
            return;
        }
        EbslGraphPort output = from.output(connection.fromPort());
        EbslGraphPort input = to.input(connection.toPort());
        if (output == null || input == null) {
            add(issues, connection.id(), "Connection references a missing port.");
            return;
        }
        if (output.kind() != input.kind()) {
            add(issues, connection.id(), "Connection mixes incompatible port kinds.");
        }
        String inputKey = connection.toKey() + ":" + connection.toPort();
        int uses = inputUseCounts.merge(inputKey, 1, Integer::sum);
        if (uses > 1 && !input.multiple()) {
            add(issues, connection.id(), "Input port accepts only one connection.");
        }
    }

    private static void add(List<EbslGraphValidationIssue> issues, String elementId, String message) {
        issues.add(new EbslGraphValidationIssue(EbslGraphValidationSeverity.ERROR, elementId, message));
    }
}
