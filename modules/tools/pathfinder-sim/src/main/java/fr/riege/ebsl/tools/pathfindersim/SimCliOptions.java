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

package fr.riege.ebsl.tools.pathfindersim;

import java.nio.file.Path;
import java.util.Locale;

public record SimCliOptions(
    String scenarioFilter,
    int maxTicks,
    int stuckWindowTicks,
    double stuckEpsilon,
    Path jsonOutput
) {
    private static final int DEFAULT_MAX_TICKS = 600;
    private static final int DEFAULT_STUCK_WINDOW = 25;
    private static final double DEFAULT_STUCK_EPSILON = 0.015;

    public static SimCliOptions parse(String[] args) {
        String scenario = "all";
        int maxTicks = DEFAULT_MAX_TICKS;
        int stuckWindow = DEFAULT_STUCK_WINDOW;
        double stuckEpsilon = DEFAULT_STUCK_EPSILON;
        Path json = null;
        for (String arg : args == null ? new String[0] : args) {
            if (arg.startsWith("--scenario=")) {
                scenario = value(arg);
            } else if (arg.startsWith("--max-ticks=")) {
                maxTicks = parsePositiveInt(value(arg), DEFAULT_MAX_TICKS);
            } else if (arg.startsWith("--stuck-window=")) {
                stuckWindow = parsePositiveInt(value(arg), DEFAULT_STUCK_WINDOW);
            } else if (arg.startsWith("--stuck-epsilon=")) {
                stuckEpsilon = parsePositiveDouble(value(arg), DEFAULT_STUCK_EPSILON);
            } else if (arg.startsWith("--json=")) {
                json = Path.of(value(arg));
            }
        }
        return new SimCliOptions(
            scenario.toLowerCase(Locale.ROOT),
            maxTicks,
            stuckWindow,
            stuckEpsilon,
            json);
    }

    boolean accepts(SimulationScenario scenario) {
        return "all".equals(scenarioFilter)
            || scenario.id().toLowerCase(Locale.ROOT).contains(scenarioFilter);
    }

    private static String value(String arg) {
        int equals = arg.indexOf('=');
        return equals < 0 ? "" : arg.substring(equals + 1).trim();
    }

    private static int parsePositiveInt(String value, int fallback) {
        try {
            return Math.max(1, Integer.parseInt(value));
        } catch (NumberFormatException ignored) {
            return fallback;
        }
    }

    private static double parsePositiveDouble(String value, double fallback) {
        try {
            return Math.max(0.0, Double.parseDouble(value));
        } catch (NumberFormatException ignored) {
            return fallback;
        }
    }
}
