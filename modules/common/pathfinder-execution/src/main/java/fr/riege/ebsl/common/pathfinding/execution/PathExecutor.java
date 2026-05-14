/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 *
 * Copyright (C) 2026 Victor Riegert and EBSL contributors
 *
 * This file is part of EBSL.
 *
 * EBSL is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * EBSL is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public
 * License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with EBSL. If not, see <https://www.gnu.org/licenses/>.
 */

package fr.riege.ebsl.common.pathfinding.execution;

import fr.riege.ebsl.common.math.Vec3d;
import fr.riege.ebsl.common.pathfinding.Node;
import fr.riege.ebsl.common.pathfinding.check.PathCheckContext;
import fr.riege.ebsl.common.pathfinding.check.PathCheckResult;
import fr.riege.ebsl.common.pathfinding.check.PathProximitySnapshot;
import fr.riege.ebsl.common.pathfinding.diagnostics.PathExecutionDiagnostics;
import fr.riege.ebsl.common.pathfinding.movement.MovementTerrain;
import fr.riege.ebsl.common.pathfinding.movement.WalkabilityChecker;
import fr.riege.ebsl.common.pathfinding.movement.types.evaluation.MovementValidationResult;
import fr.riege.ebsl.common.pathfinding.registry.PathfindingRegistries;
import fr.riege.ebsl.common.pathfinding.rotation.RotationExecutor;
import fr.riege.ebsl.common.pathfinding.settings.PathfinderSettings;
import fr.riege.ebsl.common.platform.layer.IInputLayer;
import fr.riege.ebsl.common.platform.layer.IPhysicsLayer;
import fr.riege.ebsl.common.world.layer.IPlayerLayer;
import fr.riege.ebsl.common.world.layer.IWorldLayer;

import java.util.List;

public final class PathExecutor {
    public enum State { IDLE, WALKING, REPLANNING, FINISHED, FAILED }

    private final IPlayerLayer player;
    private final IInputLayer input;
    private final MovementTerrain checker;
    private final PathTracker pathTracker = new PathTracker();
    private final PathRecoveryController recoveryController = new PathRecoveryController();
    private final RotationExecutor rotationExecutor;
    private final PathRotationController rotationController;
    private final WalkMovementController movementController;

    private State state = State.IDLE;
    private boolean precise;
    private long lastReplanTime;
    private long coastStartTime;
    private long lastSmartCutoffTime;
    private long lastCameraFrameMs;
    private int jumpCooldown;
    private int goalX;
    private int goalY;
    private int goalZ;
    private double goalCenterX = 0.5;
    private double goalCenterZ = 0.5;
    private boolean allowReplan = true;
    private boolean allowJumps = true;
    private boolean allowRotation = true;
    private boolean allowSneak = true;
    private boolean exactGoalCentering;
    private double stickySneakDistance = -1.0;
    private boolean sneakLatched;
    private double preciseGoalTolerance = ExecutionOptions.DEFAULT_TOLERANCE;
    private Runnable onFinished;
    private boolean finishAtPathEnd = true;
    private boolean replanFromPlayerRequested;
    private PathRepairRequest repairRequest;
    private String lastRepairReason = "";
    private long lastRepairReasonTime;
    private Node.MoveType lastKnownMoveType = Node.MoveType.WALK;

    public PathExecutor(IWorldLayer world, IPlayerLayer player, IPhysicsLayer physics, IInputLayer input) {
        this.player = player;
        this.input = input;
        this.checker = new WalkabilityChecker(world);
        this.rotationExecutor = new RotationExecutor(player, physics);
        this.rotationController = new PathRotationController(world, player, rotationExecutor);
        this.movementController = new WalkMovementController(world, player, input, checker);
    }

    public void start(List<Node> path, int goalX, int goalY, int goalZ, boolean precise) {
        start(new ExecutionPlan(path, goalX, goalY, goalZ, precise, null));
    }

