package fr.riege.ebsl.pathfinding;

import fr.riege.ebsl.pathfinding.debug.PathVisualizer;
import fr.riege.ebsl.pathfinding.execution.FlyExecutor;
import fr.riege.ebsl.pathfinding.movement.WalkabilityChecker;
import fr.riege.ebsl.pathfinding.pathfinder.AStarPathfinder;
import fr.riege.ebsl.pathfinding.pathing.configuration.PathfinderConfiguration;
import fr.riege.ebsl.pathfinding.pathing.result.PathfinderResult;
import fr.riege.ebsl.pathfinding.wrapper.PathPosition;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

final class WalkNavigationService {
    private static final Logger LOGGER = LoggerFactory.getLogger("ebsl-navigation");

    private final PathfindingRuntime runtime;

    WalkNavigationService(PathfindingRuntime runtime) {
        this.runtime = runtime;
    }

    void startPathfind(Minecraft mc, int x, int y, int z, boolean fly) {
        startPathfind(mc, x, y, z, fly, true);
    }

    void startPathfind(Minecraft mc, int x, int y, int z, boolean fly, boolean clearLongRange) {
        if (mc.player == null) {
            return;
        }

        if (runtime.state.isNavigating()) {
            runtime.abortCurrentNavigation(mc);
        }
        if (clearLongRange) {
            runtime.longRangeSession.clear();
        }

        WalkabilityChecker checker = !fly && mc.level != null ? new WalkabilityChecker(mc.level) : null;
        int finalY = checker != null && checker.isSolid(x, y, z) ? y + 1 : y;

        runtime.state.begin(fly ? NavigationMode.FLY : NavigationMode.WALK, x, finalY, z);
        fr.riege.ebsl.util.ClientUtils.sendMessage(mc,
            "§eFinding path to " + x + ", " + finalY + ", " + z + "...", false);

        int startX = (int) Math.floor(mc.player.getX());
        int startZ = (int) Math.floor(mc.player.getZ());
        int startY = PathPipeline.resolveStartY(checker, mc.player.getX(), mc.player.getY(), mc.player.getZ());
        PathPosition start = new PathPosition(startX, startY, startZ);
        PathPosition target = new PathPosition(x, finalY, z);

        if (fly) {
            startFlyNavigation(mc, start, target, x, finalY, z);
            return;
        }

        startWalkNavigation(mc, checker, start, target, x, finalY, z);
    }

    void startPathTest(Minecraft mc, int x, int y, int z) {
        if (mc.player == null || mc.level == null) {
            return;
        }

        if (!PathVisualizer.isEnabled()) {
            PathVisualizer.toggle();
        }
        PathVisualizer.clear();

        WalkabilityChecker checker = new WalkabilityChecker(mc.level);
        int startX = (int) Math.floor(mc.player.getX());
        int startZ = (int) Math.floor(mc.player.getZ());
        int startY = PathPipeline.resolveStartY(checker, mc.player.getX(), mc.player.getY(), mc.player.getZ());
        int finalY = checker.isSolid(x, y, z) ? y + 1 : y;

        fr.riege.ebsl.util.ClientUtils.sendMessage(mc,
            "§ePath test: running A* to " + x + ", " + finalY + ", " + z + "...", false);

        Thread thread = new Thread(
            () -> runPathTest(mc, checker, startX, startY, startZ, x, finalY, z),
            "Aether-PathTest");
        thread.setDaemon(true);
        thread.start();
    }

