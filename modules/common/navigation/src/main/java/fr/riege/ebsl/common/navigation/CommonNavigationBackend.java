/*
 * This file is part of EBSL.
 *
 * EBSL is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * EBSL is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package fr.riege.ebsl.common.navigation;

import fr.riege.ebsl.common.math.Vec3d;
import fr.riege.ebsl.common.pathfinding.*;
import fr.riege.ebsl.common.pathfinding.execution.ExecutionOptions;
import fr.riege.ebsl.common.pathfinding.execution.ExecutionPlan;
import fr.riege.ebsl.common.pathfinding.execution.PathExecutor;
import fr.riege.ebsl.common.pathfinding.execution.PathRepairRequest;
import fr.riege.ebsl.common.pathfinding.goal.NavigationRequest;
import fr.riege.ebsl.common.pathfinding.goal.NavigationTarget;
import fr.riege.ebsl.common.pathfinding.movement.MovementTerrain;
import fr.riege.ebsl.common.pathfinding.movement.WalkabilityChecker;
import fr.riege.ebsl.common.pathfinding.pathfinder.Pathfinders;
import fr.riege.ebsl.common.pathfinding.pathing.InspectablePathfinder;
import fr.riege.ebsl.common.pathfinding.pathing.NeighborStrategies;
import fr.riege.ebsl.common.pathfinding.pathing.configuration.PathfinderConfiguration;
import fr.riege.ebsl.common.pathfinding.pathing.processing.NodeProcessorRegistry;
import fr.riege.ebsl.common.pathfinding.pathing.result.*;
import fr.riege.ebsl.common.pathfinding.provider.NavigationPointProviders;
import fr.riege.ebsl.common.pathfinding.provider.WorldNavigationPointProvider;
import fr.riege.ebsl.common.pathfinding.settings.PathfinderSettings;
import fr.riege.ebsl.common.pathfinding.wrapper.PathPosition;
import fr.riege.ebsl.common.platform.layer.IInputLayer;
import fr.riege.ebsl.common.platform.layer.IPhysicsLayer;
import fr.riege.ebsl.common.platform.service.NavigationService;
import fr.riege.ebsl.common.world.layer.IPlayerLayer;
import fr.riege.ebsl.common.world.layer.IWorldLayer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

@SuppressWarnings("java:S107")
public final class CommonNavigationBackend implements NavigationService {
    private final IWorldLayer world;
    private final IPlayerLayer player;
    private final IInputLayer input;
    private final MovementTerrain checker;
    private final WorldNavigationPointProvider provider;
    private final PathExecutor executor;
    private final LongRangePathSession longRangeSession = new LongRangePathSession();
    private final Consumer<Runnable> gameThread;

    private InspectablePathfinder pathfinder;
    private final AtomicReference<PathfinderResult> lastResult = new AtomicReference<>();
    private volatile boolean navigating;
    private volatile Node.MoveType currentMoveType = Node.MoveType.WALK;
    private Runnable onFinished;
    private Runnable onFailed;
    private List<Node> activeNodes = List.of();
    private int goalX;
    private int goalY;
    private int goalZ;
    private boolean executePath;
    private ExecutionOptions executionOptions = ExecutionOptions.defaults();
    private boolean preciseExecution;
    private boolean allowParkour = true;
    private boolean allowJump = true;
    private boolean allowFall = true;
    private boolean allowWalkDiagonal = true;

    public CommonNavigationBackend(IWorldLayer world, IPlayerLayer player, IPhysicsLayer physics, IInputLayer input) {
        this(world, player, physics, input, Runnable::run);
    }

    public CommonNavigationBackend(IWorldLayer world, IPlayerLayer player, IPhysicsLayer physics,
                                   IInputLayer input, Consumer<Runnable> gameThread) {
        this.world = world;
        this.player = player;
        this.input = input == null ? new IInputLayer() {} : input;
        this.gameThread = gameThread == null ? Runnable::run : gameThread;
        this.checker = new WalkabilityChecker(world);
        this.provider = NavigationPointProviders.worldBacked(checker);
        this.executor = new PathExecutor(world, player, physics, this.input);
    }

    @Override public void startNavigation(NavigationRequest request) {
        configureOptions(request);
        Vec3d pos = player.position();
        int px = (int) Math.floor(pos.x());
        int py = (int) Math.floor(pos.y());
        int pz = (int) Math.floor(pos.z());
        switch (request.goal().resolve(px, py, pz)) {
            case NavigationTarget.Block(int x, int y, int z) -> startBlockGoalConfigured(x, y, z);
            case NavigationTarget.Column(int x, int z) -> startColumnGoalConfigured(x, z);
        }
    }

    @Override public void startBlockGoal(int x, int y, int z) {
        configureDefaults();
        startBlockGoalConfigured(x, y, z);
    }

    private void startBlockGoalConfigured(int x, int y, int z) {
        longRangeSession.clear();
        Vec3d pos = player.position();
        if (isLongRangeGoal(pos.x(), pos.z(), x, z)) {
            longRangeSession.startBlockGoal(x, y, z);
            LongRangePathSession.SegmentGoal segmentGoal = longRangeSession.planSegmentGoal(pos.x(), pos.z());
            PathPosition segmentTarget = segmentGoal.segmented()
                ? resolveSpeculativeSegmentTarget(pos.x(), pos.z(), (int) Math.floor(pos.y()), segmentGoal)
                : new PathPosition(segmentGoal.x(), y, segmentGoal.z());
            startPathTo(segmentTarget, this.onFinished, true, false);
            return;
        }
        startPathTo(new PathPosition(x, y, z), this.onFinished, true, true);
    }

    @Override public void startColumnGoal(int x, int z) {
        configureDefaults();
        startColumnGoalConfigured(x, z);
    }

    private void startColumnGoalConfigured(int x, int z) {
        Vec3d pos = player.position();
        longRangeSession.start(x, z);
        LongRangePathSession.SegmentGoal segmentGoal = longRangeSession.planSegmentGoal(pos.x(), pos.z());
        startPathTo(resolveSpeculativeSegmentTarget(pos.x(), pos.z(), (int) Math.floor(pos.y()), segmentGoal), null, true, false);
    }

    private static boolean isLongRangeGoal(double fromX, double fromZ, int goalX, int goalZ) {
        double dx = goalX + 0.5 - fromX;
        double dz = goalZ + 0.5 - fromZ;
        double maxSegmentDistance = PathfinderSettings.instance().maxSegmentDistance.value();
        return dx * dx + dz * dz > maxSegmentDistance * maxSegmentDistance;
    }

    @Override public void startPathTest(int x, int y, int z) {
        configureDefaults();
        startPathTo(new PathPosition(x, y, z), null, false, true);
    }

    @Override public void startPathTestXZ(int x, int z) {
        configureDefaults();
        Vec3d pos = player.position();
        startPathTest(x, resolveGoalYForXZ(x, (int) Math.floor(pos.y()), z), z);
    }

    @Override public void stop(boolean announce) {
        navigating = false;
        onFinished = null;
        onFailed = null;
        activeNodes = List.of();
        executePath = false;
        executor.stop();
        longRangeSession.clear();
        abortActiveSearch();
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
        input.setSneakDown(value);
    }

    @Override public void startGreenhouseWalk(Vec3d target, Runnable onFinished, boolean isFirst) {
        longRangeSession.clear();
        startPathTo(new PathPosition(target.x(), target.y(), target.z()), onFinished, true, true);
    }

    public PathfinderResult lastResult() {
        return lastResult.get();
    }

    public Collection<PathPosition> lastPathPositions() {
        if (!activeNodes.isEmpty()) {
            return activeNodes.stream().map(node -> node.position).toList();
        }
        PathfinderResult result = lastResult.get();
        Path path = result == null ? null : result.getPath();
        return path == null ? List.of() : path.collect();
    }

    @Override public NavigationStatus pathStatus() {
        if (executor.getState() == PathExecutor.State.REPLANNING) return NavigationStatus.REPLANNING;
        if (executor.getState() == PathExecutor.State.WALKING) return NavigationStatus.EXECUTING;
        PathfinderResult result = lastResult.get();
        if (navigating && result == null) return NavigationStatus.CALCULATING;
        if (result == null) return NavigationStatus.IDLE;
        if (result.hasFailed()) return NavigationStatus.FAILED;
        if (hasUsablePath(result)) return navigating ? NavigationStatus.EXECUTING : NavigationStatus.FOUND;
        return NavigationStatus.FAILED;
    }

    @Override public int lastPathNodeCount() {
        return lastPathPositions().size();
    }

    @Override public void renderCameraFrame() {
        executor.renderCameraFrame();
    }

    @Override public void renderWorld() {
        // World rendering is supplied by platform integrations when available.
    }

    @Override public void tick() {
        if (!navigating) return;
        if (!player.isAlive()) {
            stop(false);
            return;
        }

        if (executor.getState() == PathExecutor.State.REPLANNING) {
            handleExecutorReplan();
            return;
        }

        if (executor.getState() == PathExecutor.State.WALKING) {
            tickWalkingExecutor();
            return;
        }

        PathfinderResult result = lastResult.get();
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
        if (pathfinder != null) {
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
            input.releaseMovementKeys();
        }
        Runnable finished = onFinished;
        onFinished = null;
        if (finished != null) {
            finished.run();
        }
    }

    private void startPathTo(PathPosition target, Runnable onFinished, boolean executePath, boolean clearLongRange) {
        abortActiveSearch();
        clearPathCaches();
        if (clearLongRange) {
            longRangeSession.clear();
        }
        this.onFinished = onFinished;
        this.activeNodes = List.of();
        this.executePath = executePath;
        this.lastResult.set(null);
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
        if (start.equals(effectiveTarget)) {
            lastResult.set(PathfinderResults.of(PathState.FOUND, Paths.of(start, effectiveTarget, List.of(start))));
            markIdle(false);
            return;
        }
        PathfinderConfiguration config = instantConfiguration();
        InspectablePathfinder activePathfinder = Pathfinders.inspectableAStar(config);
        pathfinder = activePathfinder;

        activePathfinder.findPath(start, effectiveTarget)
            .whenComplete((result, throwable) -> onGameThread(() -> {
                if (pathfinder != activePathfinder) {
                    return;
                }
                pathfinder = null;
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
            }));
    }

    private void startFullPathTo(PathPosition start, PathPosition effectiveTarget, boolean executePath) {
        abortActiveSearch();
        PathfinderConfiguration config = fullConfiguration();
        InspectablePathfinder activePathfinder = Pathfinders.inspectableAStar(config);
        pathfinder = activePathfinder;
        activePathfinder.findPath(start, effectiveTarget)
            .whenComplete((result, throwable) -> onGameThread(() -> {
                if (pathfinder != activePathfinder) {
                    return;
                }
                pathfinder = null;
                if (throwable != null) {
                    navigating = false;
                    lastResult.set(null);
                    input.releaseMovementKeys();
                    if (onFailed != null) onFailed.run();
                    return;
                }
                handleWalkResult(result, config, executePath);
            }));
    }

    private void handleWalkResult(PathfinderResult result, PathfinderConfiguration config, boolean executePath) {
        lastResult.set(result);
        Collection<PathPosition> positions = result != null && result.getPath() != null
            ? result.getPath().collect()
            : List.of();
        if (!PathResultClassifier.hasUsablePath(result, positions)) {
            navigating = false;
            input.releaseMovementKeys();
            if (onFailed != null) onFailed.run();
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
                segmentEnd.position.flooredY(),
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
        abortActiveSearch();
        clearPathCaches();
        Vec3d pos = player.position();
        PathPosition start = new PathPosition(Math.floor(pos.x()), resolveStartY(pos.x(), pos.y(), pos.z()), Math.floor(pos.z()));
        PathfinderConfiguration config = repairConfiguration();
        InspectablePathfinder repairPathfinder = Pathfinders.inspectableAStar(config);
        pathfinder = repairPathfinder;
        repairPathfinder.findPath(start, request.joinNode().position)
            .whenComplete((result, throwable) -> onGameThread(() ->
                completePathRepair(request, config, repairPathfinder, result, throwable)));
    }

    private void completePathRepair(PathRepairRequest request,
                                    PathfinderConfiguration config,
                                    InspectablePathfinder repairPathfinder,
                                    PathfinderResult result,
                                    Throwable throwable) {
        if (pathfinder != repairPathfinder) {
            return;
        }
        pathfinder = null;
        Collection<PathPosition> positions = result != null && result.getPath() != null
            ? result.getPath().collect()
            : List.of();
        if (throwable != null || !PathResultClassifier.hasUsablePath(result, positions)) {
            restartFromCurrentGoal();
            return;
        }
        ProcessedPath repairPath = WalkPathProcessor.process(positions, config, checker);
        List<Node> merged = mergePathPrefixWithTail(repairPath.navigationPath(), request.remainingPath());
        if (merged.size() < 2) {
            restartFromCurrentGoal();
            return;
        }
        activeNodes = merged;
        startExecutor(merged, request.goalX(), request.goalY(), request.goalZ(), true);
        executor.rememberRecentRepair(request.reason());
    }

    private void restartFromCurrentGoal() {
        startPathTo(new PathPosition(goalX, goalY, goalZ), onFinished, true, !longRangeSession.isActive());
    }

    private void startExecutor(List<Node> path, int goalX, int goalY, int goalZ, boolean finishAtPathEnd) {
        if (path == null || path.isEmpty()) {
            navigating = false;
            return;
        }
        executor.start(new ExecutionPlan(path, goalX, goalY, goalZ, preciseExecution, null,
            executionOptions, finishAtPathEnd));
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
            int hy = resolveContinuationY(hx, horizonStart.flooredY(), hz);
            start = new PathPosition(hx, hy, hz);
            fromX = hx + 0.5;
            fromZ = hz + 0.5;
            rollingHorizon = true;
        } else {
            int sx = longRangeSession.currentSegmentGoalX();
            int sz = longRangeSession.currentSegmentGoalZ();
            int sy = resolveContinuationY(sx, longRangeSession.currentSegmentGoalY(), sz);
            start = new PathPosition(sx, sy, sz);
            fromX = sx + 0.5;
            fromZ = sz + 0.5;
        }
        LongRangePathSession.SegmentGoal segmentGoal = longRangeSession.planSegmentGoal(fromX, fromZ);
        PathPosition target = longRangeSession.requiresExactY() && !segmentGoal.segmented()
            ? new PathPosition(segmentGoal.x(), longRangeSession.finalGoalY(), segmentGoal.z())
            : resolveSpeculativeSegmentTarget(fromX, fromZ, start.flooredY(), segmentGoal);
        int targetX = target.flooredX();
        int targetY = target.flooredY();
        int targetZ = target.flooredZ();
        PathfinderConfiguration config = queuedConfiguration(true);
        InspectablePathfinder queuedPathfinder = Pathfinders.inspectableAStar(config);
        int requestId = longRangeSession.markSegmentCalculationStarted(queuedPathfinder);
        LongRangePathSession.SegmentAttachment attachment = startFromPlayer
            ? LongRangePathSession.SegmentAttachment.REPLACE_FROM_PLAYER
            : LongRangePathSession.SegmentAttachment.MERGE_WITH_CURRENT;
        boolean finalRollingHorizon = rollingHorizon;
        queuedPathfinder.findPath(start, target)
            .whenComplete((result, throwable) -> onGameThread(() -> handleQueuedLongRangeResult(
                requestId, result, throwable, config, attachment, finalRollingHorizon, targetX, targetY, targetZ)));
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
        if (!isAttachableSegment(processed.navigationPath(), attachment, rollingHorizon)) {
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
        longRangeSession.onSegmentStarted(pending.goalX(), pending.goalY(), pending.goalZ(), pending.needsContinuation(), pending.partial());
        if (!pending.needsContinuation()) {
            longRangeSession.markCompleted();
        }
    }

    private void tickWalkingExecutor() {
        executor.tick();
        Node.MoveType moveType = executor.getCurrentMoveType();
        if (moveType != null) {
            currentMoveType = moveType;
        }
        if (executor.getState() == PathExecutor.State.FINISHED || executor.getState() == PathExecutor.State.FAILED) {
            if (!handleLongRangeProgress(true)) {
                markIdle(false);
            }
            return;
        }
        handleLongRangeProgress(false);
    }

    private boolean isAttachableSegment(List<Node> path,
                                        LongRangePathSession.SegmentAttachment attachment,
                                        boolean rollingHorizon) {
        if (attachment == LongRangePathSession.SegmentAttachment.REPLACE_FROM_PLAYER || !rollingHorizon) {
            return true;
        }
        List<Node> current = executor.getPathSnapshot();
        if (current.isEmpty() || path.isEmpty()) {
            return true;
        }
        Node start = path.getFirst();
        double bestDistance = Double.MAX_VALUE;
        for (Node node : current) {
            bestDistance = Math.min(bestDistance, node.position.distance(start.position));
        }
        return bestDistance <= 12.0;
    }

    private PathPosition resolveSpeculativeSegmentTarget(double fromX, double fromZ, int preferredY,
                                                         LongRangePathSession.SegmentGoal segmentGoal) {
        int segmentGoalX = segmentGoal.x();
        int segmentGoalZ = segmentGoal.z();
        PathPosition direct = new PathPosition(segmentGoalX, resolveGoalYForXZ(segmentGoalX, preferredY, segmentGoalZ), segmentGoalZ);
        if (!segmentGoal.segmented() || isLoadedWalkableXZ(segmentGoalX, direct.flooredY(), segmentGoalZ)) {
            return direct;
        }

        int steps = PathfinderSettings.instance().segmentTargetBacktrackSteps.value();
        if (steps <= 0) {
            return direct;
        }

        double targetX = segmentGoalX + 0.5;
        double targetZ = segmentGoalZ + 0.5;
        for (int step = 1; step <= steps; step++) {
            double t = step / (double) (steps + 1);
            int x = (int) Math.floor(targetX + (fromX - targetX) * t);
            int z = (int) Math.floor(targetZ + (fromZ - targetZ) * t);
            int y = resolveGoalYForXZ(x, preferredY, z);
            if (isLoadedWalkableXZ(x, y, z)) {
                return new PathPosition(x, y, z);
            }
        }
        return direct;
    }

    private boolean isLoadedWalkableXZ(int x, int y, int z) {
        return world.isLoaded(x, y, z) && checker.isWalkable(x, y, z);
    }

    private PathPosition resolveTarget(PathPosition target) {
        int x = target.flooredX();
        int y = target.flooredY();
        int z = target.flooredZ();
        if (checker.isSolid(x, y, z)) {
            return new PathPosition(x, y + 1.0, z);
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

    private int resolveContinuationY(int x, int preferredY, int z) {
        if (checker.isWalkable(x, preferredY, z)
            || (checker.isPassable(x, preferredY, z) && checker.isPassable(x, preferredY + 1, z))) {
            return preferredY;
        }
        return resolveGoalYForXZ(x, preferredY, z);
    }

    private static boolean hasUsablePath(PathfinderResult result) {
        if (result == null || result.getPath() == null || result.getPathState() == PathState.ABORTED) {
            return false;
        }
        if (!result.successful() && !result.hasFallenBack()) {
            return false;
        }
        Collection<PathPosition> positions = result.getPath().collect();
        return positions != null && !positions.isEmpty();
    }

    private PathfinderConfiguration instantConfiguration() {
        return configuration(
            PathfinderSettings.instance().instantWalkMaxIterations.value(),
            PathfinderSettings.instance().instantWalkMaxLength.value(),
            PathfinderSettings.instance().instantCalculationTimeMs.value());
    }

    private PathfinderConfiguration fullConfiguration() {
        return configuration(
            PathfinderSettings.instance().defaultWalkMaxIterations.value(),
            PathfinderSettings.instance().defaultWalkMaxLength.value(),
            PathfinderSettings.instance().defaultCalculationTimeMs.value());
    }

    private PathfinderConfiguration repairConfiguration() {
        return configuration(
            PathfinderSettings.instance().repairWalkMaxIterations.value(),
            PathfinderSettings.instance().repairWalkMaxLength.value(),
            PathfinderSettings.instance().repairCalculationTimeMs.value());
    }

    private PathfinderConfiguration queuedConfiguration(boolean speculative) {
        int maxIterations = PathfinderSettings.instance().queuedLongRangeMaxIterations.value();
        int maxLength = PathfinderSettings.instance().queuedLongRangeMaxLength.value();
        int maxCalculationTimeMs = PathfinderSettings.instance().queuedCalculationTimeMs.value();
        return speculative
            ? speculativeConfiguration(maxIterations, maxLength, maxCalculationTimeMs)
            : configuration(maxIterations, maxLength, maxCalculationTimeMs);
    }

    private PathfinderConfiguration configuration(int maxIterations, int maxLength, int maxCalculationTimeMs) {
        return PathfinderConfiguration.builder()
            .maxIterations(maxIterations)
            .maxLength(maxLength)
            .provider(provider)
            .processors(NodeProcessorRegistry.createStandardProcessors())
            .neighborStrategy(NeighborStrategies.horizontalDiagonalAndVertical(
                PathfinderSettings.instance().maxJumpHeight.value(), allowParkour, allowJump, allowFall, allowWalkDiagonal))
            .async(true)
            .fallback(true)
            .earlyFallback(PathfinderSettings.instance().earlyFallbackEnabled.value())
            .earlyFallbackIterations(PathfinderSettings.instance().earlyFallbackIterations.value())
            .earlyFallbackMinPathNodes(PathfinderSettings.instance().earlyFallbackMinPathNodes.value())
            .earlyFallbackMinProgressRatio(PathfinderSettings.instance().earlyFallbackMinProgressRatio.value())
            .maxCalculationTimeMs(maxCalculationTimeMs)
            .build();
    }

    private PathfinderConfiguration speculativeConfiguration(int maxIterations, int maxLength, int maxCalculationTimeMs) {
        return PathfinderConfiguration.builder()
            .maxIterations(maxIterations)
            .maxLength(maxLength)
            .provider(provider)
            .processors(NodeProcessorRegistry.createStandardProcessors())
            .neighborStrategy(NeighborStrategies.horizontalDiagonalAndVertical(
                PathfinderSettings.instance().maxJumpHeight.value(), allowParkour, allowJump, allowFall, allowWalkDiagonal))
            .async(true)
            .fallback(true)
            .earlyFallback(true)
            .earlyFallbackIterations(PathfinderSettings.instance().speculativeLongRangeFallbackIterations.value())
            .earlyFallbackMinPathNodes(PathfinderSettings.instance().speculativeLongRangeFallbackMinNodes.value())
            .earlyFallbackMinProgressRatio(PathfinderSettings.instance().speculativeLongRangeFallbackProgress.value())
            .maxCalculationTimeMs(maxCalculationTimeMs)
            .build();
    }

    private void configureOptions(NavigationRequest request) {
        this.onFinished = request.onFinished();
        this.onFailed = request.onFailed();
        this.allowParkour = request.allowParkour();
        this.allowJump = request.allowJump();
        this.allowFall = request.allowFall();
        this.allowWalkDiagonal = request.allowWalkDiagonal();
        this.preciseExecution = request.preciseGoalTolerance() != ExecutionOptions.DEFAULT_TOLERANCE;
        double stickySneakDistance = preciseExecution && request.allowSneak() ? 5.0 : -1.0;
        this.executionOptions = new ExecutionOptions(
            request.allowReplan(),
            request.allowJump(),
            request.allowRotation(),
            request.allowSneak(),
            preciseExecution,
            stickySneakDistance,
            request.allowSneak() && executor.isSneakLatched(),
            0.5,
            0.5,
            request.preciseGoalTolerance());
    }

    private void configureDefaults() {
        this.onFinished = null;
        this.onFailed = null;
        this.executionOptions = ExecutionOptions.defaults();
        this.preciseExecution = false;
        this.allowParkour = true;
        this.allowJump = true;
        this.allowFall = true;
        this.allowWalkDiagonal = true;
    }

    private static List<Node> mergePathPrefixWithTail(List<Node> prefix, List<Node> tail) {
        ArrayList<Node> merged = new ArrayList<>();
        appendDistinct(merged, prefix);
        appendDistinct(merged, tail);
        return merged;
    }

    private static void appendDistinct(List<Node> merged, List<Node> candidates) {
        if (candidates == null) return;
        for (Node candidate : candidates) {
            if (candidate != null && (merged.isEmpty() || !merged.getLast().position.equals(candidate.position))) {
                merged.add(candidate);
            }
        }
    }

    private void onGameThread(Runnable action) {
        gameThread.accept(action);
    }

    private void abortActiveSearch() {
        InspectablePathfinder active = pathfinder;
        pathfinder = null;
        if (active != null) {
            active.abort();
        }
    }

    private void clearPathCaches() {
        checker.clearCache();
        provider.clearCache();
    }
}
