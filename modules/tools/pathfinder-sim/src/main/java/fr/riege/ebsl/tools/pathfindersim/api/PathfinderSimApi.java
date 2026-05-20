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

package fr.riege.ebsl.tools.pathfindersim.api;

import fr.riege.ebsl.common.api.core.annotation.EbslApiOperation;
import fr.riege.ebsl.common.api.core.annotation.EbslApiSurface;
import fr.riege.ebsl.common.math.Vec3d;
import fr.riege.ebsl.common.pathfinding.goal.GoalCatalog;
import fr.riege.ebsl.common.pathfinding.goal.GoalContext;
import fr.riege.ebsl.common.pathfinding.goal.GoalDefinition;
import fr.riege.ebsl.common.pathfinding.goal.GoalParameterSpec;
import fr.riege.ebsl.common.pathfinding.goal.NavigationModeType;
import fr.riege.ebsl.common.pathfinding.goal.NavigationTarget;
import fr.riege.ebsl.tools.pathfindersim.cli.SimCliOptions;
import fr.riege.ebsl.tools.pathfindersim.core.SimulationSuite;
import fr.riege.ebsl.tools.pathfindersim.replay.ReplayRepository;
import fr.riege.ebsl.tools.pathfindersim.replay.SimulationReport;
import fr.riege.ebsl.tools.pathfindersim.replay.SimulationResult;
import fr.riege.ebsl.tools.pathfindersim.scenario.SimulationScenario;
import fr.riege.ebsl.tools.pathfindersim.world.minecraft.MinecraftWorldImportOptions;
import fr.riege.ebsl.tools.pathfindersim.world.minecraft.MinecraftWorldScenarioFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@EbslApiSurface(EbslApiSurface.Domain.SIMULATION)
public final class PathfinderSimApi {
    private static final String EMPTY_CATALOG = "{\"replays\":[]}";
    private static final String INDEX_FILE = "index.json";
    private static final String JSON_EXTENSION = ".json";
    private static final int DEFAULT_MAX_TICKS = 600;

    @EbslApiOperation("Read supported simulator goal definitions.")
    public List<SimGoalDescriptor> goals() {
        GoalContext context = new GoalContext(0, 64, 0);
        return GoalCatalog.all().stream()
            .filter(definition -> definition.mode() == NavigationModeType.WALK)
            .map(definition -> goalDescriptor(definition, context))
            .toList();
    }

    @EbslApiOperation("Run a Minecraft-world route through the simulator.")
    public List<SimulationResult> runMinecraftRoute(SimRouteRequest request) throws IOException {
        MinecraftWorldImportOptions routeOptions = routeOptions(request);
        List<SimulationScenario> scenarios = MinecraftWorldScenarioFactory.create(routeOptions, null);
        SimCliOptions options = new SimCliOptions(
            "all",
            positiveOrDefault(request.maxTicks(), DEFAULT_MAX_TICKS),
            25,
            0.015,
            null,
            replayDirectory(request),
            request.saveReplay(),
            true,
            routeOptions,
            null);
        List<SimulationResult> results = new SimulationSuite(scenarios).run(options);
        if (options.replaySaveEnabled() && !results.isEmpty()) {
            new ReplayRepository(options.replayDirectory()).save(results);
        }
        return results;
    }

    @EbslApiOperation("Run a Minecraft-world route and return replay JSON.")
    public String runMinecraftRouteJson(SimRouteRequest request) throws IOException {
        return SimulationReport.toJson(runMinecraftRoute(request));
    }

    @EbslApiOperation("Read a persisted replay catalogue.")
    public String replayCatalog(Path replayDirectory) throws IOException {
        Path index = replayDirectory(replayDirectory).resolve(INDEX_FILE);
        return Files.isRegularFile(index) ? Files.readString(index) : EMPTY_CATALOG;
    }

    @EbslApiOperation("Read a persisted replay JSON file.")
    public String replay(Path replayDirectory, String fileName) throws IOException {
        if (!isReplayFileName(fileName)) {
            throw new IOException("Invalid replay file: " + fileName);
        }
        Path root = replayDirectory(replayDirectory);
        Path replay = root.resolve(fileName).normalize();
        if (!replay.startsWith(root) || !Files.isRegularFile(replay)) {
            throw new IOException("Replay not found: " + fileName);
        }
        return Files.readString(replay);
    }

    private static SimGoalDescriptor goalDescriptor(GoalDefinition definition, GoalContext context) {
        return new SimGoalDescriptor(
            definition.id(),
            definition.label(),
            definition.description(),
            definition.mode().name(),
            definition.parameters().stream()
                .map(parameter -> goalParameter(parameter, context))
                .toList());
    }

    private static SimGoalParameter goalParameter(GoalParameterSpec parameter, GoalContext context) {
        return new SimGoalParameter(parameter.id(), parameter.label(), parameter.defaultValue(context));
    }

    private static MinecraftWorldImportOptions routeOptions(SimRouteRequest request) {
        Vec3d start = request.start() == null ? new Vec3d(0.5, 64.0, 0.5) : request.start();
        int[] goal = goalBlock(request, start);
        return new MinecraftWorldImportOptions(
            request.worldDirectory(),
            start,
            true,
            goal[0],
            goal[1],
            goal[2],
            true,
            positiveOrDefault(request.radiusChunks(), MinecraftWorldImportOptions.DEFAULT_RADIUS_CHUNKS),
            positiveOrDefault(request.goalSearchBlocks(), MinecraftWorldImportOptions.DEFAULT_GOAL_SEARCH_BLOCKS),
            false);
    }

    private static int[] goalBlock(SimRouteRequest request, Vec3d start) {
        GoalDefinition definition = GoalCatalog.byId(goalId(request.goalId()));
        GoalContext context = new GoalContext(floor(start.x()), floor(start.y()), floor(start.z()));
        NavigationTarget target = definition.create(withDefaults(definition, request.goalValues(), context), context)
            .resolve(context.x(), context.y(), context.z());
        return switch (target) {
            case NavigationTarget.Block(int x, int y, int z) -> new int[] { x, y, z };
            case NavigationTarget.Column(int x, int z) -> new int[] { x, context.y(), z };
        };
    }

    private static Map<String, Integer> withDefaults(GoalDefinition definition,
                                                     Map<String, Integer> values,
                                                     GoalContext context) {
        Map<String, Integer> source = values == null ? Map.of() : values;
        LinkedHashMap<String, Integer> merged = new LinkedHashMap<>();
        for (GoalParameterSpec parameter : definition.parameters()) {
            merged.put(parameter.id(), source.getOrDefault(parameter.id(), parameter.defaultValue(context)));
        }
        return merged;
    }

    private static String goalId(String value) {
        return value == null || value.isBlank() ? GoalCatalog.WALK : value;
    }

    private static Path replayDirectory(SimRouteRequest request) {
        return replayDirectory(request.replayDirectory());
    }

    private static Path replayDirectory(Path value) {
        Path directory = value == null
            ? Path.of(System.getProperty("user.home", "."), ".ebsl", "pathfinder-sim", "replays")
            : value;
        return directory.toAbsolutePath().normalize();
    }

    private static int positiveOrDefault(int value, int fallback) {
        return value > 0 ? value : fallback;
    }

    private static int floor(double value) {
        return (int) Math.floor(value);
    }

    private static boolean isReplayFileName(String fileName) {
        return fileName != null
            && fileName.endsWith(JSON_EXTENSION)
            && Path.of(fileName).getFileName().toString().equals(fileName)
            && !INDEX_FILE.equals(fileName);
    }
}