    void startGreenhouseWalk(Minecraft mc, Vec3 target, Runnable onFinished, boolean isFirst) {
        if (mc.player == null) {
            return;
        }

        int tx = (int) Math.floor(target.x);
        int ty = (int) Math.floor(target.y);
        int tz = (int) Math.floor(target.z);
        double centerVariationX = (Math.random() * 0.4) - 0.2;
        double centerVariationZ = (Math.random() * 0.4) - 0.2;

        runtime.walkOptions.reset();
        runtime.walkOptions.configure(target.add(0, -10.0, 0), onFinished, null, !isFirst, 0.1);
        runtime.walkOptions.setGoalCenterOffsets(0.5 + centerVariationX, 0.5 + centerVariationZ);
        runtime.walkOptions.setAllowRotation(false);
        runtime.walkOptions.setAllowReplan(false);
        runtime.walkOptions.setAllowJumps(false);
        runtime.walkOptions.setExactGoalCentering(true);

        if (isFirst) {
            runtime.state.setRotationTarget(null);
            startPathfind(mc, tx, ty, tz, false);
            return;
        }

        if (runtime.state.isNavigating()) {
            runtime.abortCurrentNavigation(mc);
        }

        runtime.state.begin(NavigationMode.WALK, tx, ty, tz);
        runtime.longRangeSession.clear();

        PathPosition start = new PathPosition(mc.player.getX(), mc.player.getY(), mc.player.getZ());
        PathPosition targetPos = new PathPosition(target.x, target.y, target.z);
        List<Node> directPath = PathPipeline.buildLinearWalkPath(start, targetPos);

        PathVisualizer.setPath(directPath, 0);
        runtime.executor.start(directPath, tx, ty, tz, true, null, onFinished);
        runtime.walkOptions.applyTo(runtime.executor);

        fr.riege.ebsl.util.ClientUtils.sendMessage(mc, "§7Greenhouse path: direct walk to target.", false);
    }

    boolean updateFlyExecution(Minecraft mc) {
        if (!runtime.state.isFlyExecutionActive(runtime.flyExecutor)) {
            return false;
        }

        ensurePlayerIsFlying(mc);
        runtime.flyExecutor.tick(mc);
        if (runtime.flyExecutor.getState() == FlyExecutor.State.FINISHED) {
            runtime.longRangeSession.clear();
            runtime.state.markIdle();
        }
        return true;
    }

    boolean isWalkExecutionDone() {
        var state = runtime.executor.getState();
        return state == fr.riege.ebsl.pathfinding.execution.PathExecutor.State.FINISHED
            || state == fr.riege.ebsl.pathfinding.execution.PathExecutor.State.FAILED;
    }

    void refreshWalkVisualizer() {
        PathVisualizer.setCameraPath(runtime.executor.getCameraPath());
        PathVisualizer.updateExecution(runtime.executor.getWaypointIndex(), runtime.executor.getCamTargetIdx());
    }

    void startPreparedWalkSegment(LongRangePathSession.PendingSegment pendingSegment) {
        runtime.state.updateGoal(pendingSegment.goalX(), pendingSegment.goalY(), pendingSegment.goalZ());
        if (runtime.executor.getState() == fr.riege.ebsl.pathfinding.execution.PathExecutor.State.WALKING
            && pendingSegment.attachment() == LongRangePathSession.SegmentAttachment.MERGE_WITH_CURRENT) {
            runtime.executor.continueWith(
                pendingSegment.path(),
                pendingSegment.goalX(),
                pendingSegment.goalY(),
                pendingSegment.goalZ());
        } else {
            runtime.executor.start(
                pendingSegment.path(),
                pendingSegment.goalX(),
                pendingSegment.goalY(),
                pendingSegment.goalZ(),
                runtime.walkOptions.isPreciseExecution(),
                runtime.state.rotationTarget(),
                runtime.walkOptions.onFinished());
        }
        runtime.walkOptions.applyTo(runtime.executor);
        PathVisualizer.setPath(runtime.executor.getPathSnapshot(), runtime.executor.getWaypointIndex());
        refreshWalkVisualizer();
    }

    private void startFlyNavigation(Minecraft mc, PathPosition start, PathPosition target,
                                    int x, int y, int z) {
        List<Node> rawNodes = PathPipeline.buildLinearFlyPath(start, target);
        List<Node> smoothed = PathPipeline.smoothFlyPath(mc, rawNodes);

        if (smoothed.isEmpty()) {
            runtime.longRangeSession.clear();
            runtime.state.markIdle();
            if (mc.player != null) {
                fr.riege.ebsl.util.ClientUtils.sendMessage(mc, "§cFly path build failed!", false);
            }
            return;
        }

        PathVisualizer.setPath(smoothed, 0);
        if (mc.player != null) {
            fr.riege.ebsl.util.ClientUtils.sendMessage(mc,
                "§aFly path: " + smoothed.size() + " waypoints (direct LOS). Flying...", false);
        }
        runtime.flyExecutor.start(smoothed, x, y, z);
    }

