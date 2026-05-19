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

package fr.riege.ebsl.tools.pathfindersim.cli;

import fr.riege.ebsl.tools.pathfindersim.scenario.SimulationScenario;
import fr.riege.ebsl.tools.pathfindersim.world.minecraft.MinecraftWorldImportOptions;
import fr.riege.ebsl.tools.pathfindersim.world.minecraft.MinecraftStressGrid;

import java.nio.file.Path;
import java.util.Locale;

public record SimCliOptions(
    String scenarioFilter,
    int maxTicks,
    int stuckWindowTicks,
    double stuckEpsilon,
    Path jsonOutput,
    boolean ui,
    MinecraftWorldImportOptions minecraftWorldImportOptions,
    MinecraftStressGrid minecraftStressGrid
) {
    private static final int DEFAULT_MAX_TICKS = 600;
    private static final int DEFAULT_STUCK_WINDOW = 25;
    private static final double DEFAULT_STUCK_EPSILON = 0.015;

    public static SimCliOptions parse(String[] args) {
        ParseState state = new ParseState();
        for (String arg : args == null ? new String[0] : args) {
            state.apply(arg);
        }
        return new SimCliOptions(
            state.scenario.toLowerCase(Locale.ROOT),
            state.maxTicks,
            state.stuckWindow,
            state.stuckEpsilon,
            state.json,
            state.ui,
            state.minecraftWorld.buildOrNull(),
            state.minecraftStressGrid);
    }

    public boolean accepts(SimulationScenario scenario) {
        return "all".equals(scenarioFilter)
            || scenario.id().toLowerCase(Locale.ROOT).contains(scenarioFilter);
    }

    private static final class ParseState {
        private String scenario = "all";
        private int maxTicks = DEFAULT_MAX_TICKS;
        private int stuckWindow = DEFAULT_STUCK_WINDOW;
        private double stuckEpsilon = DEFAULT_STUCK_EPSILON;
        private Path json;
        private boolean ui;
        private MinecraftStressGrid minecraftStressGrid;
        private final MinecraftWorldImportOptions.Builder minecraftWorld = MinecraftWorldImportOptions.builder();

        void apply(String arg) {
            if (applyCore(arg)) {
                return;
            }
            applyMinecraft(arg);
        }

        private boolean applyCore(String arg) {
            if (arg.startsWith("--scenario=")) {
                scenario = value(arg);
                return true;
            }
            if (arg.startsWith("--max-ticks=")) {
                maxTicks = parsePositiveInt(value(arg), DEFAULT_MAX_TICKS);
                return true;
            }
            if (arg.startsWith("--stuck-window=")) {
                stuckWindow = parsePositiveInt(value(arg), DEFAULT_STUCK_WINDOW);
                return true;
            }
            if (arg.startsWith("--stuck-epsilon=")) {
                stuckEpsilon = parsePositiveDouble(value(arg), DEFAULT_STUCK_EPSILON);
                return true;
            }
            if (arg.startsWith("--json=")) {
                json = Path.of(value(arg));
                return true;
            }
            if ("--ui".equals(arg)) {
                ui = true;
                return true;
            }
            return false;
        }

        private void applyMinecraft(String arg) {
            if (arg.startsWith("--mc-world=")) {
                minecraftWorld.worldDirectory(Path.of(value(arg)));
            } else if (arg.startsWith("--mc-start=")) {
                minecraftWorld.start(parseVec(value(arg), 0.5, 64.0, 0.5));
            } else if (arg.startsWith("--mc-goal=")) {
                minecraftWorld.goal(parseBlock(value(arg), 0, 64, 0));
            } else if (arg.startsWith("--mc-radius=")) {
                minecraftWorld.radiusChunks(parsePositiveInt(value(arg), MinecraftWorldImportOptions.DEFAULT_RADIUS_CHUNKS));
            } else if (arg.startsWith("--mc-goal-search=")) {
                minecraftWorld.goalSearchBlocks(parsePositiveInt(
                    value(arg),
                    MinecraftWorldImportOptions.DEFAULT_GOAL_SEARCH_BLOCKS));
            } else if (arg.startsWith("--mc-stress-grid=")) {
                minecraftStressGrid = parseStressGrid(value(arg));
            } else if ("--mc-diagnostics".equals(arg)) {
                minecraftWorld.diagnostics(true);
            }
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

        private static double[] parseVec(String value, double fallbackX, double fallbackY, double fallbackZ) {
            String[] parts = value.split(",");
            if (parts.length != 3) {
                return new double[] { fallbackX, fallbackY, fallbackZ };
            }
            try {
                return new double[] {
                    Double.parseDouble(parts[0].trim()),
                    Double.parseDouble(parts[1].trim()),
                    Double.parseDouble(parts[2].trim())
                };
            } catch (NumberFormatException ignored) {
                return new double[] { fallbackX, fallbackY, fallbackZ };
            }
        }

        private static int[] parseBlock(String value, int fallbackX, int fallbackY, int fallbackZ) {
            String[] parts = value.split(",");
            if (parts.length != 3) {
                return new int[] { fallbackX, fallbackY, fallbackZ };
            }
            try {
                return new int[] {
                    Integer.parseInt(parts[0].trim()),
                    Integer.parseInt(parts[1].trim()),
                    Integer.parseInt(parts[2].trim())
                };
            } catch (NumberFormatException ignored) {
                return new int[] { fallbackX, fallbackY, fallbackZ };
            }
        }

        private static MinecraftStressGrid parseStressGrid(String value) {
            int[] parts = parseGridParts(value);
            return new MinecraftStressGrid(parts[0], parts[1], parts[2], parts[3]);
        }

        private static int[] parseGridParts(String value) {
            String[] parts = value.split(",");
            int[] parsed = { 2, 1, 2, 1 };
            int count = Math.min(parts.length, parsed.length);
            for (int i = 0; i < count; i++) {
                parsed[i] = parsePositiveInt(parts[i].trim(), parsed[i]);
            }
            parsed[0] = Math.max(0, parsed[0]);
            parsed[1] = Math.max(0, parsed[1]);
            parsed[2] = Math.max(0, parsed[2]);
            return parsed;
        }
    }
}
