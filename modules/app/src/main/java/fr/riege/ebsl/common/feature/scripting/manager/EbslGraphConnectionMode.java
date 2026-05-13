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

import java.util.Locale;

public enum EbslGraphConnectionMode {
    FLOW("flow"),
    EACH_INPUT("each_input");

    private final String id;

    EbslGraphConnectionMode(String id) {
        this.id = id;
    }

    public String id() {
        return id;
    }

    public static EbslGraphConnectionMode byId(String id) {
        String normalized = id == null ? "" : id.trim().toLowerCase(Locale.ROOT).replace('-', '_');
        for (EbslGraphConnectionMode mode : values()) {
            if (mode.id.equals(normalized) || mode.name().toLowerCase(Locale.ROOT).equals(normalized)) {
                return mode;
            }
        }
        return FLOW;
    }
}
