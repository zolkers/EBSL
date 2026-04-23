package fr.riege.ebsl.pathfinding;

import fr.riege.ebsl.pathfinding.movement.WalkabilityChecker;
import fr.riege.ebsl.pathfinding.pathfinder.AStarPathfinder;
import fr.riege.ebsl.pathfinding.pathing.configuration.PathfinderConfiguration;
import fr.riege.ebsl.pathfinding.pathing.result.PathfinderResult;
import fr.riege.ebsl.pathfinding.wrapper.PathPosition;
import net.minecraft.client.Minecraft;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;

final class LongRangeSegmentPlanner {
    private static final Logger LOGGER = LoggerFactory.getLogger("ebsl-navigation");

    private final PathfindingRuntime runtime;

    LongRangeSegmentPlanner(PathfindingRuntime runtime) {
        this.runtime = runtime;
    }

    void queueNextSegment(Minecraft mc, boolean startFromPlayer) {
        if (mc.player == null || mc.level == null) {
            return;
        }

        WalkabilityChecker checker = new WalkabilityChecker(mc.level);
        int playerY = PathPipeline.resolveStartY(checker, mc.player.getX(), mc.player.getY(), mc.player.getZ());
        int startX = startFromPlayer
            ? (int) Math.floor(mc.player.getX())
            : runtime.longRangeSession.currentSegmentGoalX();
        int startZ = startFromPlayer
            ? (int) Math.floor(mc.player.getZ())
            : runtime.longRangeSession.currentSegmentGoalZ();
        int startY = startFromPlayer
            ? playerY
            : PathPipeline.resolveGoalYForXZ(checker, startX, playerY, startZ);
        PathPosition start = new PathPosition(startX, startY, startZ);

        double fromX = startFromPlayer ? mc.player.getX() : startX + 0.5;
        double fromZ = startFromPlayer ? mc.player.getZ() : startZ + 0.5;
        LongRangePathSession.SegmentGoal segmentGoal = runtime.longRangeSession.planSegmentGoal(fromX, fromZ);
        int goalY = PathPipeline.resolveGoalYForXZ(checker, segmentGoal.x(), startY, segmentGoal.z());
        PathPosition target = new PathPosition(segmentGoal.x(), goalY, segmentGoal.z());

        PathfinderConfiguration config = segmentGoal.segmented()
            ? PathPipeline.createWalkPathfinderConfiguration(checker, true, 120000, 12000)
            : PathPipeline.createWalkPathfinderConfiguration(checker, true);
        AStarPathfinder pathfinder = new AStarPathfinder(config);
        int requestSegmentId = runtime.longRangeSession.markSegmentCalculationStarted(pathfinder);

        LongRangePathSession.SegmentAttachment attachment = startFromPlayer
            ? LongRangePathSession.SegmentAttachment.REPLACE_FROM_PLAYER
            : LongRangePathSession.SegmentAttachment.MERGE_WITH_CURRENT;
        pathfinder.findPath(start, target).whenComplete((result, throwable) ->
            mc.execute(() -> handleResult(
                mc,
                checker,
                config,
                pathfinder,
                requestSegmentId,
                result,
                throwable,
                attachment,
                segmentGoal.x(),
                goalY,
                segmentGoal.z())));
    }

    private void handleResult(Minecraft mc, WalkabilityChecker checker,
                              PathfinderConfiguration config,
                              AStarPathfinder pathfinder,
                              int requestSegmentId,
                              PathfinderResult result,
                              Throwable throwable,
                              LongRangePathSession.SegmentAttachment attachment,
                              int requestedX,
                              int requestedY,
                              int requestedZ) {
        if (!runtime.longRangeSession.isActive()
            || runtime.state.isAborted()
            || !runtime.longRangeSession.isCurrentCalculation(requestSegmentId)) {
            return;
        }

        if (throwable != null || result == null) {
            if (throwable != null) {
                LOGGER.error("Long-range segment calculation failed", throwable);
            }
            failQueuedSegment(mc, "async error");
            return;
        }

        if (result.getPath() == null) {
            failQueuedSegment(mc, "missing path");
            return;
        }

        Collection<PathPosition> positions = result.getPath().collect();
        if (!PathResultClassifier.hasUsablePath(result, positions)) {
            failQueuedSegment(mc, "unusable result " + result.getPathState());
            return;
        }

        ProcessedPath processedPath = PathPipeline.processWalkPath(positions, config, checker);
        if (processedPath.navigationPath().isEmpty()) {
            failQueuedSegment(mc, "empty processed path");
            return;
        }

        PathPosition last = positions instanceof List<?>
            ? ((List<PathPosition>) positions).getLast()
            : processedPath.rawNodes().getLast().position;
        boolean partial = PathResultClassifier.isPartialWalkResult(
            result, positions, requestedX, requestedY, requestedZ);
        boolean needsContinuation = partial
            || last.flooredX() != runtime.longRangeSession.finalGoalX()
            || last.flooredZ() != runtime.longRangeSession.finalGoalZ();

        runtime.longRangeSession.setPreparedSegment(new LongRangePathSession.PendingSegment(
            processedPath.navigationPath(),
            processedPath.navigationPath().getLast().position.flooredX(),
            processedPath.navigationPath().getLast().position.flooredY(),
            processedPath.navigationPath().getLast().position.flooredZ(),
            needsContinuation,
            partial,
            pathfinder.getExploredCount(),
            attachment
        ));

        fr.riege.ebsl.util.ClientUtils.sendDebugMessage(mc,
            "Queued long-range segment ready: "
                + processedPath.navigationPath().size()
                + " nodes toward XZ "
                + runtime.longRangeSession.finalGoalX() + ", "
                + runtime.longRangeSession.finalGoalZ()
                + " (" + (needsContinuation ? "partial" : "full") + ")");
    }

    private void failQueuedSegment(Minecraft mc, String reason) {
        runtime.longRangeSession.markSegmentCalculationFailed(System.currentTimeMillis());
        fr.riege.ebsl.util.ClientUtils.sendDebugMessage(mc,
            "Long-range segment queue failed, retrying soon: " + reason);
    }
}