    public void start(ExecutionPlan plan) {
        this.goalX = plan.goalX();
        this.goalY = plan.goalY();
        this.goalZ = plan.goalZ();
        this.precise = plan.precise();
        this.onFinished = plan.onFinished();
        ExecutionOptions opts = plan.options();
        this.allowReplan = opts.allowReplan();
        this.allowJumps = opts.allowJumps();
        this.allowRotation = opts.allowRotation();
        this.allowSneak = opts.allowSneak();
        this.exactGoalCentering = opts.exactGoalCentering();
        this.stickySneakDistance = allowSneak ? opts.stickySneakDistance() : -1.0;
        this.preciseGoalTolerance = Math.max(0.01, opts.preciseGoalTolerance());
        this.goalCenterX = opts.goalCenterX();
        this.goalCenterZ = opts.goalCenterZ();
        this.sneakLatched = allowSneak && opts.sneakLatched();
        this.finishAtPathEnd = plan.finishAtPathEnd();
        resetTransientState();
        pathTracker.start(plan.path());
        movementController.setPath(plan.path());
        rotationController.rebuild(plan.path());
        refreshVisualizer();
        state = State.WALKING;
    }

    public State getState() { return state; }

    public Node.MoveType getCurrentMoveType() {
        if (state == State.REPLANNING) return lastKnownMoveType;
        if (state != State.WALKING) return null;
        Node waypoint = pathTracker.getMovementWaypoint();
        if (waypoint != null && waypoint.moveType() != null) {
            lastKnownMoveType = waypoint.moveType();
        }
        return lastKnownMoveType;
    }

    public int getWaypointIndex() { return pathTracker.getPursuitSegment(); }
    public int getCamTargetIdx() { return rotationController.getCamTargetIdx(); }
    public int getCameraIndex() { return rotationController.getCameraIndex(); }
    public List<Vec3d> getCameraPath() { return rotationController.getCameraPath(); }
    public List<Node> getPathSnapshot() { return pathTracker.getPathSnapshot(); }
    public boolean isSneakLatched() { return sneakLatched; }
    public void setSneakLatched(boolean sneakLatched) { this.sneakLatched = sneakLatched; }
    public double getProgressRatio(Vec3d playerPos) { return pathTracker.getProgressRatio(playerPos); }
    public double getRemainingDistance(Vec3d playerPos) { return pathTracker.getRemainingDistance(playerPos); }
    public Node getNodeAtRatio(double ratio) { return pathTracker.getNodeAtRatio(ratio); }

    public boolean canAcceptSpeculativeReplacement() {
        Node.MoveType moveType = getCurrentMoveType();
        return state == State.WALKING
            && (player.onGround() || player.isInWater())
            && moveType != Node.MoveType.JUMP
            && moveType != Node.MoveType.PARKOUR
            && moveType != Node.MoveType.FALL
            && jumpCooldown == 0;
    }

    public boolean consumeReplanFromPlayerRequest() {
        boolean requested = replanFromPlayerRequested;
        replanFromPlayerRequested = false;
        return requested;
    }

    public PathRepairRequest consumeRepairRequest() {
        PathRepairRequest request = repairRequest;
        repairRequest = null;
        return request;
    }

    public void rememberRecentRepair(String reason) {
        lastRepairReason = reason;
        lastRepairReasonTime = System.currentTimeMillis();
    }

    void setJumpCooldown(int jumpCooldown) {
        this.jumpCooldown = Math.max(0, jumpCooldown);
    }

    public void continueWith(List<Node> continuationPath, int goalX, int goalY, int goalZ) {
        if (continuationPath == null || continuationPath.isEmpty()) return;
        this.goalX = goalX;
        this.goalY = goalY;
        this.goalZ = goalZ;
        pathTracker.continueWith(continuationPath);
        movementController.setPath(pathTracker.getPath());
        rotationController.rebuild(pathTracker.getPath());
        refreshVisualizer();
    }

    public void trimAndContinueWith(double trimRatio, List<Node> newPath, int goalX, int goalY, int goalZ) {
        if (newPath == null || newPath.isEmpty()) return;
        this.goalX = goalX;
        this.goalY = goalY;
        this.goalZ = goalZ;
        pathTracker.trimAndContinueWith(trimRatio, newPath);
        movementController.setPath(pathTracker.getPath());
        rotationController.rebuild(pathTracker.getPath());
        refreshVisualizer();
    }

