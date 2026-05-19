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

package fr.riege.ebsl.tools.pathfindersim.world.minecraft;

import fr.riege.ebsl.common.math.Vec3d;
import fr.riege.ebsl.common.navigation.PathPlannerOptions;
import fr.riege.ebsl.common.navigation.PathPlan;
import fr.riege.ebsl.common.navigation.PathPlanningService;
import fr.riege.ebsl.common.navigation.runtime.entity.EntityFollowerOptions;
import fr.riege.ebsl.common.navigation.runtime.headless.HeadlessWorldLayer;
import fr.riege.ebsl.common.pathfinding.movement.MovementTerrain;
import fr.riege.ebsl.common.pathfinding.wrapper.PathPosition;
import fr.riege.ebsl.tools.pathfindersim.scenario.SimulationScenario;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletionException;

public final class MinecraftWorldScenarioFactory {
    private static final int START_SCAN_RADIUS = 24;
    private static final int GOAL_SCAN_STEP = 2;
    private static final int MAX_GOAL_PLAN_ATTEMPTS = 32;
    private static final int GOAL_PROBE_TIME_MS = 75;

    private MinecraftWorldScenarioFactory() {
    }

    public static SimulationScenario create(MinecraftWorldImportOptions options) throws IOException {
        MinecraftWorldImportOptions effectiveOptions = effectiveOptions(options);
        ImportedMinecraftWorld importedWorld = new AnvilWorldLoader().load(effectiveOptions);
        HeadlessWorldLayer world = importedWorld.world();
        PathPlannerOptions plannerOptions = PathPlannerOptions.defaults().toBuilder()
            .async(false)
            .fallback(false)
            .processPath(true)
            .maxIterations(120_000)
            .maxLength(20_000)
            .maxCalculationTimeMs(5_000)
            .build();
        PathPlanningService planner = new PathPlanningService(world);
        MinecraftWalkTarget startTarget = resolveStart(effectiveOptions, planner);
        MinecraftWalkTarget goalTarget = resolveGoal(effectiveOptions, planner, startTarget, plannerOptions);
        return new SimulationScenario(
            "minecraft_world",
            description(effectiveOptions, importedWorld.stats(), startTarget, goalTarget),
            world,
            new Vec3d(startTarget.centerX(), startTarget.y(), startTarget.centerZ()),
            goalTarget.x(),
            goalTarget.y(),
            goalTarget.z(),
            plannerOptions,
            EntityFollowerOptions.defaults());
    }

    private static MinecraftWorldImportOptions effectiveOptions(MinecraftWorldImportOptions options) throws IOException {
        Path worldDirectory = resolveWorldDirectory(options.worldDirectory());
        if (options.startExplicit() && options.goalExplicit()) {
            return withWorldDirectory(options, worldDirectory, options.start(), options.radiusChunks());
        }
        List<Vec3d> anchors = MinecraftWorldAnchors.load(worldDirectory);
        Vec3d start = options.startExplicit()
            ? options.start()
            : anchors.stream().findFirst().orElse(options.start());
        int radius = options.goalExplicit()
            ? options.radiusChunks()
            : Math.max(options.radiusChunks(), (int) Math.ceil(options.goalSearchBlocks() / 16.0) + 1);
        return new MinecraftWorldImportOptions(
            worldDirectory,
            start,
            true,
            options.goalX(),
            options.goalY(),
            options.goalZ(),
            options.goalExplicit(),
            radius,
            options.goalSearchBlocks(),
            options.diagnostics());
    }

    private static MinecraftWorldImportOptions withWorldDirectory(MinecraftWorldImportOptions options,
                                                                  Path worldDirectory,
                                                                  Vec3d start,
                                                                  int radius) {
        return new MinecraftWorldImportOptions(
            worldDirectory,
            start,
            options.startExplicit(),
            options.goalX(),
            options.goalY(),
            options.goalZ(),
            options.goalExplicit(),
            radius,
            options.goalSearchBlocks(),
            options.diagnostics());
    }

    private static Path resolveWorldDirectory(Path requested) throws IOException {
        for (Path candidate : pathCandidates(requested)) {
            Path resolved = resolveExistingOrSibling(candidate);
            if (Files.isDirectory(resolved)) {
                return resolved;
            }
        }
        return requested;
    }

    private static List<Path> pathCandidates(Path requested) {
        if (requested.isAbsolute()) {
            return List.of(requested);
        }
        List<Path> candidates = new ArrayList<>();
        Path current = Path.of(System.getProperty("user.dir")).toAbsolutePath();
        while (current != null) {
            candidates.add(current.resolve(requested).normalize());
            current = current.getParent();
        }
        candidates.add(requested);
        return List.copyOf(candidates);
    }

    private static Path resolveExistingOrSibling(Path requested) throws IOException {
        if (Files.isDirectory(requested)) {
            return requested;
        }
        Path parent = requested.getParent();
        Path fileName = requested.getFileName();
        if (parent == null || fileName == null || !Files.isDirectory(parent)) {
            return requested;
        }
        String prefix = fileName.toString();
        try (var candidates = Files.list(parent)) {
            return candidates
                .filter(Files::isDirectory)
                .filter(path -> path.getFileName().toString().startsWith(prefix))
                .findFirst()
                .orElse(requested);
        }
    }

    private static MinecraftWalkTarget resolveStart(MinecraftWorldImportOptions options, PathPlanningService planner) {
        int x = (int) Math.floor(options.start().x());
        int y = planner.resolveStartY(options.start().x(), options.start().y(), options.start().z());
        int z = (int) Math.floor(options.start().z());
        return nearestWalkable(planner.checker(), x, y, z, START_SCAN_RADIUS)
            .orElse(new MinecraftWalkTarget(x, y, z));
    }

