package fr.riege.ebsl.pathfinding;

import fr.riege.ebsl.pathfinding.Node;
import fr.riege.ebsl.pathfinding.debug.PathVisualizer;
import fr.riege.ebsl.pathfinding.execution.PathRepairRequest;
import fr.riege.ebsl.pathfinding.goal.Goal;
import fr.riege.ebsl.pathfinding.goal.GoalBlock;
import fr.riege.ebsl.pathfinding.goal.GoalRequestHandlerRegistry;
import fr.riege.ebsl.pathfinding.goal.GoalXZ;
import fr.riege.ebsl.pathfinding.goal.NavigationModeType;
import fr.riege.ebsl.pathfinding.goal.NavigationRequest;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;

/**
 * Orchestrates navigation requests and delegates state, path shaping and walk options
 * to dedicated pathfinding components.
 */
public final class PathfindingManager {
    private static final PathfindingManager INSTANCE = new PathfindingManager();

    private final PathfindingRuntime runtime = new PathfindingRuntime();
    private final WalkNavigationService walkService = new WalkNavigationService(runtime);
    private final LongRangeNavigationController longRangeController =
        new LongRangeNavigationController(runtime, walkService);

    private PathfindingManager() {
    }

    public static void update(Minecraft mc) {
        INSTANCE.tick(mc);
    }

    private void tick(Minecraft mc) {
        if (mc.player == null) {
            return;
        }

        if (runtime.state.shouldRestartWalkPathfind(runtime.executor)) {
            PathRepairRequest repairRequest = runtime.executor.consumeRepairRequest();
            if (repairRequest != null
                && walkService.startPathRepair(mc, repairRequest)) {
                return;
            }
            if (runtime.executor.consumeReplanFromPlayerRequest()
                && longRangeController.replanFromPlayer(mc)) {
                return;
            }
            if (longRangeController.continueReplanning(mc)) {
                return;
            }
            walkService.startPathfind(
                mc,
                runtime.state.goalX(),
                runtime.state.goalY(),
                runtime.state.goalZ(),
                false,
                !runtime.longRangeSession.isActive());
            return;
        }

        if (walkService.updateFlyExecution(mc)) {
            return;
        }

        NavigationMode modeBeforeTick = runtime.state.activeMode();
        runtime.executor.tick(mc);
        boolean longRangeKeepsNavigationAlive = longRangeController.handleProgress(mc, modeBeforeTick);
        if (PathVisualizer.isEnabled() && runtime.state.activeMode() == modeBeforeTick) {
            PathVisualizer.updateExecution(runtime.executor.getWaypointIndex(), runtime.executor.getCamTargetIdx());
        }
        if (runtime.state.activeMode() == modeBeforeTick
            && walkService.isWalkExecutionDone()
            && !longRangeKeepsNavigationAlive) {
            runtime.longRangeSession.clear();
            runtime.state.markIdle();
        }
    }

    public static void startPathfind(Minecraft mc, int x, int y, int z) {
        startGoal(mc, NavigationRequest.builder(new GoalBlock(x, y, z)).build());
    }

    public static void startPathfind(Minecraft mc, int x, int y, int z, boolean fly) {
        startGoal(mc, NavigationRequest.builder(new GoalBlock(x, y, z))
            .mode(fly ? NavigationModeType.FLY : NavigationModeType.WALK)
            .build());
    }

    public static void startFlyPathfind(Minecraft mc, int x, int y, int z) {
        startGoal(mc, NavigationRequest.builder(new GoalBlock(x, y, z))
            .mode(NavigationModeType.FLY)
            .build());
    }

    public static void startPathfindXZ(Minecraft mc, int x, int z) {
        startGoal(mc, NavigationRequest.builder(new GoalXZ(x, z))
            .mode(NavigationModeType.WALK)
            .build());
    }

    public static void startConfiguredWalk(Minecraft mc, int x, int y, int z,
                                           Runnable onFinished, Runnable onFailed,
                                           boolean allowReplan, double preciseGoalTolerance) {
        startGoal(mc, NavigationRequest.builder(new GoalBlock(x, y, z))
            .mode(NavigationModeType.WALK)
            .allowReplan(allowReplan)
            .preciseGoalTolerance(preciseGoalTolerance)
            .onFinished(onFinished)
            .onFailed(onFailed)
            .build());
    }

    public static void startGoal(Minecraft mc, NavigationRequest request) {
        INSTANCE.startGoalInternal(mc, request);
    }

    private void startGoalInternal(Minecraft mc, NavigationRequest request) {
        runtime.walkOptions.reset();
        runtime.walkOptions.configure(
            request.onFinished(),
            request.onFailed(),
            request.allowReplan(),
            request.preciseGoalTolerance());
        GoalRequestHandlerRegistry.start(mc, request);
    }

    public static void startBlockGoal(Minecraft mc, GoalBlock goalBlock, NavigationRequest request) {
        INSTANCE.runtime.longRangeSession.clear();
        INSTANCE.walkService.startPathfind(
            mc,
            goalBlock.x(),
            goalBlock.y(),
            goalBlock.z(),
            request.mode() == NavigationModeType.FLY);
    }

    public static void startXZGoal(Minecraft mc, GoalXZ goalXZ, NavigationRequest request) {
        if (request.mode() != NavigationModeType.WALK) {
            throw new IllegalArgumentException("GoalXZ only supports WALK mode");
        }
        INSTANCE.longRangeController.startPathfindXZ(mc, goalXZ.x(), goalXZ.z());
    }

    /**
     * Runs A* without any movement. Results are shown in the visualizer only.
     */
    public static void startPathTest(Minecraft mc, int x, int y, int z) {
        INSTANCE.walkService.startPathTest(mc, x, y, z);
    }

    public static void startGreenhouseWalk(Minecraft mc, Vec3 target, Runnable onFinished, boolean isFirst) {
        INSTANCE.walkService.startGreenhouseWalk(mc, target, onFinished, isFirst);
    }

    public static void stop() {
        stop(true);
    }

    public static void stop(boolean announce) {
        boolean wasNavigating = INSTANCE.runtime.state.isNavigating();
        Minecraft mc = Minecraft.getInstance();
        INSTANCE.runtime.abortCurrentNavigation(mc);
        PathVisualizer.clear();
        if (announce && wasNavigating && mc.player != null) {
            fr.riege.ebsl.util.ClientUtils.sendMessage(mc, "§eNavigation stopped.", false);
        }
    }

    public static boolean isNavigating() {
        return INSTANCE.runtime.state.isNavigating();
    }

    public static Node.MoveType getCurrentMoveType() {
        return INSTANCE.runtime.executor.getCurrentMoveType();
    }

    public static boolean isWalkSneakLatched() {
        return INSTANCE.runtime.executor.isSneakLatched();
    }

    public static void setWalkSneakLatched(boolean walkSneakLatched) {
        INSTANCE.runtime.executor.setSneakLatched(walkSneakLatched);
    }
}