    @SuppressWarnings("java:S3776")
    public void tick() {
        if (!canTickMovement()) {
            return;
        }
        if (pauseForBlockingClientState()) return;

        if (player.onGround() || player.isInWater()) {
            jumpCooldown = Math.max(0, jumpCooldown - 1);
        }

        Vec3d playerPos = player.position();
        List<Node> path = pathTracker.getPath();
        if (path == null || path.isEmpty()) {
            finish();
            return;
        }
        if (isAtGoal(playerPos)) {
            finish();
            return;
        }

        double distMoved = pathTracker.noteMovementProgress(playerPos, PathfinderSettings.instance().stuckDistThreshold.value());
        long now = System.currentTimeMillis();
        pathTracker.advancePursuit(playerPos, now);
        pathTracker.computeAndTrackPathProgress(playerPos, PathfinderSettings.instance().pathProgressEpsilon.value(), now);

        PathProximitySnapshot proximity = pathTracker.analyzePathProximity(playerPos);
        pathTracker.updateSevereOffPathState(proximity, now);
        if (handlePathChecks(playerPos, path, proximity, now)) return;
        path = pathTracker.getPath();
        proximity = pathTracker.analyzePathProximity(playerPos);

        MovementValidationResult movementValidation = movementController.validateCurrentSegment(
            playerPos, pathTracker.getPursuitSegment());
        if (!movementValidation.valid()) {
            triggerRepair(
                Math.min(path.size() - 2, pathTracker.getPursuitSegment() + 1),
                movementValidation.reason());
            return;
        }

        if (pathTracker.getPursuitSegment() >= path.size() - 1) {
            tickGoalCoasting(playerPos);
            return;
        }

        PathProgressSnapshot progress = new PathProgressSnapshot(
            distMoved,
            now - pathTracker.getLastProgressTime(),
            now - pathTracker.getLastPathProgressTime(),
            proximity);
        PathRecoveryController.RecoveryDecision recovery = recoveryController.update(
            player,
            input,
            progress,
            allowReplan,
            now - lastReplanTime > PathfinderSettings.instance().replanCooldownMs.value(),
            jumpCooldown,
            recoveryMoveType(path, pathTracker.getPursuitSegment()));
        if (handleRecoveryDecision(playerPos, proximity, recovery)) {
            return;
        }
        boolean recoveryJump = recovery.action() == PathRecoveryController.Action.RECOVERY_JUMP;
        if (recoveryJump) {
            jumpCooldown = PathfinderSettings.instance().jumpCooldownTicks.value();
        }

        Node movementWp = pathTracker.getMovementWaypoint();
        updateCameraIntent(playerPos, path);
        applyCameraFallbackIfRenderStale();
        double finalDx = (goalX + goalCenterX) - playerPos.x();
        double finalDy = goalY - playerPos.y();
        double finalDz = (goalZ + goalCenterZ) - playerPos.z();
        double distToFinal = Math.sqrt(finalDx * finalDx + finalDy * finalDy + finalDz * finalDz);

        if (allowSneak && !sneakLatched && stickySneakDistance > 0 && distToFinal <= stickySneakDistance) {
            sneakLatched = true;
        }
        if (movementWp != null) {
            movementController.apply(this, playerPos, movementWp, distToFinal,
                allowJumps && !recoveryJump, allowSneak && sneakLatched,
                pathTracker.getPursuitSegment(), jumpCooldown, pathTracker.getLastProgressTime());
        }
        input.setSneakDown(allowSneak && sneakLatched && !player.isInWater());
    }

    private boolean canTickMovement() {
        if (state != State.WALKING && state != State.REPLANNING) {
            return false;
        }
        if (state == State.REPLANNING) {
            return false;
        }
        if (!player.isAlive()) {
            stop();
            return false;
        }
        return true;
    }

