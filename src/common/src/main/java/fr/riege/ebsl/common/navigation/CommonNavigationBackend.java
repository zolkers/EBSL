package fr.riege.ebsl.common.navigation;

import fr.riege.ebsl.common.layer.IPhysicsLayer;
import fr.riege.ebsl.common.layer.IPlayerLayer;
import fr.riege.ebsl.common.layer.IWorldLayer;
import fr.riege.ebsl.common.math.Vec3d;
import fr.riege.ebsl.common.pathfinding.Node;
import fr.riege.ebsl.common.pathfinding.ProcessedPath;
import fr.riege.ebsl.common.pathfinding.LongRangePathSession;
import fr.riege.ebsl.common.pathfinding.PathResultClassifier;
import fr.riege.ebsl.common.pathfinding.WalkPathProcessor;
import fr.riege.ebsl.common.pathfinding.execution.ExecutionPlan;
import fr.riege.ebsl.common.pathfinding.execution.PathExecutor;
import fr.riege.ebsl.common.pathfinding.execution.PathRepairRequest;
import fr.riege.ebsl.common.pathfinding.movement.WalkabilityChecker;
import fr.riege.ebsl.common.pathfinding.pathfinder.AStarPathfinder;
import fr.riege.ebsl.common.pathfinding.pathing.NeighborStrategies;
import fr.riege.ebsl.common.pathfinding.pathing.configuration.PathfinderConfiguration;
import fr.riege.ebsl.common.pathfinding.pathing.processing.impl.LayerPathProcessor;
import fr.riege.ebsl.common.pathfinding.pathing.result.Path;
import fr.riege.ebsl.common.pathfinding.pathing.result.PathState;
import fr.riege.ebsl.common.pathfinding.pathing.result.PathfinderResult;
import fr.riege.ebsl.common.pathfinding.provider.LayerNavigationPointProvider;
import fr.riege.ebsl.common.pathfinding.settings.PathfinderSettings;
import fr.riege.ebsl.common.pathfinding.wrapper.PathPosition;
import fr.riege.ebsl.common.service.NavigationService;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public final class CommonNavigationBackend implements NavigationService {
    private final IWorldLayer world;
    private final IPlayerLayer player;
    private final IPhysicsLayer physics;
    private final WalkabilityChecker checker;
    private final LayerNavigationPointProvider provider;
    private final PathExecutor executor;
    private final LongRangePathSession longRangeSession = new LongRangePathSession();

    private AStarPathfinder pathfinder;
    private volatile PathfinderResult lastResult;
    private volatile boolean navigating;
    private volatile Node.MoveType currentMoveType = Node.MoveType.WALK;
    private Runnable onFinished;
    private List<Node> activeNodes = List.of();
    private int goalX, goalY, goalZ;
    private boolean executePath;

    public CommonNavigationBackend(IWorldLayer world, IPlayerLayer player, IPhysicsLayer physics) {
        this.world = world;
        this.player = player;
        this.physics = physics;
        this.checker = new WalkabilityChecker(world);
        this.provider = new LayerNavigationPointProvider(checker);
        this.executor = new PathExecutor(world, player, physics);
    }

    @Override public void startBlockGoal(int x, int y, int z) {
        longRangeSession.clear();
        startPathTo(new PathPosition(x, y, z), null, true, true);
    }

    @Override public void startColumnGoal(int x, int z) {
        Vec3d pos = player.position();
        longRangeSession.start(x, z);
        LongRangePathSession.SegmentGoal segmentGoal = longRangeSession.planSegmentGoal(pos.x(), pos.z());
        int y = resolveGoalYForXZ(segmentGoal.x(), (int) Math.floor(pos.y()), segmentGoal.z());
        startPathTo(new PathPosition(segmentGoal.x(), y, segmentGoal.z()), null, true, false);
    }

    @Override public void startPathTest(int x, int y, int z) {
        startPathTo(new PathPosition(x, y, z), null, false, true);
    }

    @Override public void startPathTestXZ(int x, int z) {
        Vec3d pos = player.position();
        startPathTest(x, resolveGoalYForXZ(x, (int) Math.floor(pos.y()), z), z);
    }

    @Override public void stop(boolean announce) {
        navigating = false;
        onFinished = null;
        activeNodes = List.of();
        executePath = false;
        executor.stop();
        longRangeSession.clear();
        if (pathfinder != null) {
            pathfinder.abort();
        }
    }

    @Override public boolean isNavigating() {
        return navigating;
    }

    @Override public Node.MoveType currentMoveType() {
        Node.MoveType moveType = executor.getCurrentMoveType();
        return moveType == null ? currentMoveType : moveType;
    }

    @Override public boolean isWalkSneakLatched() {
        return executor.isSneakLatched();
    }

    @Override public void setWalkSneakLatched(boolean value) {
        executor.setSneakLatched(value);
        physics.setSneak(value);
    }

    @Override public void startGreenhouseWalk(Vec3d target, Runnable onFinished, boolean isFirst) {
        longRangeSession.clear();
        startPathTo(new PathPosition(target.x(), target.y(), target.z()), onFinished, true, true);
    }

    public PathfinderResult lastResult() {
        return lastResult;
    }

    public Collection<PathPosition> lastPathPositions() {
        if (!activeNodes.isEmpty()) {
            return activeNodes.stream().map(node -> node.position).toList();
        }
        Path path = lastResult == null ? null : lastResult.getPath();
        return path == null ? List.of() : path.collect();
    }

    @Override public String pathStatus() {
        if (executor.getState() == PathExecutor.State.REPLANNING) return "replanning";
        if (executor.getState() == PathExecutor.State.WALKING) return "executing";
        PathfinderResult result = lastResult;
        if (navigating && result == null) return "calculating";
        if (result == null) return "idle";
        if (result.hasFailed()) return "failed";
        if (hasUsablePath(result)) return navigating ? "executing" : "found";
        return result.getPathState().name().toLowerCase();
    }

    @Override public int lastPathNodeCount() {
        return lastPathPositions().size();
    }

    @Override public void tick() {
        if (!navigating) return;
        if (!player.isAlive()) {
            stop(false);
            return;
        }

        if (executor.getState() == PathExecutor.State.WALKING || executor.getState() == PathExecutor.State.REPLANNING) {
            executor.tick();
            Node.MoveType moveType = executor.getCurrentMoveType();
            if (moveType != null) currentMoveType = moveType;
            if (executor.getState() == PathExecutor.State.FINISHED || executor.getState() == PathExecutor.State.FAILED) {
                if (!handleLongRangeProgress(true)) {
                    markIdle(false);
                }
            } else if (executor.getState() == PathExecutor.State.REPLANNING) {
                handleExecutorReplan();
            } else {
                handleLongRangeProgress(false);
            }
            return;
        }

        PathfinderResult result = lastResult;
        if (!hasUsablePath(result)) {
            return;
        }
        if (!executePath) {
            markIdle(true);
            return;
        }
        if (!activeNodes.isEmpty()) {
            startExecutor(activeNodes, goalX, goalY, goalZ, true);
        }
    }

    private void handleExecutorReplan() {
        PathRepairRequest repairRequest = executor.consumeRepairRequest();
        if (repairRequest != null) {
            startPathRepair(repairRequest);
            return;
        }
        if (executor.consumeReplanFromPlayerRequest() && longRangeSession.isActive()) {
            longRangeSession.forceNextCalculationFromPlayer();
            queueNextLongRangeSegment(true, null);
            return;
        }
        if (longRangeSession.hasPreparedSegment()) {
            startPreparedLongRangeSegment();
            return;
        }
        if (!longRangeSession.hasSegmentCalculationInFlight()) {
            startPathTo(new PathPosition(goalX, goalY, goalZ), onFinished, true, !longRangeSession.isActive());
        }
    }

    private void markIdle(boolean releaseInputs) {
        navigating = false;
        activeNodes = List.of();
        executePath = false;
        if (releaseInputs) {
            physics.clearInputs();
        }
        Runnable finished = onFinished;
        onFinished = null;
        if (finished != null) {
            finished.run();
        }
    }

    private void startPathTo(PathPosition target, Runnable onFinished, boolean executePath, boolean clearLongRange) {
        checker.clearCache();
        if (clearLongRange) {
            longRangeSession.clear();
        }
        this.onFinished = onFinished;
        this.activeNodes = List.of();
        this.executePath = executePath;
        this.lastResult = null;
        navigating = true;
        currentMoveType = Node.MoveType.WALK;

        Vec3d pos = player.position();
        PathPosition start = new PathPosition(
            Math.floor(pos.x()),
            resolveStartY(pos.x(), pos.y(), pos.z()),
            Math.floor(pos.z()));
        PathPosition effectiveTarget = resolveTarget(target);
        goalX = effectiveTarget.flooredX();
        goalY = effectiveTarget.flooredY();
        goalZ = effectiveTarget.flooredZ();
        PathfinderConfiguration config = instantConfiguration();
        pathfinder = new AStarPathfinder(config);

        CompletableFuture
            .supplyAsync(() -> pathfinder.findPath(start, effectiveTarget).toCompletableFuture().join())
            .whenComplete((result, throwable) -> {
                if (throwable != null) {
                    startFullPathTo(start, effectiveTarget, executePath);
                    return;
                }
                Collection<PathPosition> positions = result != null && result.getPath() != null
                    ? result.getPath().collect()
                    : List.of();
                if (PathResultClassifier.classifyWalkResult(result, positions, goalX, goalY, goalZ)
                    == PathResultClassifier.PathAvailability.FAILED) {
                    startFullPathTo(start, effectiveTarget, executePath);
                    return;
                }
                handleWalkResult(result, config, executePath);
            });
    }

    private void startFullPathTo(PathPosition start, PathPosition effectiveTarget, boolean executePath) {
        PathfinderConfiguration config = fullConfiguration();
        pathfinder = new AStarPathfinder(config);
        CompletableFuture
            .supplyAsync(() -> pathfinder.findPath(start, effectiveTarget).toCompletableFuture().join())
            .whenComplete((result, throwable) -> {
                if (throwable != null) {
                    navigating = false;
                    lastResult = null;
                    physics.clearInputs();
                    return;
                }
                handleWalkResult(result, config, executePath);
            });
    }

    private void handleWalkResult(PathfinderResult result, PathfinderConfiguration config, boolean executePath) {
        lastResult = result;
        Collection<PathPosition> positions = result != null && result.getPath() != null
            ? result.getPath().collect()
            : List.of();
        if (!PathResultClassifier.hasUsablePath(result, positions)) {
            navigating = false;
            physics.clearInputs();
            return;
        }

        ProcessedPath processed = WalkPathProcessor.process(positions, config, checker);
        activeNodes = processed.navigationPath();
        boolean partial = PathResultClassifier.isPartialWalkResult(result, positions, goalX, goalY, goalZ);
        if (partial && !longRangeSession.isActive()) {
            longRangeSession.startBlockGoal(goalX, goalY, goalZ);
        }
        if (longRangeSession.isActive() && !activeNodes.isEmpty()) {
            Node segmentEnd = activeNodes.getLast();
            boolean continuationNeeded = partial
                || !longRangeSession.isFinalSegmentGoal(
                    segmentEnd.position.flooredX(),
                    segmentEnd.position.flooredY(),
                    segmentEnd.position.flooredZ());
            longRangeSession.onSegmentStarted(
                segmentEnd.position.flooredX(),
                segmentEnd.position.flooredZ(),
                continuationNeeded,
                partial);
        }
        if (executePath) {
            startExecutor(activeNodes, goalX, goalY, goalZ, !longRangeSession.shouldKeepNavigationAlive());
        }
    }

    private void startPathRepair(PathRepairRequest request) {
        if (request == null || request.joinNode() == null) {
            startPathTo(new PathPosition(goalX, goalY, goalZ), onFinished, true, !longRangeSession.isActive());
            return;
        }
        checker.clearCache();
        Vec3d pos = player.position();
        PathPosition start = new PathPosition(Math.floor(pos.x()), resolveStartY(pos.x(), pos.y(), pos.z()), Math.floor(pos.z()));
        PathfinderConfiguration config = repairConfiguration();
        AStarPathfinder repairPathfinder = new AStarPathfinder(config);
        pathfinder = repairPathfinder;
        CompletableFuture
            .supplyAsync(() -> repairPathfinder.findPath(start, request.joinNode().position).toCompletableFuture().join())
            .whenComplete((result, throwable) -> {
                Collection<PathPosition> positions = result != null && result.getPath() != null
                    ? result.getPath().collect()
                    : List.of();
                if (throwable != null || !PathResultClassifier.hasUsablePath(result, positions)) {
                    startPathTo(new PathPosition(goalX, goalY, goalZ), onFinished, true, !longRangeSession.isActive());
                    return;
                }
                ProcessedPath repairPath = WalkPathProcessor.process(positions, config, checker);
                List<Node> merged = mergePathPrefixWithTail(repairPath.navigationPath(), request.remainingPath());
                if (merged.size() < 2) {
                    startPathTo(new PathPosition(goalX, goalY, goalZ), onFinished, true, !longRangeSession.isActive());
                    return;
                }
                activeNodes = merged;
                startExecutor(merged, request.goalX(), request.goalY(), request.goalZ(), true);
                executor.rememberRecentRepair(request.reason());
            });
    }

    private void startExecutor(List<Node> path, int goalX, int goalY, int goalZ, boolean finishAtPathEnd) {
        if (path == null || path.isEmpty()) {
            navigating = false;
            return;
        }
        Runnable segmentFinished = finishAtPathEnd ? onFinished : null;
        executor.start(new ExecutionPlan(path, goalX, goalY, goalZ, false, segmentFinished,
            fr.riege.ebsl.common.pathfinding.execution.ExecutionOptions.defaults(), finishAtPathEnd));
    }

    private boolean handleLongRangeProgress(boolean walkExecutionDone) {
        if (!longRangeSession.isActive()) {
            return false;
        }
        Vec3d pos = player.position();
        if (longRangeSession.isFinalGoalReached(pos)) {
            Runnable finished = onFinished;
            onFinished = null;
            longRangeSession.clear();
            navigating = false;
            activeNodes = List.of();
            executePath = false;
            executor.stop();
            if (finished != null) finished.run();
            return false;
        }
        double progressRatio = executor.getProgressRatio(pos);
        double remainingDistance = executor.getRemainingDistance(pos);
        LongRangePathSession.SegmentQueueDecision decision = longRangeSession.queueDecision(
            progressRatio, remainingDistance, walkExecutionDone, System.currentTimeMillis());
        if (decision != LongRangePathSession.SegmentQueueDecision.NONE) {
            boolean startFromPlayer = decision == LongRangePathSession.SegmentQueueDecision.EMERGENCY_FROM_PLAYER
                || longRangeSession.shouldUsePlayerRecoveryStart(progressRatio, walkExecutionDone);
            PathPosition horizon = null;
            if (!startFromPlayer) {
                Node horizonNode = executor.getNodeAtRatio(PathfinderSettings.instance().horizonTrimRatio.value());
                horizon = horizonNode == null ? null : horizonNode.position;
            }
            queueNextLongRangeSegment(startFromPlayer, horizon);
        }
        if (longRangeSession.hasPreparedSegment()
            && longRangeSession.shouldActivatePreparedSegment(progressRatio, remainingDistance, walkExecutionDone)) {
            startPreparedLongRangeSegment();
        }
        return longRangeSession.shouldKeepNavigationAlive();
    }

    private void queueNextLongRangeSegment(boolean startFromPlayer, PathPosition horizonStart) {
        Vec3d pos = player.position();
        int playerY = resolveStartY(pos.x(), pos.y(), pos.z());
        PathPosition start;
        boolean rollingHorizon = false;
        double fromX;
        double fromZ;
        if (startFromPlayer) {
            start = new PathPosition(Math.floor(pos.x()), playerY, Math.floor(pos.z()));
            fromX = pos.x();
            fromZ = pos.z();
        } else if (horizonStart != null) {
            int hx = horizonStart.flooredX();
            int hz = horizonStart.flooredZ();
            int hy = resolveGoalYForXZ(hx, playerY, hz);
            start = new PathPosition(hx, hy, hz);
            fromX = hx + 0.5;
            fromZ = hz + 0.5;
            rollingHorizon = true;
        } else {
            int sx = longRangeSession.currentSegmentGoalX();
            int sz = longRangeSession.currentSegmentGoalZ();
            int sy = resolveGoalYForXZ(sx, playerY, sz);
            start = new PathPosition(sx, sy, sz);
            fromX = sx + 0.5;
            fromZ = sz + 0.5;
        }
        LongRangePathSession.SegmentGoal segmentGoal = longRangeSession.planSegmentGoal(fromX, fromZ);
        int gy = longRangeSession.requiresExactY() && !segmentGoal.segmented()
            ? longRangeSession.finalGoalY()
            : resolveGoalYForXZ(segmentGoal.x(), playerY, segmentGoal.z());
        PathPosition target = new PathPosition(segmentGoal.x(), gy, segmentGoal.z());
        PathfinderConfiguration config = queuedConfiguration();
        AStarPathfinder queuedPathfinder = new AStarPathfinder(config);
        int requestId = longRangeSession.markSegmentCalculationStarted(queuedPathfinder);
        LongRangePathSession.SegmentAttachment attachment = startFromPlayer
            ? LongRangePathSession.SegmentAttachment.REPLACE_FROM_PLAYER
            : LongRangePathSession.SegmentAttachment.MERGE_WITH_CURRENT;
        boolean finalRollingHorizon = rollingHorizon;
        CompletableFuture
            .supplyAsync(() -> queuedPathfinder.findPath(start, target).toCompletableFuture().join())
            .whenComplete((result, throwable) -> handleQueuedLongRangeResult(
                requestId, result, throwable, config, attachment, finalRollingHorizon, segmentGoal.x(), gy, segmentGoal.z()));
    }

    private void handleQueuedLongRangeResult(int requestId, PathfinderResult result, Throwable throwable,
                                             PathfinderConfiguration config,
                                             LongRangePathSession.SegmentAttachment attachment,
                                             boolean rollingHorizon,
                                             int requestedX, int requestedY, int requestedZ) {
        if (!longRangeSession.isActive() || !longRangeSession.isCurrentCalculation(requestId)) {
            return;
        }
        Collection<PathPosition> positions = result != null && result.getPath() != null
            ? result.getPath().collect()
            : List.of();
        if (throwable != null || !PathResultClassifier.hasUsablePath(result, positions)) {
            longRangeSession.markSegmentCalculationFailed(System.currentTimeMillis());
            return;
        }
        ProcessedPath processed = WalkPathProcessor.process(positions, config, checker);
        if (processed.navigationPath().isEmpty()) {
            longRangeSession.markSegmentCalculationFailed(System.currentTimeMillis());
            return;
        }
        PathPosition last = positions instanceof List<PathPosition> list ? list.getLast() : processed.rawNodes().getLast().position;
        boolean partial = PathResultClassifier.isPartialWalkResult(result, positions, requestedX, requestedY, requestedZ);
        boolean needsContinuation = partial || !longRangeSession.isFinalSegmentGoal(last.flooredX(), last.flooredY(), last.flooredZ());
        Node end = processed.navigationPath().getLast();
        longRangeSession.setPreparedSegment(new LongRangePathSession.PendingSegment(
            processed.navigationPath(),
            end.position.flooredX(),
            end.position.flooredY(),
            end.position.flooredZ(),
            needsContinuation,
            partial,
            0,
            attachment,
            rollingHorizon));
    }

    private void startPreparedLongRangeSegment() {
        LongRangePathSession.PendingSegment pending = longRangeSession.preparedSegment();
        if (pending == null) {
            return;
        }
        goalX = pending.goalX();
        goalY = pending.goalY();
        goalZ = pending.goalZ();
        if (executor.getState() == PathExecutor.State.WALKING
            && pending.attachment() == LongRangePathSession.SegmentAttachment.MERGE_WITH_CURRENT) {
            if (pending.rollingHorizon()) {
                executor.trimAndContinueWith(PathfinderSettings.instance().horizonTrimRatio.value(), pending.path(), goalX, goalY, goalZ);
            } else {
                executor.continueWith(pending.path(), goalX, goalY, goalZ);
            }
        } else {
            startExecutor(pending.path(), goalX, goalY, goalZ, !pending.needsContinuation());
        }
        activeNodes = executor.getPathSnapshot();
        longRangeSession.onSegmentStarted(pending.goalX(), pending.goalZ(), pending.needsContinuation(), pending.partial());
    }

    private PathPosition resolveTarget(PathPosition target) {
        int x = target.flooredX();
        int y = target.flooredY();
        int z = target.flooredZ();
        if (checker.isSolid(x, y, z)) {
            return new PathPosition(x, y + 1, z);
        }
        return target;
    }

    private int resolveStartY(double playerX, double playerY, double playerZ) {
        int x = (int) Math.floor(playerX);
        int y = (int) Math.floor(playerY);
        int z = (int) Math.floor(playerZ);
        if (checker.isPassable(x, y, z)) return y;
        if (checker.isPassable(x, y + 1, z)) return y + 1;
        return (int) Math.ceil(playerY);
    }

    private int resolveGoalYForXZ(int x, int preferredY, int z) {
        for (int offset = 3; offset >= -4; offset--) {
            int candidateY = preferredY + offset;
            if (checker.isWalkable(x, candidateY, z)) return candidateY;
        }
        if (checker.isPassable(x, preferredY, z) && checker.isPassable(x, preferredY + 1, z)) return preferredY;
        if (checker.isSolid(x, preferredY, z)) return preferredY + 1;
        return preferredY;
    }

    private static boolean hasUsablePath(PathfinderResult result) {
        return result != null
            && result.getPath() != null
            && (result.successful() || result.hasFallenBack())
            && result.getPath().collect() != null
            && !result.getPath().collect().isEmpty()
            && result.getPathState() != PathState.ABORTED;
    }

    private PathfinderConfiguration instantConfiguration() {
        return configuration(PathfinderSettings.instance().instantWalkMaxIterations.value(), PathfinderSettings.instance().instantWalkMaxLength.value());
    }

    private PathfinderConfiguration fullConfiguration() {
        return configuration(PathfinderSettings.instance().defaultWalkMaxIterations.value(), PathfinderSettings.instance().defaultWalkMaxLength.value());
    }

    private PathfinderConfiguration repairConfiguration() {
        return configuration(PathfinderSettings.instance().repairWalkMaxIterations.value(), PathfinderSettings.instance().repairWalkMaxLength.value());
    }

    private PathfinderConfiguration queuedConfiguration() {
        return configuration(PathfinderSettings.instance().queuedLongRangeMaxIterations.value(), PathfinderSettings.instance().queuedLongRangeMaxLength.value());
    }

    private PathfinderConfiguration configuration(int maxIterations, int maxLength) {
        return PathfinderConfiguration.builder()
            .maxIterations(maxIterations)
            .maxLength(maxLength)
            .provider(provider)
            .processors(List.of(new LayerPathProcessor()))
            .neighborStrategy(NeighborStrategies.horizontalDiagonalAndVertical(
                PathfinderSettings.instance().maxJumpHeight.value(), true, true, true, true))
            .fallback(true)
            .build();
    }

    private static List<Node> mergePathPrefixWithTail(List<Node> prefix, List<Node> tail) {
        java.util.ArrayList<Node> merged = new java.util.ArrayList<>();
        appendDistinct(merged, prefix);
        appendDistinct(merged, tail);
        return merged;
    }

    private static void appendDistinct(List<Node> merged, List<Node> candidates) {
        if (candidates == null) return;
        for (Node candidate : candidates) {
            if (candidate == null) continue;
            if (!merged.isEmpty() && merged.getLast().position.equals(candidate.position)) continue;
            merged.add(candidate);
        }
    }
}