    private void startWalkNavigation(Minecraft mc, WalkabilityChecker checker,
                                     PathPosition start, PathPosition target,
                                     int x, int y, int z) {
        PathfinderConfiguration config = PathPipeline.createWalkPathfinderConfiguration(checker, true);
        AStarPathfinder pathfinder = new AStarPathfinder(config);
        runtime.state.setCurrentPathfinder(pathfinder);
        long startMs = System.currentTimeMillis();

        pathfinder.findPath(start, target)
            .whenComplete((result, throwable) -> mc.execute(() -> {
                if (runtime.state.isAborted() || !runtime.state.isCurrentPathfinder(pathfinder)) {
                    return;
                }
                if (throwable != null) {
                    runtime.state.clearCurrentPathfinder();
                    runtime.state.markIdle();
                    LOGGER.error("Walk path calculation failed for target {}, {}, {}", x, y, z, throwable);
                    if (mc.player != null) {
                        fr.riege.ebsl.util.ClientUtils.sendMessage(mc, "§cPath calculation failed. See log for details.", false);
                    }
                    if (runtime.walkOptions.onFailed() != null) {
                        runtime.walkOptions.onFailed().run();
                    }
                    return;
                }
                handleWalkResult(mc, result, config, checker, x, y, z, startMs, pathfinder.getExploredCount());
            }));
    }

    private void handleWalkResult(Minecraft mc, PathfinderResult result,
                                  PathfinderConfiguration config, WalkabilityChecker checker,
                                  int x, int y, int z,
                                  long startMs, long exploredCount) {
        runtime.state.clearCurrentPathfinder();

        Collection<PathPosition> positions = result != null && result.getPath() != null
            ? result.getPath().collect()
            : Collections.emptyList();
        if (!PathResultClassifier.hasUsablePath(result, positions)) {
            runtime.state.markIdle();
            if (mc.player != null) {
                fr.riege.ebsl.util.ClientUtils.sendMessage(mc, "§cNo path found!", false);
            }
            if (runtime.walkOptions.onFailed() != null) {
                runtime.walkOptions.onFailed().run();
            }
            return;
        }

        ProcessedPath processedPath = PathPipeline.processWalkPath(positions, config, checker);
        if (processedPath.navigationPath().isEmpty()) {
            runtime.state.markIdle();
            if (runtime.longRangeSession.isActive()) {
                runtime.longRangeSession.markSegmentCalculationFailed(System.currentTimeMillis());
            }
            if (mc.player != null) {
                fr.riege.ebsl.util.ClientUtils.sendMessage(mc, "§cPath processing produced no usable waypoints.", false);
            }
            return;
        }
        PathVisualizer.setPath(processedPath.navigationPath(), 0);
        PathVisualizer.setCameraPath(Collections.emptyList());
        boolean partial = PathResultClassifier.isPartialWalkResult(result, positions, x, y, z);
        boolean longRangeActive = runtime.longRangeSession.isActive();
        boolean longRangeFinalSegment = longRangeActive
            && x == runtime.longRangeSession.finalGoalX()
            && z == runtime.longRangeSession.finalGoalZ();
        boolean longRangeIntermediateSegment = longRangeActive && !longRangeFinalSegment;
        Node segmentEnd = processedPath.navigationPath().getLast();
        boolean continuationNeeded = longRangeActive
            && (!runtime.longRangeSession.isFinalGoalReached(new Vec3(x + 0.5, y, z + 0.5)) || partial)
            && (x != runtime.longRangeSession.finalGoalX()
            || z != runtime.longRangeSession.finalGoalZ()
            || partial);
        runtime.longRangeSession.onSegmentStarted(
            segmentEnd.position.flooredX(),
            segmentEnd.position.flooredZ(),
            continuationNeeded,
            partial);

        String resultTypeStr = describeResultType(partial, longRangeIntermediateSegment);
        long elapsedMs = System.currentTimeMillis() - startMs;
        int pathLen = processedPath.rawNodes().size();

        if (mc.player != null) {
            fr.riege.ebsl.util.ClientUtils.sendMessage(mc,
                "§ePath result: " + resultTypeStr
                    + "§e | explored: " + exploredCount
                    + " | waypoints: " + pathLen
                    + String.format(" | dist: %.1f blk", processedPath.pathLength())
                    + " | time: " + elapsedMs + "ms",
                false);
            fr.riege.ebsl.util.ClientUtils.sendMessage(mc,
                describeWalkingMessage(partial, longRangeIntermediateSegment, pathLen),
                false);
        }

        runtime.executor.start(
            processedPath.navigationPath(),
            x,
            y,
            z,
            runtime.walkOptions.isPreciseExecution(),
            runtime.state.rotationTarget(),
            runtime.walkOptions.onFinished());
        runtime.walkOptions.applyTo(runtime.executor);
        refreshWalkVisualizer();
    }

