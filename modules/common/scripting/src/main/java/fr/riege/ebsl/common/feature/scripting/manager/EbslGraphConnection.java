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

import java.util.function.UnaryOperator;

public record EbslGraphConnection(
    String id,
    String fromKey,
    String fromPort,
    String toKey,
    String toPort,
    EbslGraphConnectionMode mode,
    String label
) {
    public static final String DEFAULT_FLOW_PORT = "main";

    public EbslGraphConnection(String fromKey, String toKey) {
        this(defaultId(fromKey, DEFAULT_FLOW_PORT, toKey, DEFAULT_FLOW_PORT),
            fromKey,
            DEFAULT_FLOW_PORT,
            toKey,
            DEFAULT_FLOW_PORT,
            EbslGraphConnectionMode.FLOW,
            "");
    }

    public EbslGraphConnection(String fromKey, String toKey, EbslGraphConnectionMode mode, String label) {
        this(defaultId(fromKey, DEFAULT_FLOW_PORT, toKey, DEFAULT_FLOW_PORT),
            fromKey,
            DEFAULT_FLOW_PORT,
            toKey,
            DEFAULT_FLOW_PORT,
            mode,
            label);
    }

    public EbslGraphConnection(String id, String fromKey, String toKey, EbslGraphConnectionMode mode, String label) {
        this(id, fromKey, DEFAULT_FLOW_PORT, toKey, DEFAULT_FLOW_PORT, mode, label);
    }

    public EbslGraphConnection {
        fromKey = fromKey == null ? "" : fromKey;
        fromPort = normalizePort(fromPort);
        toKey = toKey == null ? "" : toKey;
        toPort = normalizePort(toPort);
        id = id == null || id.isBlank() ? defaultId(fromKey, fromPort, toKey, toPort) : id;
        mode = mode == null ? EbslGraphConnectionMode.FLOW : mode;
        label = label == null ? "" : label.trim();
    }

    public boolean touches(String key) {
        return fromKey.equals(key) || toKey.equals(key);
    }

    public EbslGraphConnection withMode(EbslGraphConnectionMode mode) {
        return new EbslGraphConnection(id, fromKey, fromPort, toKey, toPort, mode, label);
    }

    public EbslGraphConnection withLabel(String label) {
        return new EbslGraphConnection(id, fromKey, fromPort, toKey, toPort, mode, label);
    }

    public EbslGraphConnection withPorts(String fromPort, String toPort) {
        return new EbslGraphConnection("", fromKey, fromPort, toKey, toPort, mode, label);
    }

    public EbslGraphConnection remap(UnaryOperator<String> mapper) {
        String mappedFrom = mapper.apply(fromKey);
        String mappedTo = mapper.apply(toKey);
        return new EbslGraphConnection(defaultId(mappedFrom, fromPort, mappedTo, toPort),
            mappedFrom,
            fromPort,
            mappedTo,
            toPort,
            mode,
            label);
    }

    private static String defaultId(String fromKey, String fromPort, String toKey, String toPort) {
        return (fromKey == null ? "" : fromKey) + ":" + normalizePort(fromPort)
            + "->" + (toKey == null ? "" : toKey) + ":" + normalizePort(toPort);
    }

    private static String normalizePort(String port) {
        return port == null || port.isBlank() ? DEFAULT_FLOW_PORT : port.trim();
    }
}
