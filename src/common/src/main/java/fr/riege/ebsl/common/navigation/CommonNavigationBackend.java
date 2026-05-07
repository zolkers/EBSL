package fr.riege.ebsl.common.navigation;

import fr.riege.ebsl.common.platform.layer.IPhysicsLayer;
import fr.riege.ebsl.common.platform.layer.IPlayerLayer;
import fr.riege.ebsl.common.platform.layer.IWorldLayer;
import fr.riege.ebsl.common.math.Vec3d;
import fr.riege.ebsl.common.pathfinding.*;
import fr.riege.ebsl.common.pathfinding.execution.ExecutionOptions;
import fr.riege.ebsl.common.pathfinding.execution.ExecutionPlan;
import fr.riege.ebsl.common.pathfinding.execution.PathExecutor;
import fr.riege.ebsl.common.pathfinding.execution.PathRepairRequest;
import fr.riege.ebsl.common.pathfinding.goal.*;
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
import fr.riege.ebsl.common.platform.service.NavigationService;

import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

public final class CommonNavigationBackend implements NavigationService {
    private final IWorldLayer world;
    private final IPlayerLayer player;
    private final IPhysicsLayer physics;
    private final WalkabilityChecker checker;
    private final LayerNavigationPointProvider provider;
    private final PathExecutor executor;
    private final LongRangePathSession longRangeSession = new LongRangePathSession();
    private final Consumer<Runnable> gameThread;

    private AStarPathfinder pathfinder;
    private volatile PathfinderResult lastResult;
    private volatile boolean navigating;
    private volatile Node.MoveType currentMoveType = Node.MoveType.WALK;
    private Runnable onFinished;
    private Runnable onFailed;
    private List<Node> activeNodes = List.of();
    private int goalX, goalY, goalZ;
    private boolean executePath;
    private ExecutionOptions executionOptions = ExecutionOptions.defaults();
    private boolean preciseExecution;
    private boolean allowParkour = true;
    private boolean allowJump = true;
    private boolean allowFall = true;
    private boolean allowWalkDiagonal = true;

    public CommonNavigationBackend(IWorldLayer world, IPlayerLayer player, IPhysicsLayer physics) {
        this(world, player, physics, Runnable::run);
    }

    public CommonNavigationBackend(IWorldLayer world, IPlayerLayer player, IPhysicsLayer physics, Consumer<Runnable> gameThread) {
        this.world = world;
        this.player = player;
        this.physics = physics;
        this.gameThread = gameThread == null ? Runnable::run : gameThread;
        this.checker = new WalkabilityChecker(world);
        this.provider = new LayerNavigationPointProvider(checker);
        this.executor = new PathExecutor(world, player, physics);
    }

    @Override public void startNavigation(NavigationRequest request) {
        configureOptions(request);
        Vec3d pos = player.position();
        int px = (int) Math.floor(pos.x());
        int py = (int) Math.floor(pos.y());
        int pz = (int) Math.floor(pos.z());
        switch (request.goal().resolve(px, py, pz)) {
            case NavigationTarget.Block(int x, int y, int z) -> startBlockGoalConfigured(x, y, z);
            case NavigationTarget.Column(int x, int z)       -> startColumnGoalConfigured(x, z);
        }
    }

    @Override public void startBlockGoal(int x, int y, int z) {
        configureDefaults();
        startBlockGoalConfigured(x, y, z);
    }