    private boolean handleRecoveryDecision(Vec3d playerPos,
                                           PathProximitySnapshot proximity,
                                           PathRecoveryController.RecoveryDecision recovery) {
        if (recovery.noteProgress()) {
            pathTracker.noteMovementProgress(playerPos, 0.0);
        }
        return switch (recovery.action()) {
            case REPLAN_FROM_PLAYER -> {
                triggerReplan(true);
                yield true;
            }
            case REPAIR_TO_SEGMENT -> {
                triggerRepair(chooseLocalRepairSegment(proximity), recovery.reason());
                yield true;
            }
            case TICK_HANDLED -> true;
            case ALIGN_TO_PATH -> {
                Vec3d correction = pathTracker.computeCorrectionToPath(playerPos, proximity);
                InputApplier.applyCornerAlignment(player, input, correction.x(), correction.z());
                yield true;
            }
            default -> false;
        };
    }

    public void stop() {
        state = State.IDLE;
        rotationController.stop();
        releaseAll();
        PathExecutionDiagnostics.clear();
    }

    public void renderCameraFrame() {
        if (state != State.WALKING || !allowRotation) {
            return;
        }
        List<Node> path = pathTracker.getPath();
        if (path == null || path.isEmpty()) {
            return;
        }
        updateCameraIntent(player.position(), path);
        rotationController.renderFrame();
        lastCameraFrameMs = System.currentTimeMillis();
    }

    public void tickRotation() {
        renderCameraFrame();
    }

    public void releaseAll() {
        InputApplier.releaseAll(input, allowSneak && sneakLatched);
    }

    private void resetTransientState() {
        jumpCooldown = 0;
        coastStartTime = 0;
        lastSmartCutoffTime = 0;
        replanFromPlayerRequested = false;
        repairRequest = null;
        lastRepairReason = "";
        lastRepairReasonTime = 0;
        lastCameraFrameMs = 0;
        lastKnownMoveType = Node.MoveType.WALK;
        recoveryController.reset();
        rotationController.reset();
    }

    private boolean pauseForBlockingClientState() {
        if (player.isFlying()) {
            releaseAll();
            input.setSneakDown(true);
            return true;
        }
        return false;
    }

    private boolean handlePathChecks(Vec3d playerPos, List<Node> path, PathProximitySnapshot proximity, long now) {
        PathCheckResult pathCheckResult = PathfindingRegistries.pathChecks().evaluate(new PathCheckContext(
            playerPos,
            path,
            pathTracker.getPursuitSegment(),
            goalX,
            goalY,
            goalZ,
            pathTracker.getSevereOffPathDuration(now),
            proximity));

        if (pathCheckResult.isForceReplan()) {
            triggerReplan(true);
            return true;
        }
        if (pathCheckResult.isRepairToSegment()) {
            triggerRepair(pathCheckResult.cutoffSegmentIndex(), pathCheckResult.reason());
            return true;
        }
        if (!pathCheckResult.isCutoff()
            || now - lastSmartCutoffTime < PathfinderSettings.instance().smartCutoffCooldownMs.value()
            || !pathTracker.applySmartCutoff(pathCheckResult.cutoffSegmentIndex())) {
            return false;
        }
        lastSmartCutoffTime = now;
        movementController.setPath(pathTracker.getPath());
        rotationController.rebuild(pathTracker.getPath());
        refreshVisualizer();
        return false;
    }

    private void tickGoalCoasting(Vec3d playerPos) {
        if (!finishAtPathEnd && !isAtGoal(playerPos)) {
            triggerReplan(true);
            return;
        }
        if (!precise) {
            finish();
            return;
        }
        if (coastStartTime == 0) coastStartTime = System.currentTimeMillis();
        double dx = (goalX + goalCenterX) - playerPos.x();
        double dz = (goalZ + goalCenterZ) - playerPos.z();
        double hDist = Math.sqrt(dx * dx + dz * dz);
        if (exactGoalCentering && hDist > preciseGoalTolerance) {
            InputApplier.applyGoalCentering(player, input, dx, dz);
        } else {
            releaseAll();
        }
        if (hDist <= preciseGoalTolerance
            || System.currentTimeMillis() - coastStartTime > PathfinderSettings.instance().coastTimeoutMs.value()) {
            finish();
        }
    }

