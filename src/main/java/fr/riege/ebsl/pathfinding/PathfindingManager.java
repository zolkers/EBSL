package fr.riege.ebsl.pathfinding;

import fr.riege.ebsl.pathfinding.debug.PathVisualizer;
import fr.riege.ebsl.pathfinding.execution.FlyExecutor;
import fr.riege.ebsl.pathfinding.execution.PathExecutor;
import fr.riege.ebsl.pathfinding.movement.WalkabilityChecker;
import fr.riege.ebsl.pathfinding.pathfinder.AStarPathfinder;
import fr.riege.ebsl.pathfinding.pathing.configuration.PathfinderConfiguration;
import fr.riege.ebsl.pathfinding.pathing.result.PathState;
import fr.riege.ebsl.pathfinding.pathing.result.PathfinderResult;
import fr.riege.ebsl.pathfinding.wrapper.PathPosition;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Orchestrates navigation requests and delegates state, path shaping and walk options
 * to dedicated pathfinding components.
 */
public final class PathfindingManager {

    private static final PathExecutor EXECUTOR = new PathExecutor();
    private static final FlyExecutor FLY_EXECUTOR = new FlyExecutor();
    private static final NavigationState STATE = new NavigationState();
    private static final WalkExecutionOptions WALK_OPTIONS = new WalkExecutionOptions();

    private PathfindingManager() {
    }

    public static void update(Minecraft mc) {
        if (mc.player == null) {
            return;
        }

        PathVisualizer.captureCamera(mc);

        if (STATE.shouldRestartWalkPathfind(EXECUTOR)) {
            doStartPathfind(mc, STATE.goalX(), STATE.goalY(), STATE.goalZ(), false);
            return;
        }

        if (STATE.isFlyExecutionActive(FLY_EXECUTOR)) {
            ensurePlayerIsFlying(mc);
            FLY_EXECUTOR.tick(mc);
            if (FLY_EXECUTOR.getState() == FlyExecutor.State.FINISHED) {
                STATE.markIdle();
            }
            return;
        }

        NavigationMode modeBeforeTick = STATE.activeMode();
        EXECUTOR.tick(mc);
        if (PathVisualizer.isEnabled() && STATE.activeMode() == modeBeforeTick) {
            PathVisualizer.updateExecution(EXECUTOR.getWaypointIndex(), EXECUTOR.getCamTargetIdx());
        }
        if (STATE.activeMode() == modeBeforeTick && isWalkExecutionDone()) {
            STATE.markIdle();
        }
    }

    public static void startPathfind(Minecraft mc, int x, int y, int z) {
        WALK_OPTIONS.reset();
        STATE.setRotationTarget(null);
        doStartPathfind(mc, x, y, z, false);
    }

    public static void startPathfind(Minecraft mc, int x, int y, int z, boolean fly) {
        WALK_OPTIONS.reset();
        STATE.setRotationTarget(null);
        startPathfind(mc, x, y, z, fly, null);
    }

    public static void startPathfind(Minecraft mc, int x, int y, int z, boolean fly, Entity rotTarget) {
        WALK_OPTIONS.reset();
        STATE.setRotationTarget(rotTarget);
        doStartPathfind(mc, x, y, z, fly);
    }

    public static void startFlyPathfind(Minecraft mc, int x, int y, int z) {
        WALK_OPTIONS.reset();
        STATE.setRotationTarget(null);
        doStartPathfind(mc, x, y, z, true);
    }

    public static void startConfiguredWalk(Minecraft mc, int x, int y, int z,
                                           Runnable onFinished, Runnable onFailed,
                                           boolean allowReplan, double preciseGoalTolerance) {
        WALK_OPTIONS.reset();
        WALK_OPTIONS.configure(null, onFinished, onFailed, allowReplan, preciseGoalTolerance);
        STATE.setRotationTarget(null);
        doStartPathfind(mc, x, y, z, false);
    }

    /**
     * Runs A* without any movement. Results are shown in the visualizer only.
     */
    public static void startPathTest(Minecraft mc, int x, int y, int z) {
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

    public static void startGreenhouseWalk(Minecraft mc, Vec3 target, Runnable onFinished, boolean isFirst) {
        if (mc.player == null) {
            return;
        }

        int tx = (int) Math.floor(target.x);
        int ty = (int) Math.floor(target.y);
        int tz = (int) Math.floor(target.z);
        double centerVariationX = (Math.random() * 0.4) - 0.2;
        double centerVariationZ = (Math.random() * 0.4) - 0.2;

        WALK_OPTIONS.reset();
        WALK_OPTIONS.configure(target.add(0, -10.0, 0), onFinished, null, !isFirst, 0.1);
        WALK_OPTIONS.setGoalCenterOffsets(0.5 + centerVariationX, 0.5 + centerVariationZ);
        WALK_OPTIONS.setAllowRotation(false);
        WALK_OPTIONS.setAllowReplan(false);
        WALK_OPTIONS.setAllowJumps(false);
        WALK_OPTIONS.setExactGoalCentering(true);

        if (isFirst) {
            STATE.setRotationTarget(null);
            doStartPathfind(mc, tx, ty, tz, false);
            return;
        }

        if (STATE.isNavigating()) {
            abortCurrentNavigation(mc);
        }

        STATE.begin(NavigationMode.WALK, tx, ty, tz);

        PathPosition start = new PathPosition(mc.player.getX(), mc.player.getY(), mc.player.getZ());
        PathPosition targetPos = new PathPosition(target.x, target.y, target.z);
        List<Node> directPath = PathPipeline.buildLinearWalkPath(start, targetPos);

        PathVisualizer.setPath(directPath, 0);
        EXECUTOR.start(directPath, tx, ty, tz, true, null, onFinished);
        WALK_OPTIONS.applyTo(EXECUTOR);

        fr.riege.ebsl.util.ClientUtils.sendMessage(mc, "§7Greenhouse path: direct walk to target.", false);
    }

    public static void stop() {
        stop(true);
    }

    public static void stop(boolean announce) {
        boolean wasNavigating = STATE.isNavigating();
        Minecraft mc = Minecraft.getInstance();
        abortCurrentNavigation(mc);
        PathVisualizer.clear();
        if (announce && wasNavigating && mc.player != null) {
            fr.riege.ebsl.util.ClientUtils.sendMessage(mc, "§eNavigation stopped.", false);
        }
    }

    public static boolean isNavigating() {
        return STATE.isNavigating();
    }

    public static boolean isWalkSneakLatched() {
        WALK_OPTIONS.setSneakLatched(EXECUTOR.isSneakLatched());
        return WALK_OPTIONS.isSneakLatched();
    }

    public static void setWalkSneakLatched(boolean walkSneakLatched) {
        WALK_OPTIONS.setSneakLatched(walkSneakLatched);
        EXECUTOR.setSneakLatched(walkSneakLatched);
    }

    private static void runPathTest(Minecraft mc, WalkabilityChecker checker,
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
            result = null;
        }

        PathPipeline.pushExploredNodesToVisualizer(pathfinder);

        PathfinderResult finalResult = result;
        mc.execute(() -> handlePathTestResult(mc, checker, config, pathfinder, finalResult, startMs));
    }

