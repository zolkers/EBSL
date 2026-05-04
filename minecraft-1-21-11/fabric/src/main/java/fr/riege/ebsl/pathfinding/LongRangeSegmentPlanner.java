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
        queueNextSegment(mc, startFromPlayer, null);
    }

    void queueNextSegment(Minecraft mc, boolean startFromPlayer, PathPosition horizonStart) {
        if (mc.player == null || mc.level == null) {
            return;
        }

        WalkabilityChecker checker = new WalkabilityChecker(mc.level);
        int playerY = PathPipeline.resolveStartY(checker, mc.player.getX(), mc.player.getY(), mc.player.getZ());

        PathPosition start;
        boolean rollingHorizon = false;
        double fromX, fromZ;

        if (startFromPlayer) {
            int startX = (int) Math.floor(mc.player.getX());
            int startZ = (int) Math.floor(mc.player.getZ());
            start = new PathPosition(startX, playerY, startZ);
            fromX = mc.player.getX();
            fromZ = mc.player.getZ();
        } else if (horizonStart != null) {
            int hx = horizonStart.flooredX();
            int hz = horizonStart.flooredZ();
            int hy = PathPipeline.resolveGoalYForXZ(checker, hx, playerY, hz);
            start = new PathPosition(hx, hy, hz);
            fromX = hx + 0.5;
            fromZ = hz + 0.5;
            rollingHorizon = true;
        } else {
            int startX = runtime.longRangeSession.currentSegmentGoalX();
            int startZ = runtime.longRangeSession.currentSegmentGoalZ();
            int startY = PathPipeline.resolveGoalYForXZ(checker, startX, playerY, startZ);
            start = new PathPosition(startX, startY, startZ);
            fromX = startX + 0.5;
            fromZ = startZ + 0.5;
        }

        LongRangePathSession.SegmentGoal segmentGoal = runtime.longRangeSession.planSegmentGoal(fromX, fromZ);
        int goalY = runtime.longRangeSession.requiresExactY() && !segmentGoal.segmented()
            ? runtime.longRangeSession.finalGoalY()
            : PathPipeline.resolveGoalYForXZ(checker, segmentGoal.x(), playerY, segmentGoal.z());
        PathPosition target = new PathPosition(segmentGoal.x(), goalY, segmentGoal.z());

        PathfinderConfiguration config = PathPipeline.createQueuedLongRangeSegmentConfiguration(checker);
        AStarPathfinder pathfinder = new AStarPathfinder(config);
        int requestSegmentId = runtime.longRangeSession.markSegmentCalculationStarted(pathfinder);

        LongRangePathSession.SegmentAttachment attachment = startFromPlayer
            ? LongRangePathSession.SegmentAttachment.REPLACE_FROM_PLAYER
            : LongRangePathSession.SegmentAttachment.MERGE_WITH_CURRENT;
        boolean rollingHorizonFinal = rollingHorizon;
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
                rollingHorizonFinal,
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
                              boolean rollingHorizon,
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
            || !runtime.longRangeSession.isFinalSegmentGoal(
                last.flooredX(),
                last.flooredY(),
                last.flooredZ());

        runtime.longRangeSession.setPreparedSegment(new LongRangePathSession.PendingSegment(
            processedPath.navigationPath(),
            processedPath.navigationPath().getLast().position.flooredX(),
            processedPath.navigationPath().getLast().position.flooredY(),
            processedPath.navigationPath().getLast().position.flooredZ(),
            needsContinuation,
            partial,
            pathfinder.getExploredCount(),
            attachment,
            rollingHorizon
        ));

        fr.riege.ebsl.util.ClientUtils.sendDebugMessage(mc,
            "Queued long-range segment ready: "
                + processedPath.navigationPath().size()
                + " nodes toward XZ "
                + runtime.longRangeSession.finalGoalX() + ", "
                + runtime.longRangeSession.finalGoalZ()
                + " (" + (needsContinuation ? "partial" : "full") + ")"
                + (rollingHorizon ? " [horizon]" : ""));
    }

    private void failQueuedSegment(Minecraft mc, String reason) {
        runtime.longRangeSession.markSegmentCalculationFailed(System.currentTimeMillis());
        fr.riege.ebsl.util.ClientUtils.sendDebugMessage(mc,
            "Long-range segment queue failed, retrying soon: " + reason);
    }
}
