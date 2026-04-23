package fr.riege.ebsl.pathfinding;

import fr.riege.ebsl.pathfinding.debug.PathVisualizer;
import fr.riege.ebsl.pathfinding.movement.WalkabilityChecker;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;

final class LongRangeNavigationController {
    private final PathfindingRuntime runtime;
    private final WalkNavigationService walkNavigationService;
    private final LongRangeSegmentPlanner segmentPlanner;

    LongRangeNavigationController(PathfindingRuntime runtime, WalkNavigationService walkNavigationService) {
        this.runtime = runtime;
        this.walkNavigationService = walkNavigationService;
        this.segmentPlanner = new LongRangeSegmentPlanner(runtime);
    }

    void startPathfindXZ(Minecraft mc, int x, int z) {
        runtime.walkOptions.reset();
        runtime.state.setRotationTarget(null);
        runtime.longRangeSession.start(x, z);

        if (mc.player == null || mc.level == null) {
            return;
        }

        if (runtime.state.isNavigating()) {
            runtime.abortCurrentNavigation(mc);
            runtime.longRangeSession.start(x, z);
        }

        WalkabilityChecker checker = new WalkabilityChecker(mc.level);
        int startY = PathPipeline.resolveStartY(checker, mc.player.getX(), mc.player.getY(), mc.player.getZ());
        LongRangePathSession.SegmentGoal segmentGoal = runtime.longRangeSession.planSegmentGoal(mc.player.getX(), mc.player.getZ());
        int goalY = PathPipeline.resolveGoalYForXZ(checker, segmentGoal.x(), startY, segmentGoal.z());
        walkNavigationService.startPathfind(mc, segmentGoal.x(), goalY, segmentGoal.z(), false, false);
    }

    boolean handleProgress(Minecraft mc, NavigationMode modeBeforeTick) {
        if (mc.player == null
            || !runtime.longRangeSession.isActive()
            || runtime.state.activeMode() != NavigationMode.WALK
            || modeBeforeTick != NavigationMode.WALK) {
            return false;
        }

        Vec3 playerPos = mc.player.position();
        if (runtime.longRangeSession.isFinalGoalReached(playerPos)) {
            fr.riege.ebsl.util.ClientUtils.sendMessage(mc,
                "§aLong-range goal reached at XZ "
                    + runtime.longRangeSession.finalGoalX()
                    + ", "
                    + runtime.longRangeSession.finalGoalZ() + ".", false);
            runtime.abortCurrentNavigation(mc);
            PathVisualizer.clear();
            if (runtime.walkOptions.onFinished() != null) {
                runtime.walkOptions.onFinished().run();
            }
            return false;
        }

        boolean walkExecutionDone = walkNavigationService.isWalkExecutionDone();
        double progressRatio = runtime.executor.getProgressRatio(playerPos);
        double remainingDistance = runtime.executor.getRemainingDistance(playerPos);
        LongRangePathSession.SegmentQueueDecision queueDecision = runtime.longRangeSession.queueDecision(
            progressRatio,
            remainingDistance,
            walkExecutionDone,
            System.currentTimeMillis());
        if (queueDecision != LongRangePathSession.SegmentQueueDecision.NONE) {
            segmentPlanner.queueNextSegment(mc,
                queueDecision == LongRangePathSession.SegmentQueueDecision.EMERGENCY_FROM_PLAYER
                    || runtime.longRangeSession.shouldUsePlayerRecoveryStart(progressRatio, walkExecutionDone));
        }

        if (runtime.longRangeSession.hasPreparedSegment()
            && ((progressRatio >= runtime.longRangeSession.recalcThresholdRatio()
                && runtime.executor.getState() == fr.riege.ebsl.pathfinding.execution.PathExecutor.State.WALKING)
                || walkExecutionDone)) {
            startPreparedSegment(mc);
        }
        return runtime.longRangeSession.shouldKeepNavigationAlive();
    }

    boolean replanFromPlayer(Minecraft mc) {
        if (mc.player == null || !runtime.longRangeSession.isActive()) {
            return false;
        }
        runtime.longRangeSession.forceNextCalculationFromPlayer();
        segmentPlanner.queueNextSegment(mc, true);
        return true;
    }

    private void startPreparedSegment(Minecraft mc) {
        LongRangePathSession.PendingSegment pendingSegment = runtime.longRangeSession.preparedSegment();
        if (pendingSegment == null) {
            return;
        }

        walkNavigationService.startPreparedWalkSegment(pendingSegment);
        runtime.longRangeSession.onSegmentStarted(
            pendingSegment.goalX(),
            pendingSegment.goalZ(),
            pendingSegment.needsContinuation(),
            pendingSegment.partial());

        if (mc.player != null) {
            fr.riege.ebsl.util.ClientUtils.sendMessage(mc,
                "§7Switching to next long-range segment toward XZ "
                    + runtime.longRangeSession.finalGoalX() + ", "
                    + runtime.longRangeSession.finalGoalZ()
                    + " (" + pendingSegment.exploredCount() + " explored).",
                false);
        }
    }
}
