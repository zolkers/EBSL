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

import fr.riege.ebsl.common.math.Vec3d;
import fr.riege.ebsl.tools.pathfindersim.replay.ReplayPaths;
import fr.riege.ebsl.tools.pathfindersim.scenario.SimulationScenario;
import fr.riege.ebsl.tools.pathfindersim.world.minecraft.MinecraftRouteSpec;
import fr.riege.ebsl.tools.pathfindersim.world.minecraft.MinecraftStressGrid;
import fr.riege.ebsl.tools.pathfindersim.world.minecraft.MinecraftWorldImportOptions;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public record SimCliOptions(
    String scenarioFilter,
    int maxTicks,
    int stuckWindowTicks,
    double stuckEpsilon,
    Path jsonOutput,
    Path replayDirectory,
    boolean replaySaveEnabled,
    boolean headless,
    int repeatRuns,
    boolean failOnRegression,
    double regressionMaxFinalDistance,
    int regressionMaxStuckEvents,
    int regressionMaxRecoveryAttempts,
    int regressionMaxBackwardTicks,
    double regressionMaxAverageLateralError,
    MinecraftWorldImportOptions minecraftWorldImportOptions,
    List<MinecraftRouteSpec> minecraftRoutes,
    MinecraftStressGrid minecraftStressGrid
) {
    public SimCliOptions {
        minecraftRoutes = minecraftRoutes == null ? List.of() : List.copyOf(minecraftRoutes);
    }

    private static final int DEFAULT_MAX_TICKS = 600;
    private static final int DEFAULT_STUCK_WINDOW = 25;
    private static final double DEFAULT_STUCK_EPSILON = 0.015;
    private static final int DEFAULT_REPEAT_RUNS = 1;
    private static final double DEFAULT_REGRESSION_MAX_FINAL_DISTANCE = 1.25;
    private static final int DEFAULT_REGRESSION_MAX_STUCK_EVENTS = 5;
    private static final int DEFAULT_REGRESSION_MAX_RECOVERY_ATTEMPTS = 6;
    private static final int DEFAULT_REGRESSION_MAX_BACKWARD_TICKS = 80;
    private static final double DEFAULT_REGRESSION_MAX_AVERAGE_LATERAL_ERROR = 0.25;

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
            state.replayDirectory,
            state.replaySaveEnabled,
            state.headless,
            state.repeatRuns,
            state.failOnRegression,
            state.regressionMaxFinalDistance,
            state.regressionMaxStuckEvents,
            state.regressionMaxRecoveryAttempts,
            state.regressionMaxBackwardTicks,
            state.regressionMaxAverageLateralError,
            state.minecraftWorld.buildOrNull(),
            state.minecraftRoutes,
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
        private Path replayDirectory = defaultReplayDirectory();
        private boolean replaySaveEnabled = true;
        private boolean headless;
        private int repeatRuns = DEFAULT_REPEAT_RUNS;
        private boolean failOnRegression;
        private double regressionMaxFinalDistance = DEFAULT_REGRESSION_MAX_FINAL_DISTANCE;
        private int regressionMaxStuckEvents = DEFAULT_REGRESSION_MAX_STUCK_EVENTS;
        private int regressionMaxRecoveryAttempts = DEFAULT_REGRESSION_MAX_RECOVERY_ATTEMPTS;
        private int regressionMaxBackwardTicks = DEFAULT_REGRESSION_MAX_BACKWARD_TICKS;
        private double regressionMaxAverageLateralError = DEFAULT_REGRESSION_MAX_AVERAGE_LATERAL_ERROR;
        private final List<MinecraftRouteSpec> minecraftRoutes = new ArrayList<>();
        private MinecraftStressGrid minecraftStressGrid;
        private final MinecraftWorldImportOptions.Builder minecraftWorld = MinecraftWorldImportOptions.builder();

        void apply(String arg) {
            if (applyCore(arg)) {
                return;
            }
            applyMinecraft(arg);
        }

        private boolean applyCore(String arg) {
            return applyScenarioOption(arg)
                || applyRegressionOption(arg)
                || applyReplayOption(arg)
                || applyModeOption(arg);
        }

        private boolean applyScenarioOption(String arg) {
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
            if (arg.startsWith("--repeat=") || arg.startsWith("--regression-runs=")) {
                repeatRuns = parsePositiveInt(value(arg), DEFAULT_REPEAT_RUNS);
                return true;
            }
            return false;
        }

        private boolean applyRegressionOption(String arg) {
            if ("--fail-on-regression".equals(arg)) {
                failOnRegression = true;
                return true;
            }
            if (arg.startsWith("--regression-max-final-distance=") || arg.startsWith("--max-final-distance=")) {
                regressionMaxFinalDistance = parsePositiveDouble(value(arg), DEFAULT_REGRESSION_MAX_FINAL_DISTANCE);
                return true;
            }
            if (arg.startsWith("--regression-max-stuck-events=")) {
                regressionMaxStuckEvents = parseNonNegativeInt(value(arg), DEFAULT_REGRESSION_MAX_STUCK_EVENTS);
                return true;
            }
            if (arg.startsWith("--regression-max-recovery-attempts=")) {
                regressionMaxRecoveryAttempts = parseNonNegativeInt(
                    value(arg),
                    DEFAULT_REGRESSION_MAX_RECOVERY_ATTEMPTS);
                return true;
            }
            if (arg.startsWith("--regression-max-backward-ticks=")) {
                regressionMaxBackwardTicks = parseNonNegativeInt(value(arg), DEFAULT_REGRESSION_MAX_BACKWARD_TICKS);
                return true;
            }
            if (arg.startsWith("--regression-max-average-lateral-error=")) {
                regressionMaxAverageLateralError = parsePositiveDouble(
                    value(arg),
                    DEFAULT_REGRESSION_MAX_AVERAGE_LATERAL_ERROR);
                return true;
            }
            return false;
        }

        private boolean applyReplayOption(String arg) {
            if (arg.startsWith("--replay-dir=")) {
                replayDirectory = Path.of(value(arg));
                return true;
            }
            if ("--no-replay-save".equals(arg)) {
                replaySaveEnabled = false;
                return true;
            }
            return false;
        }

        private boolean applyModeOption(String arg) {
            if ("--headless".equals(arg)) {
                headless = true;
                return true;
            }
            if ("--ui".equals(arg)) {
                headless = false;
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
            } else if (arg.startsWith("--mc-route=")) {
                parseRoute(value(arg)).ifPresent(minecraftRoutes::add);
            } else if ("--mc-diagnostics".equals(arg)) {
                minecraftWorld.diagnostics(true);
            }
        }

        private static String value(String arg) {
            int equals = arg.indexOf('=');
            return equals < 0 ? "" : arg.substring(equals + 1).trim();
        }

        private static Path defaultReplayDirectory() {
            return ReplayPaths.defaultReplayDirectory();
        }

        private static int parsePositiveInt(String value, int fallback) {
            try {
                return Math.max(1, Integer.parseInt(value));
            } catch (NumberFormatException ignored) {
                return fallback;
            }
        }

        private static int parseNonNegativeInt(String value, int fallback) {
            try {
                return Math.max(0, Integer.parseInt(value));
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

        private static Optional<MinecraftRouteSpec> parseRoute(String value) {
            String[] routeParts = value.split(value.contains("@") ? "@" : "\\|");
            if (routeParts.length != 3) {
                return Optional.empty();
            }
            double[] start = parseVec(routeParts[1], 0.5, 64.0, 0.5);
            int[] goal = parseBlock(routeParts[2], 0, 64, 0);
            return Optional.of(new MinecraftRouteSpec(
                routeParts[0],
                new Vec3d(start[0], start[1], start[2]),
                goal[0],
                goal[1],
                goal[2]));
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