    private boolean isAtGoal(Vec3d playerPos) {
        double dx = (goalX + goalCenterX) - playerPos.x();
        double dy = goalY - playerPos.y();
        double dz = (goalZ + goalCenterZ) - playerPos.z();
        double hDist = Math.sqrt(dx * dx + dz * dz);
        double tolerance = precise ? preciseGoalTolerance : PathfinderSettings.instance().goalReachedHDist.value();
        return hDist <= tolerance && Math.abs(dy) <= PathfinderSettings.instance().goalReachedVDist.value();
    }

    private Node.MoveType recoveryMoveType(List<Node> path, int pursuitSegment) {
        if (isParkourRecoveryWindow(path, pursuitSegment)) {
            return Node.MoveType.PARKOUR;
        }
        if (path == null || path.isEmpty()) {
            return Node.MoveType.WALK;
        }
        int idx = Math.clamp(pursuitSegment, 0, path.size() - 1);
        Node.MoveType type = path.get(idx).moveType();
        return type == null ? Node.MoveType.WALK : type;
    }

    private boolean isParkourRecoveryWindow(List<Node> path, int pursuitSegment) {
        if (path == null || path.isEmpty()) {
            return false;
        }
        int start = Math.clamp(pursuitSegment, 0, path.size() - 1);
        int end = (int) Math.clamp(start + 2L, 0L, path.size() - 1L);
        for (int i = start; i <= end; i++) {
            if (path.get(i).moveType() == Node.MoveType.PARKOUR) {
                return true;
            }
        }
        return false;
    }

    private void triggerReplan(boolean fromPlayer) {
        releaseAll();
        lastReplanTime = System.currentTimeMillis();
        pathTracker.markReplanTriggered(lastReplanTime);
        replanFromPlayerRequested = fromPlayer;
        state = State.REPLANNING;
    }

    private void triggerRepair(int segmentIndex, String reason) {
        long now = System.currentTimeMillis();
        if (reason.equals(lastRepairReason) && now - lastRepairReasonTime < 1500) {
            triggerReplan(true);
            return;
        }
        repairRequest = pathTracker.createRepairRequest(segmentIndex, reason, goalX, goalY, goalZ).orElse(null);
        if (repairRequest == null) {
            triggerReplan(true);
            return;
        }
        releaseAll();
        lastRepairReason = reason;
        lastRepairReasonTime = now;
        lastReplanTime = now;
        pathTracker.markReplanTriggered(lastReplanTime);
        state = State.REPLANNING;
    }

    private int chooseLocalRepairSegment(PathProximitySnapshot proximity) {
        List<Node> path = pathTracker.getPath();
        if (path == null || path.size() < 2) {
            return 0;
        }
        int base = Math.max(pathTracker.getPursuitSegment(), proximity.nearestSegmentIndex());
        int lookahead = proximity.horizontalDistance() >= PathfinderSettings.instance().localRepairDriftThreshold.value()
            ? PathfinderSettings.instance().localRepairDriftLookahead.value()
            : PathfinderSettings.instance().localRepairLookahead.value();
        int target = base + lookahead;
        int maxJoinSegment = Math.max(0, path.size() - 2);
        return Math.clamp(target, 0, maxJoinSegment);
    }

    private void finish() {
        state = State.FINISHED;
        rotationController.stop();
        releaseAll();
        Runnable finished = onFinished;
        onFinished = null;
        if (finished != null) finished.run();
    }

    private void refreshVisualizer() {
        PathExecutionDiagnostics.setPath(pathTracker.getPathSnapshot());
        PathExecutionDiagnostics.setCameraPath(rotationController.getCameraPath());
        PathExecutionDiagnostics.updateExecution(rotationController.getCamTargetIdx());
    }

    private void updateCameraIntent(Vec3d playerPos, List<Node> path) {
        if (!allowRotation) {
            return;
        }
        rotationController.updateRotation(playerPos, path, pathTracker.getPursuitSegment(), null);
        PathExecutionDiagnostics.updateExecution(rotationController.getCamTargetIdx());
    }

    private void applyCameraFallbackIfRenderStale() {
        if (!allowRotation) {
            return;
        }
        long now = System.currentTimeMillis();
        if (lastCameraFrameMs == 0 || now - lastCameraFrameMs > 75) {
            rotationController.renderFrame();
        }
    }

}