    private static MinecraftWalkTarget resolveGoal(MinecraftWorldImportOptions options,
                                                   PathPlanningService planner,
                                                   MinecraftWalkTarget start,
                                                   PathPlannerOptions plannerOptions) {
        if (options.goalExplicit()) {
            int y = planner.resolveGoalYForXZ(options.goalX(), options.goalY(), options.goalZ());
            return nearestWalkable(planner.checker(), options.goalX(), y, options.goalZ(), START_SCAN_RADIUS)
                .orElse(new MinecraftWalkTarget(options.goalX(), y, options.goalZ()));
        }
        return farReachableGoal(options, planner, start, plannerOptions)
            .orElse(start);
    }

    private static Optional<MinecraftWalkTarget> nearestWalkable(MovementTerrain terrain,
                                                                int centerX,
                                                                int centerY,
                                                                int centerZ,
                                                                int radius) {
        MinecraftWalkTarget best = null;
        double bestDistance = Double.MAX_VALUE;
        int minY = centerY - radius;
        int maxY = centerY + radius;
        for (int x = centerX - radius; x <= centerX + radius; x++) {
            for (int z = centerZ - radius; z <= centerZ + radius; z++) {
                for (int y = minY; y <= maxY; y++) {
                    if (terrain.isWalkable(x, y, z)) {
                        double distance = squaredDistance(centerX, centerY, centerZ, x, y, z);
                        if (distance < bestDistance) {
                            bestDistance = distance;
                            best = new MinecraftWalkTarget(x, y, z);
                        }
                    }
                }
            }
        }
        return Optional.ofNullable(best);
    }

    private static Optional<MinecraftWalkTarget> farReachableGoal(MinecraftWorldImportOptions options,
                                                                  PathPlanningService planner,
                                                                  MinecraftWalkTarget start,
                                                                  PathPlannerOptions plannerOptions) {
        List<MinecraftWalkTarget> candidates = walkableCandidates(planner.checker(), start, options.goalSearchBlocks());
        PathPosition startPosition = new PathPosition(start.x(), start.y(), start.z());
        PathPlannerOptions probeOptions = plannerOptions.toBuilder()
            .maxIterations(25_000)
            .maxLength(6_000)
            .maxCalculationTimeMs(GOAL_PROBE_TIME_MS)
            .build();
        int attempts = 0;
        MinecraftWalkTarget fallback = null;
        for (MinecraftWalkTarget candidate : candidates) {
            if (++attempts > MAX_GOAL_PLAN_ATTEMPTS) {
                break;
            }
            PathPosition target = new PathPosition(candidate.x(), candidate.y(), candidate.z());
            try {
                PathPlan plan = planner.plan(startPosition, target, probeOptions).toCompletableFuture().join();
                if (plan.complete() && plan.positions().size() > 1) {
                    return Optional.of(candidate);
                }
                if (fallback == null && plan.usable() && plan.positions().size() > 1) {
                    fallback = candidate;
                }
            } catch (CompletionException ignored) {
                planner.clearCaches();
            }
        }
        return fallback == null ? candidates.stream().findFirst() : Optional.of(fallback);
    }

    private static List<MinecraftWalkTarget> walkableCandidates(MovementTerrain terrain,
                                                                MinecraftWalkTarget start,
                                                                int radius) {
        List<MinecraftWalkTarget> candidates = new ArrayList<>();
        for (int x = start.x() - radius; x <= start.x() + radius; x += GOAL_SCAN_STEP) {
            for (int z = start.z() - radius; z <= start.z() + radius; z += GOAL_SCAN_STEP) {
                Optional<Integer> walkableY = bestWalkableY(terrain, x, start.y(), z, radius);
                if (walkableY.isPresent()) {
                    candidates.add(new MinecraftWalkTarget(x, walkableY.get(), z));
                }
            }
        }
        return candidates.stream()
            .filter(candidate -> candidate.horizontalDistanceSquared(start.centerX(), start.centerZ()) >= 64.0)
            .sorted(Comparator.comparingDouble(
                (MinecraftWalkTarget candidate) -> goalScore(candidate, start))
                .reversed())
            .limit(MAX_GOAL_PLAN_ATTEMPTS * 2L)
            .toList();
    }

    private static double goalScore(MinecraftWalkTarget candidate, MinecraftWalkTarget start) {
        int verticalDelta = Math.abs(candidate.y() - start.y());
        return candidate.horizontalDistanceSquared(start.centerX(), start.centerZ()) - verticalDelta * 64.0;
    }

    private static Optional<Integer> bestWalkableY(MovementTerrain terrain, int x, int centerY, int z, int radius) {
        for (int y = centerY + radius; y >= centerY - radius; y--) {
            if (terrain.isWalkable(x, y, z)) {
                return Optional.of(y);
            }
        }
        return Optional.empty();
    }

    private static double squaredDistance(int ax, int ay, int az, int bx, int by, int bz) {
        double dx = (double) ax - bx;
        double dy = (double) ay - by;
        double dz = (double) az - bz;
        return dx * dx + dy * dy + dz * dz;
    }

    private static String description(MinecraftWorldImportOptions options,
                                      MinecraftImportStats stats,
                                      MinecraftWalkTarget start,
                                      MinecraftWalkTarget goal) {
        return "Minecraft Anvil import "
            + (options.goalExplicit() ? "with explicit goal" : "with auto reachable goal")
            + " start=" + start.x() + ',' + start.y() + ',' + start.z()
            + " goal=" + goal.x() + ',' + goal.y() + ',' + goal.z()
            + " import={" + stats.summary() + '}';
    }
}