    private void startBlockGoalConfigured(int x, int y, int z) {
        longRangeSession.clear();
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
        int y = resolveGoalYForXZ(segmentGoal.x(), (int) Math.floor(pos.y()), segmentGoal.z());
        startPathTo(new PathPosition(segmentGoal.x(), y, segmentGoal.z()), null, true, false);
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

    @Override public NavigationStatus pathStatus() {
        if (executor.getState() == PathExecutor.State.REPLANNING) return NavigationStatus.REPLANNING;
        if (executor.getState() == PathExecutor.State.WALKING) return NavigationStatus.EXECUTING;
        PathfinderResult result = lastResult;
        if (navigating && result == null) return NavigationStatus.CALCULATING;
        if (result == null) return NavigationStatus.IDLE;
        if (result.hasFailed()) return NavigationStatus.FAILED;
        if (hasUsablePath(result)) return navigating ? NavigationStatus.EXECUTING : NavigationStatus.FOUND;
        return NavigationStatus.FAILED;
    }

    @Override public int lastPathNodeCount() {
        return lastPathPositions().size();
    }

    @Override public void renderWorld() {
        executor.tickRotation();
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
            executor.tick();
            Node.MoveType moveType = executor.getCurrentMoveType();
            if (moveType != null) currentMoveType = moveType;
            if (executor.getState() == PathExecutor.State.FINISHED || executor.getState() == PathExecutor.State.FAILED) {
                if (!handleLongRangeProgress(true)) {
                    markIdle(false);
                }
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
            physics.clearInputs();
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
        AStarPathfinder activePathfinder = new AStarPathfinder(config);
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
        AStarPathfinder activePathfinder = new AStarPathfinder(config);
        pathfinder = activePathfinder;
        activePathfinder.findPath(start, effectiveTarget)
            .whenComplete((result, throwable) -> onGameThread(() -> {
                if (pathfinder != activePathfinder) {
                    return;
                }
                pathfinder = null;
                if (throwable != null) {
                    navigating = false;
                    lastResult = null;
                    physics.clearInputs();
                    if (onFailed != null) onFailed.run();
                    return;
                }
                handleWalkResult(result, config, executePath);
            }));
    }

    private void handleWalkResult(PathfinderResult result, PathfinderConfiguration config, boolean executePath) {
        lastResult = result;
        Collection<PathPosition> positions = result != null && result.getPath() != null
            ? result.getPath().collect()
            : List.of();
        if (!PathResultClassifier.hasUsablePath(result, positions)) {
            navigating = false;
            physics.clearInputs();
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
        AStarPathfinder repairPathfinder = new AStarPathfinder(config);
        pathfinder = repairPathfinder;
        repairPathfinder.findPath(start, request.joinNode().position)
            .whenComplete((result, throwable) -> onGameThread(() -> {
                if (pathfinder != repairPathfinder) {
                    return;
                }
                pathfinder = null;
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
            }));
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
        int gy = longRangeSession.requiresExactY() && !segmentGoal.segmented()
            ? longRangeSession.finalGoalY()
            : resolveGoalYForXZ(segmentGoal.x(), start.flooredY(), segmentGoal.z());
        PathPosition target = new PathPosition(segmentGoal.x(), gy, segmentGoal.z());
        PathfinderConfiguration config = queuedConfiguration();
        AStarPathfinder queuedPathfinder = new AStarPathfinder(config);
        int requestId = longRangeSession.markSegmentCalculationStarted(queuedPathfinder);
        LongRangePathSession.SegmentAttachment attachment = startFromPlayer
            ? LongRangePathSession.SegmentAttachment.REPLACE_FROM_PLAYER
            : LongRangePathSession.SegmentAttachment.MERGE_WITH_CURRENT;
        boolean finalRollingHorizon = rollingHorizon;
        queuedPathfinder.findPath(start, target)
            .whenComplete((result, throwable) -> onGameThread(() -> handleQueuedLongRangeResult(
                requestId, result, throwable, config, attachment, finalRollingHorizon, segmentGoal.x(), gy, segmentGoal.z())));
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
                PathfinderSettings.instance().maxJumpHeight.value(), allowParkour, allowJump, allowFall, allowWalkDiagonal))
            .async(true)
            .fallback(true)
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

    private void onGameThread(Runnable action) {
        gameThread.accept(action);
    }

    private void abortActiveSearch() {
        AStarPathfinder active = pathfinder;
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