    private static void handlePathTestResult(Minecraft mc, WalkabilityChecker checker,
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

    private static void abortCurrentNavigation(Minecraft mc) {
        STATE.abortCurrentNavigation(mc, EXECUTOR, FLY_EXECUTOR);
    }

    private static void doStartPathfind(Minecraft mc, int x, int y, int z, boolean fly) {
        if (mc.player == null) {
            return;
        }

        if (STATE.isNavigating()) {
            abortCurrentNavigation(mc);
        }

        WalkabilityChecker checker = !fly && mc.level != null ? new WalkabilityChecker(mc.level) : null;
        int finalY = checker != null && checker.isSolid(x, y, z) ? y + 1 : y;

        STATE.begin(fly ? NavigationMode.FLY : NavigationMode.WALK, x, finalY, z);
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

    private static void startFlyNavigation(Minecraft mc, PathPosition start, PathPosition target,
                                           int x, int y, int z) {
        List<Node> rawNodes = PathPipeline.buildLinearFlyPath(start, target);
        List<Node> smoothed = PathPipeline.smoothFlyPath(mc, rawNodes);

        if (smoothed.isEmpty()) {
            STATE.markIdle();
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
        FLY_EXECUTOR.start(smoothed, x, y, z);
    }

    private static void startWalkNavigation(Minecraft mc, WalkabilityChecker checker,
                                            PathPosition start, PathPosition target,
                                            int x, int y, int z) {
        PathfinderConfiguration config = PathPipeline.createWalkPathfinderConfiguration(checker, true);
        AStarPathfinder pathfinder = new AStarPathfinder(config);
        STATE.setCurrentPathfinder(pathfinder);
        long startMs = System.currentTimeMillis();

        pathfinder.findPath(start, target)
            .thenAccept(result -> mc.execute(() -> {
                if (STATE.isAborted()) {
                    return;
                }
                handleWalkResult(mc, result, config, checker, x, y, z, startMs, pathfinder.getExploredCount());
            }));
    }

    private static void handleWalkResult(Minecraft mc, PathfinderResult result,
                                         PathfinderConfiguration config, WalkabilityChecker checker,
                                         int x, int y, int z,
                                         long startMs, long exploredCount) {
        STATE.clearCurrentPathfinder();

        Collection<PathPosition> positions = result.getPath().collect();
        boolean hasPath = result.successful() || result.hasFallenBack();

        if (!hasPath || positions.isEmpty()) {
            STATE.markIdle();
            if (mc.player != null) {
                fr.riege.ebsl.util.ClientUtils.sendMessage(mc, "§cNo path found!", false);
            }
            if (WALK_OPTIONS.onFailed() != null) {
                WALK_OPTIONS.onFailed().run();
            }
            return;
        }

        if (result.getPathState() == PathState.ABORTED) {
            STATE.markIdle();
            return;
        }

        ProcessedPath processedPath = PathPipeline.processWalkPath(positions, config, checker);
        PathVisualizer.setPath(processedPath.navigationPath(), 0);
        PathVisualizer.setCameraPath(Collections.emptyList());

        String resultTypeStr = result.successful() ? "§aFull" : "§ePartial";
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
                (result.successful() ? "§a" : "§e")
                    + "Path found (" + pathLen + " waypoints). Walking...",
                false);
        }

        EXECUTOR.start(processedPath.navigationPath(), x, y, z, WALK_OPTIONS.isPreciseExecution(),
            STATE.rotationTarget(), WALK_OPTIONS.onFinished());
        WALK_OPTIONS.applyTo(EXECUTOR);

        PathVisualizer.setCameraPath(EXECUTOR.getCameraPath());
        PathVisualizer.updateExecution(EXECUTOR.getWaypointIndex(), EXECUTOR.getCamTargetIdx());
    }

    private static boolean isWalkExecutionDone() {
        PathExecutor.State state = EXECUTOR.getState();
        return state == PathExecutor.State.FINISHED || state == PathExecutor.State.FAILED;
    }

    private static void ensurePlayerIsFlying(Minecraft mc) {
        if (mc.player != null && mc.player.getAbilities().mayfly && !mc.player.getAbilities().flying) {
            mc.player.getAbilities().flying = true;
            mc.player.onUpdateAbilities();
        }
    }
}