    private static String describeResultType(boolean partial, boolean longRangeIntermediateSegment) {
        if (longRangeIntermediateSegment) {
            return partial ? "§ePartial segment" : "§aSegment";
        }
        return partial ? "§ePartial" : "§aFull";
    }

    private static String describeWalkingMessage(boolean partial, boolean longRangeIntermediateSegment, int pathLen) {
        if (longRangeIntermediateSegment) {
            return (partial ? "§e" : "§a")
                + "Long-range segment ready (" + pathLen + " waypoints). Continuing...";
        }
        return (partial ? "§e" : "§a")
            + "Path found (" + pathLen + " waypoints). Walking...";
    }

    private void runPathTest(Minecraft mc, WalkabilityChecker checker,
                             int startX, int startY, int startZ,
                             int targetX, int targetY, int targetZ) {
        long startMs = System.currentTimeMillis();
        PathfinderConfiguration config = PathPipeline.createWalkPathfinderConfiguration(checker, true);
        AStarPathfinder pathfinder = new AStarPathfinder(config);
        PathPosition start = new PathPosition(startX, startY, startZ);
        PathPosition target = new PathPosition(targetX, targetY, targetZ);

        PathfinderResult result;
        try {
            result = pathfinder.findPath(start, target).toCompletableFuture().join();
        } catch (Exception exception) {
            LOGGER.error("Path test calculation failed for target {}, {}, {}", targetX, targetY, targetZ, exception);
            result = null;
        }

        PathPipeline.pushExploredNodesToVisualizer(pathfinder);
        PathfinderResult finalResult = result;
        mc.execute(() -> handlePathTestResult(mc, checker, config, pathfinder, finalResult, startMs));
    }

    private void handlePathTestResult(Minecraft mc, WalkabilityChecker checker,
                                      PathfinderConfiguration config,
                                      AStarPathfinder pathfinder,
                                      PathfinderResult result, long startMs) {
        if (result == null) {
            if (mc.player != null) {
                fr.riege.ebsl.util.ClientUtils.sendMessage(mc, "§cPath test error!", false);
            }
            return;
        }

        String typeStr;
        int pathLen = 0;
        double pathBlocks = 0;
        Collection<PathPosition> positions = result.getPath().collect();

        if (result.successful() || result.hasFallenBack()) {
            typeStr = result.successful() ? "§aFull" : "§ePartial";
            ProcessedPath processedPath = PathPipeline.processWalkPath(positions, config, checker);
            pathLen = processedPath.navigationPath().size();
            pathBlocks = processedPath.pathLength();
            PathVisualizer.setPath(processedPath.navigationPath(), 0);
            PathVisualizer.setCameraPath(Collections.emptyList());
        } else {
            typeStr = "§cNone";
        }

        long elapsedMs = System.currentTimeMillis() - startMs;
        if (mc.player != null) {
            fr.riege.ebsl.util.ClientUtils.sendMessage(mc,
                "§ePath test result: " + typeStr
                    + "§6 | explored: " + pathfinder.getExploredCount()
                    + " | waypoints: " + pathLen
                    + String.format(" | dist: %.1f blk", pathBlocks)
                    + " | time: " + elapsedMs + "ms",
                false);
            fr.riege.ebsl.util.ClientUtils.sendDebugMessage(mc,
                "Path profile: " + pathfinder.getProfilingReport());
        }
    }

    private static void ensurePlayerIsFlying(Minecraft mc) {
        if (mc.player != null && mc.player.getAbilities().mayfly && !mc.player.getAbilities().flying) {
            mc.player.getAbilities().flying = true;
            mc.player.onUpdateAbilities();
        }
    }
}
