package fr.riege.ebsl.pathfinding;

import fr.riege.ebsl.pathfinding.movement.WalkabilityChecker;
import fr.riege.ebsl.pathfinding.pathfinder.AStarPathfinder;
import fr.riege.ebsl.pathfinding.pathing.NeighborStrategies;
import fr.riege.ebsl.pathfinding.pathing.configuration.PathfinderConfiguration;
import fr.riege.ebsl.pathfinding.pathing.processing.impl.MinecraftPathProcessor;
import fr.riege.ebsl.pathfinding.settings.PathfinderSettings;
import fr.riege.ebsl.pathfinding.provider.impl.MinecraftNavigationProvider;
import fr.riege.ebsl.pathfinding.wrapper.PathPosition;
import net.minecraft.util.Mth;

import java.util.Collection;
import java.util.ArrayList;
import java.util.List;

final class PathPipeline {
    private PathPipeline() {
    }

    static PathfinderConfiguration createWalkPathfinderConfiguration(WalkabilityChecker checker, boolean async) {
        return createWalkPathfinderConfiguration(checker, async, true);
    }

    static PathfinderConfiguration createWalkPathfinderConfiguration(WalkabilityChecker checker, boolean async,
                                                                     boolean allowParkour) {
        return createWalkPathfinderConfiguration(checker, async, allowParkour, true, true, true);
    }

    static PathfinderConfiguration createWalkPathfinderConfiguration(WalkabilityChecker checker, boolean async,
                                                                     boolean allowParkour, boolean allowJump,
                                                                     boolean allowFall, boolean allowWalkDiagonal) {
        return createWalkPathfinderConfiguration(
            checker,
            async,
            PathfinderSettings.instance().defaultWalkMaxIterations.value(),
            PathfinderSettings.instance().defaultWalkMaxLength.value(),
            allowParkour, allowJump, allowFall, allowWalkDiagonal);
    }

    static PathfinderConfiguration createInstantWalkPathfinderConfiguration(WalkabilityChecker checker) {
        return createInstantWalkPathfinderConfiguration(checker, true);
    }

    static PathfinderConfiguration createInstantWalkPathfinderConfiguration(WalkabilityChecker checker,
                                                                           boolean allowParkour) {
        return createInstantWalkPathfinderConfiguration(checker, allowParkour, true, true, true);
    }

    static PathfinderConfiguration createInstantWalkPathfinderConfiguration(WalkabilityChecker checker,
                                                                            boolean allowParkour, boolean allowJump,
                                                                            boolean allowFall, boolean allowWalkDiagonal) {
        return createWalkPathfinderConfiguration(
            checker,
            true,
            PathfinderSettings.instance().instantWalkMaxIterations.value(),
            PathfinderSettings.instance().instantWalkMaxLength.value(),
            allowParkour, allowJump, allowFall, allowWalkDiagonal);
    }

    static PathfinderConfiguration createRepairWalkPathfinderConfiguration(WalkabilityChecker checker) {
        return createRepairWalkPathfinderConfiguration(checker, true);
    }

    static PathfinderConfiguration createRepairWalkPathfinderConfiguration(WalkabilityChecker checker,
                                                                          boolean allowParkour) {
        return createRepairWalkPathfinderConfiguration(checker, allowParkour, true, true, true);
    }

    static PathfinderConfiguration createRepairWalkPathfinderConfiguration(WalkabilityChecker checker,
                                                                           boolean allowParkour, boolean allowJump,
                                                                           boolean allowFall, boolean allowWalkDiagonal) {
        return createWalkPathfinderConfiguration(
            checker,
            true,
            PathfinderSettings.instance().repairWalkMaxIterations.value(),
            PathfinderSettings.instance().repairWalkMaxLength.value(),
            allowParkour, allowJump, allowFall, allowWalkDiagonal);
    }

    static PathfinderConfiguration createQueuedLongRangeSegmentConfiguration(WalkabilityChecker checker) {
        return createWalkPathfinderConfiguration(
            checker,
            true,
            PathfinderSettings.instance().queuedLongRangeMaxIterations.value(),
            PathfinderSettings.instance().queuedLongRangeMaxLength.value());
    }

    static PathfinderConfiguration createWalkPathfinderConfiguration(WalkabilityChecker checker, boolean async,
                                                                     int maxIterations, int maxLength) {
        return createWalkPathfinderConfiguration(checker, async, maxIterations, maxLength, true);
    }

    static PathfinderConfiguration createWalkPathfinderConfiguration(WalkabilityChecker checker, boolean async,
                                                                     int maxIterations, int maxLength,
                                                                     boolean allowParkour) {
        return createWalkPathfinderConfiguration(checker, async, maxIterations, maxLength, allowParkour, true, true, true);
    }

    static PathfinderConfiguration createWalkPathfinderConfiguration(WalkabilityChecker checker, boolean async,
                                                                     int maxIterations, int maxLength,
                                                                     boolean allowParkour, boolean allowJump,
                                                                     boolean allowFall, boolean allowWalkDiagonal) {
        return PathfinderConfiguration.builder()
            .provider(new MinecraftNavigationProvider(checker))
            .processors(List.of(new MinecraftPathProcessor(checker,
                PathfinderSettings.instance().maxJumpHeight.value())))
            .neighborStrategy(NeighborStrategies.horizontalDiagonalAndVertical(
                PathfinderSettings.instance().maxJumpHeight.value(), allowParkour, allowJump, allowFall, allowWalkDiagonal))
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
