package fr.riege.ebsl.pathfinding;

import fr.riege.ebsl.pathfinding.movement.WalkabilityChecker;
import fr.riege.ebsl.pathfinding.pathfinder.AStarPathfinder;
import fr.riege.ebsl.pathfinding.pathing.NeighborStrategies;
import fr.riege.ebsl.pathfinding.pathing.configuration.PathfinderConfiguration;
import fr.riege.ebsl.pathfinding.pathing.processing.impl.MinecraftPathProcessor;
import fr.riege.ebsl.pathfinding.provider.impl.MinecraftNavigationProvider;
import fr.riege.ebsl.pathfinding.wrapper.PathPosition;
import net.minecraft.util.Mth;

import java.util.Collection;
import java.util.ArrayList;
import java.util.List;

final class PathPipeline {
    private static final int INSTANT_WALK_MAX_ITERATIONS = 12000;
    private static final int INSTANT_WALK_MAX_LENGTH = 1800;

    private PathPipeline() {
    }

    static PathfinderConfiguration createWalkPathfinderConfiguration(WalkabilityChecker checker, boolean async) {
        return createWalkPathfinderConfiguration(checker, async, 100000, 12500);
    }

    static PathfinderConfiguration createInstantWalkPathfinderConfiguration(WalkabilityChecker checker) {
        return createWalkPathfinderConfiguration(
            checker,
            true,
            INSTANT_WALK_MAX_ITERATIONS,
            INSTANT_WALK_MAX_LENGTH);
    }

    static PathfinderConfiguration createRepairWalkPathfinderConfiguration(WalkabilityChecker checker) {
        return createWalkPathfinderConfiguration(checker, true, 8000, 600);
    }

    static PathfinderConfiguration createQueuedLongRangeSegmentConfiguration(WalkabilityChecker checker) {
        return createWalkPathfinderConfiguration(checker, true, 24000, 2600);
    }

    static PathfinderConfiguration createWalkPathfinderConfiguration(WalkabilityChecker checker, boolean async,
                                                                     int maxIterations, int maxLength) {
        return PathfinderConfiguration.builder()
            .provider(new MinecraftNavigationProvider(checker))
            .processors(List.of(new MinecraftPathProcessor(checker,
                PathfinderConfig.PATHFINDER_MAX_JUMP_HEIGHT.get())))
            .neighborStrategy(NeighborStrategies.horizontalDiagonalAndVertical(
                PathfinderConfig.PATHFINDER_MAX_JUMP_HEIGHT.get()))
            .maxIterations(maxIterations)
            .maxLength(maxLength)
            .async(async)
            .fallback(true)
            .build();
    }

    static int resolveStartY(WalkabilityChecker checker, double playerX, double playerY, double playerZ) {
        int x = Mth.floor(playerX);
        int y = Mth.floor(playerY);
        int z = Mth.floor(playerZ);
        if (checker == null) {
            return y;
        }
        if (checker.isPassable(x, y, z)) {
            return y;
        }
        if (checker.isPassable(x, y + 1, z)) {
            return y + 1;
        }
        return Mth.ceil(playerY);
    }

    static int resolveGoalYForXZ(WalkabilityChecker checker, int x, int preferredY, int z) {
        if (checker == null) {
            return preferredY;
        }

        for (int offset = 3; offset >= -4; offset--) {
            int candidateY = preferredY + offset;
            if (checker.isWalkable(x, candidateY, z)) {
                return candidateY;
            }
        }
        if (checker.isPassable(x, preferredY, z) && checker.isPassable(x, preferredY + 1, z)) {
            return preferredY;
        }
        if (checker.isSolid(x, preferredY, z)) {
            return preferredY + 1;
        }
        return preferredY;
    }

    static List<Node> buildLinearFlyPath(PathPosition start, PathPosition target) {
        return PathGeometry.buildLinearFlyPath(start, target);
    }

    static List<Node> buildLinearWalkPath(PathPosition start, PathPosition target) {
        return PathGeometry.buildLinearWalkPath(start, target);
    }

    static List<Node> smoothFlyPath(net.minecraft.client.Minecraft mc, List<Node> path) {
        return PathGeometry.smoothFlyPath(mc, path);
    }

    static ProcessedPath processWalkPath(Collection<PathPosition> positions,
                                         PathfinderConfiguration config,
                                         WalkabilityChecker checker) {
        return WalkPathProcessor.processWalkPath(positions, config, checker);
    }

    static void pushExploredNodesToVisualizer(AStarPathfinder pathfinder) {
        PathClosedSetVisualizer.pushExploredNodes(pathfinder);
    }

    static List<Node> mergePathPrefixWithTail(List<Node> prefix, List<Node> tail) {
        List<Node> merged = new ArrayList<>();
        appendDistinct(merged, prefix);
        appendDistinct(merged, tail);
        return merged;
    }

    private static void appendDistinct(List<Node> merged, List<Node> candidates) {
        if (candidates == null) {
            return;
        }
        for (Node candidate : candidates) {
            if (candidate == null) {
                continue;
            }
            if (!merged.isEmpty() && merged.getLast().position.equals(candidate.position)) {
                continue;
            }
            merged.add(candidate);
        }
    }
}
