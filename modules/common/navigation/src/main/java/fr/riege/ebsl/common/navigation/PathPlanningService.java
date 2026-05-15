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

package fr.riege.ebsl.common.navigation;

import fr.riege.ebsl.common.pathfinding.ProcessedPath;
import fr.riege.ebsl.common.pathfinding.WalkPathProcessor;
import fr.riege.ebsl.common.pathfinding.movement.MovementTerrain;
import fr.riege.ebsl.common.pathfinding.movement.WalkabilityChecker;
import fr.riege.ebsl.common.pathfinding.pathfinder.Pathfinders;
import fr.riege.ebsl.common.pathfinding.pathing.InspectablePathfinder;
import fr.riege.ebsl.common.pathfinding.pathing.NeighborStrategies;
import fr.riege.ebsl.common.pathfinding.pathing.configuration.PathfinderConfiguration;
import fr.riege.ebsl.common.pathfinding.pathing.goal.PathGoal;
import fr.riege.ebsl.common.pathfinding.pathing.goal.PathGoals;
import fr.riege.ebsl.common.pathfinding.pathing.result.PathfinderResult;
import fr.riege.ebsl.common.pathfinding.provider.NavigationPointProviders;
import fr.riege.ebsl.common.pathfinding.provider.WorldNavigationPointProvider;
import fr.riege.ebsl.common.pathfinding.registry.PathfindingRegistries;
import fr.riege.ebsl.common.pathfinding.settings.PathfinderSettings;
import fr.riege.ebsl.common.pathfinding.wrapper.PathPosition;
import fr.riege.ebsl.common.world.layer.IWorldLayer;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletionStage;

public final class PathPlanningService {
    private final IWorldLayer world;
    private final MovementTerrain checker;
    private final WorldNavigationPointProvider provider;

    public PathPlanningService(IWorldLayer world) {
        this.world = Objects.requireNonNull(world, "world");
        this.checker = new WalkabilityChecker(world);
        this.provider = NavigationPointProviders.worldBacked(checker);
    }

    public IWorldLayer world() {
        return world;
    }

    public MovementTerrain checker() {
        return checker;
    }

    public void clearCaches() {
        checker.clearCache();
        provider.clearCache();
    }

    public CompletionStage<PathPlan> plan(PathPosition start, PathPosition target) {
        return plan(start, target, PathPlannerOptions.defaults());
    }

    public CompletionStage<PathPlan> plan(PathPosition start, PathPosition target, PathPlannerOptions options) {
        Objects.requireNonNull(start, "start");
        Objects.requireNonNull(target, "target");
        return plan(start, PathGoals.exact(resolveTarget(target)), options);
    }

    public CompletionStage<PathPlan> plan(PathPosition start, PathGoal goal, PathPlannerOptions options) {
        Objects.requireNonNull(start, "start");
        Objects.requireNonNull(goal, "goal");
        PathPlannerOptions effectiveOptions = options == null ? PathPlannerOptions.defaults() : options;
        return planOnce(start, goal, effectiveOptions);
    }

    private CompletionStage<PathPlan> planOnce(PathPosition start, PathGoal goal, PathPlannerOptions options) {
        clearCaches();
        PathfinderConfiguration configuration = configuration(options);
        InspectablePathfinder pathfinder = Pathfinders.inspectableAStar(configuration);
        return pathfinder.findPath(start.floor(), goal)
            .thenApply(result -> toPlan(result, configuration, options));
    }

    public PathPosition positionFromEntity(double x, double y, double z) {
        return new PathPosition(Math.floor(x), resolveStartY(x, y, z), Math.floor(z));
    }

    public PathPosition resolveTarget(PathPosition target) {
        int x = target.flooredX();
        int y = target.flooredY();
        int z = target.flooredZ();
        if (checker.isSolid(x, y, z)) {
            return new PathPosition(x, y + 1.0, z);
        }
        return target.floor();
    }

    public int resolveStartY(double entityX, double entityY, double entityZ) {
        int x = (int) Math.floor(entityX);
        int y = (int) Math.floor(entityY);
        int z = (int) Math.floor(entityZ);
        if (checker.isPassable(x, y, z)) return y;
        if (checker.isPassable(x, y + 1, z)) return y + 1;
        return (int) Math.ceil(entityY);
    }

    public int resolveGoalYForXZ(int x, int preferredY, int z) {
        for (int offset = 3; offset >= -4; offset--) {
            int candidateY = preferredY + offset;
            if (checker.isWalkable(x, candidateY, z)) return candidateY;
        }
        if (checker.isPassable(x, preferredY, z) && checker.isPassable(x, preferredY + 1, z)) return preferredY;
        if (checker.isSolid(x, preferredY, z)) return preferredY + 1;
        return preferredY;
    }

    public PathfinderConfiguration configuration(PathPlannerOptions options) {
        PathPlannerOptions effectiveOptions = options == null ? PathPlannerOptions.defaults() : options;
        PathfinderSettings settings = PathfinderSettings.instance();
        return PathfinderConfiguration.builder()
            .maxIterations(effectiveOptions.maxIterations())
            .maxLength(effectiveOptions.maxLength())
            .provider(provider)
            .processors(PathfindingRegistries.nodeProcessors().standardProcessors())
            .neighborStrategy(NeighborStrategies.horizontalDiagonalAndVertical(
                effectiveOptions.maxJumpHeight(),
                effectiveOptions.allowParkour(),
                effectiveOptions.allowJump(),
                effectiveOptions.allowFall(),
                effectiveOptions.allowWalkDiagonal()))
            .async(effectiveOptions.async())
            .fallback(effectiveOptions.fallback())
            .earlyFallback(settings.earlyFallbackEnabled.value())
            .earlyFallbackIterations(settings.earlyFallbackIterations.value())
            .earlyFallbackMinPathNodes(settings.earlyFallbackMinPathNodes.value())
            .earlyFallbackMinProgressRatio(settings.earlyFallbackMinProgressRatio.value())
            .goalRefinement(settings.goalRefinementEnabled.value())
            .goalRefinementMinIterations(settings.goalRefinementMinIterations.value())
            .goalRefinementMaxIterations(settings.goalRefinementMaxIterations.value())
            .goalRefinementMaxTimeMs(settings.goalRefinementMaxTimeMs.value())
            .goalRefinementCostMargin(settings.goalRefinementCostMargin.value())
            .maxCalculationTimeMs(effectiveOptions.maxCalculationTimeMs())
            .qualityRiskCostWeight(effectiveOptions.qualityPlanningMode().costAware()
                ? effectiveOptions.qualityRiskCostWeight()
                : 0.0)
            .qualityTerrainCostWeight(effectiveOptions.qualityPlanningMode().costAware()
                ? effectiveOptions.qualityTerrainCostWeight()
                : 0.0)
            .build();
    }

    private PathPlan toPlan(PathfinderResult result,
                            PathfinderConfiguration configuration,
                            PathPlannerOptions options) {
        Collection<PathPosition> positions = result != null && result.getPath() != null
            ? result.getPath().collect()
            : List.of();
        if (positions.isEmpty()) {
            return PathPlan.empty(result, configuration);
        }
        ProcessedPath processedPath = options.processPath()
            ? WalkPathProcessor.process(positions, configuration, checker)
            : null;
        return PathPlan.from(result, configuration, positions, processedPath, checker);
    }
}
