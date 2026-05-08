package fr.riege.ebsl.common.navigation;

import fr.riege.ebsl.common.platform.layer.IWorldLayer;
import fr.riege.ebsl.common.pathfinding.ProcessedPath;
import fr.riege.ebsl.common.pathfinding.WalkPathProcessor;
import fr.riege.ebsl.common.pathfinding.movement.WalkabilityChecker;
import fr.riege.ebsl.common.pathfinding.pathfinder.AStarPathfinder;
import fr.riege.ebsl.common.pathfinding.pathing.NeighborStrategies;
import fr.riege.ebsl.common.pathfinding.pathing.configuration.PathfinderConfiguration;
import fr.riege.ebsl.common.pathfinding.pathing.processing.NodeProcessorRegistry;
import fr.riege.ebsl.common.pathfinding.pathing.result.PathfinderResult;
import fr.riege.ebsl.common.pathfinding.provider.LayerNavigationPointProvider;
import fr.riege.ebsl.common.pathfinding.settings.PathfinderSettings;
import fr.riege.ebsl.common.pathfinding.wrapper.PathPosition;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletionStage;

public final class PathPlanningService {
    private final IWorldLayer world;
    private final WalkabilityChecker checker;
    private final LayerNavigationPointProvider provider;

    public PathPlanningService(IWorldLayer world) {
        this.world = Objects.requireNonNull(world, "world");
        this.checker = new WalkabilityChecker(world);
        this.provider = new LayerNavigationPointProvider(checker);
    }

    public IWorldLayer world() {
        return world;
    }

    public WalkabilityChecker checker() {
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
        PathPlannerOptions effectiveOptions = options == null ? PathPlannerOptions.defaults() : options;
        clearCaches();
        PathfinderConfiguration configuration = configuration(effectiveOptions);
        AStarPathfinder pathfinder = new AStarPathfinder(configuration);
        return pathfinder.findPath(start.floor(), resolveTarget(target))
            .thenApply(result -> toPlan(result, configuration, effectiveOptions));
    }

    public PathPosition positionFromEntity(double x, double y, double z) {
        return new PathPosition(Math.floor(x), resolveStartY(x, y, z), Math.floor(z));
    }

    public PathPosition resolveTarget(PathPosition target) {
        int x = target.flooredX();
        int y = target.flooredY();
        int z = target.flooredZ();
        if (checker.isSolid(x, y, z)) {
            return new PathPosition(x, y + 1, z);
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
            .processors(NodeProcessorRegistry.createStandardProcessors())
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
            .maxCalculationTimeMs(effectiveOptions.maxCalculationTimeMs())
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
        return PathPlan.from(result, configuration, positions, processedPath);
    }
}
