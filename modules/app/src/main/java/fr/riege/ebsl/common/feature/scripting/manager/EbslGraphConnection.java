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

public record EbslGraphConnection(String id, String fromKey, String toKey, EbslGraphConnectionMode mode, String label) {
    public EbslGraphConnection(String fromKey, String toKey) {
        this(defaultId(fromKey, toKey), fromKey, toKey, EbslGraphConnectionMode.FLOW, "");
    }

    public EbslGraphConnection(String fromKey, String toKey, EbslGraphConnectionMode mode, String label) {
        this(defaultId(fromKey, toKey), fromKey, toKey, mode, label);
    }

    public EbslGraphConnection {
        id = id == null || id.isBlank() ? defaultId(fromKey, toKey) : id;
        fromKey = fromKey == null ? "" : fromKey;
        toKey = toKey == null ? "" : toKey;
        mode = mode == null ? EbslGraphConnectionMode.FLOW : mode;
        label = label == null ? "" : label.trim();
    }

    public boolean touches(String key) {
        return fromKey.equals(key) || toKey.equals(key);
    }

    public EbslGraphConnection withMode(EbslGraphConnectionMode mode) {
        return new EbslGraphConnection(id, fromKey, toKey, mode, label);
    }

    public EbslGraphConnection withLabel(String label) {
        return new EbslGraphConnection(id, fromKey, toKey, mode, label);
    }

    public EbslGraphConnection remap(UnaryOperator<String> mapper) {
        String mappedFrom = mapper.apply(fromKey);
        String mappedTo = mapper.apply(toKey);
        return new EbslGraphConnection(defaultId(mappedFrom, mappedTo), mappedFrom, mappedTo, mode, label);
    }

    private static String defaultId(String fromKey, String toKey) {
        return (fromKey == null ? "" : fromKey) + "->" + (toKey == null ? "" : toKey);
    }
}
