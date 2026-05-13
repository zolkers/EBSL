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

package fr.riege.ebsl.common.core.log;

import java.util.Locale;

public enum AppLogLevel {
    ERROR,
    FATAL,
    WARN,
    INFO,
    DEBUG,
    TRACE,
    OTHER;

    public static AppLogLevel fromName(String name) {
        if (name == null || name.isBlank()) {
            return OTHER;
        }
        try {
            return valueOf(name.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ignored) {
            return OTHER;
        }
    }

    public String label() {
        return this == OTHER ? "LOG" : name();
    }
}
