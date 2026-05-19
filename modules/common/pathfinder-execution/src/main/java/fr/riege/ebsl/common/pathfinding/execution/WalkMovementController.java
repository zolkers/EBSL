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
import fr.riege.ebsl.common.pathfinding.movement.MovementTerrain;
import fr.riege.ebsl.common.pathfinding.movement.types.evaluation.MovementValidationContext;
import fr.riege.ebsl.common.pathfinding.movement.types.evaluation.MovementValidationResult;
import fr.riege.ebsl.common.pathfinding.movement.types.execution.MovementExecutionContext;
import fr.riege.ebsl.common.pathfinding.movement.types.execution.WaterMovementContext;
import fr.riege.ebsl.common.pathfinding.provider.NavigationPointProviders;
import fr.riege.ebsl.common.pathfinding.provider.WorldNavigationPointProvider;
import fr.riege.ebsl.common.pathfinding.registry.PathfindingRegistries;
import fr.riege.ebsl.common.pathfinding.settings.PathfinderSettings;
import fr.riege.ebsl.common.platform.layer.IInputLayer;
import fr.riege.ebsl.common.world.layer.IPlayerLayer;
import fr.riege.ebsl.common.world.layer.IWorldLayer;

import java.util.List;

@SuppressWarnings("java:S107")
final class WalkMovementController {
    private static final double SLIME_ASCENT_JUMP_TRIGGER_DIST = 1.7;
    private static final double WATER_ENTRY_SURFACE_DROP = 0.6;
    private static final double PARKOUR_OPEN_BRAKE_REMAINING = 0.25;
    private static final double PARKOUR_CONSTRAINED_BRAKE_REMAINING = 0.45;
    private static final double PARKOUR_GROUNDED_BRAKE_OVERSHOOT = -0.12;
    private static final double PARKOUR_MIN_BRAKE_SPEED = 0.035;
    private static final double PARKOUR_LATERAL_CORRECTION = 0.18;
    private static final double PARKOUR_SHORT_TAKEOFF_MARGIN = 0.12;
    private static final double PARKOUR_MEDIUM_TAKEOFF_MARGIN = 0.30;
    private static final double PARKOUR_LONG_TAKEOFF_MARGIN = 0.45;
    private static final double PARKOUR_AIR_DRAG = 0.91;
    private static final double PARKOUR_GRAVITY = 0.08;
    private static final double PARKOUR_VERTICAL_DRAG = 0.98;
    private static final double PARKOUR_LANDING_FRONT_MARGIN = 0.38;
    private static final double PARKOUR_LANDING_BACK_MARGIN = 0.34;
    private static final double PARKOUR_SHORT_ASCENT_CARRY_SPEED = 0.17;
    private static final double PARKOUR_CHAIN_OVERSHOOT_MARGIN = 0.35;
    private static final double PARKOUR_TAKEOFF_MAX_LATERAL = 0.34;
    private static final double PARKOUR_TAKEOFF_SPEED_TOLERANCE = 0.06;

    private final IWorldLayer world;
    private final IPlayerLayer player;
    private final IInputLayer input;
    private final MovementTerrain checker;
    private final WorldNavigationPointProvider navigationPointProvider;
    private final InputApplier.MovementMemory movementMemory = new InputApplier.MovementMemory();
    private List<Node> path;
    private int lastValidatedSegment = -1;
    private int validationTick;
    private MovementValidationResult lastValidationResult = MovementValidationResult.ok();

    WalkMovementController(IWorldLayer world, IPlayerLayer player, IInputLayer input, MovementTerrain checker) {
        this.world = world;
        this.player = player;
        this.input = input;
        this.checker = checker;
        this.navigationPointProvider = NavigationPointProviders.worldBacked(checker);
    }

    void setPath(List<Node> path) {
        this.path = path;
        this.lastValidatedSegment = -1;
        this.validationTick = 0;
        this.lastValidationResult = MovementValidationResult.ok();
        this.movementMemory.reset();
        this.navigationPointProvider.clearCache();
    }

    MovementValidationResult validateCurrentSegment(Vec3d playerPos, int pursuitSegment) {
        if (path == null || path.size() < 2 || pursuitSegment + 1 >= path.size()) {
            return MovementValidationResult.ok();
        }
        validationTick++;
        if (pursuitSegment == lastValidatedSegment && validationTick % 5 != 0) {
            return lastValidationResult;
        }
        checker.clearCache();
        navigationPointProvider.clearCache();
        int currentIndex = Math.clamp(pursuitSegment, 0, path.size() - 1);
        int targetIndex = (int) Math.clamp(currentIndex + 1L, 0L, path.size() - 1L);
        Node from = path.get(currentIndex);
        Node target = path.get(targetIndex);
        Node next = path.get((int) Math.clamp(targetIndex + 1L, 0L, path.size() - 1L));
        MovementValidationContext context = new MovementValidationContext(
            checker,
            navigationPointProvider,
            from,
            target,
            next,
            playerPos,
            pursuitSegment);
        lastValidatedSegment = pursuitSegment;
        lastValidationResult = PathfindingRegistries.movementEvaluators().get(target.moveType()).validate(context);
        return lastValidationResult;
    }

    void apply(PathExecutor executor, Vec3d playerPos, Node movementWaypoint,
               double distToFinal, boolean allowJumps, boolean sneakLatched,
               int pursuitSegment, int jumpCooldown, long lastProgressTime) {
        double dxWp = movementWaypoint.position.centeredX() - playerPos.x();
        double dzWp = movementWaypoint.position.centeredZ() - playerPos.z();
        boolean partialSupportWaypoint = isPartialSupportWaypoint(movementWaypoint);
        boolean nearWaypoint = Math.sqrt(dxWp * dxWp + dzWp * dzWp) < 2.0;
        boolean nearStepUp = PathfindingRegistries.movementEvaluators().get(movementWaypoint.moveType()).reducesSprintNearWaypoint()
            && nearWaypoint;
        boolean nearPartialSupportJump = movementWaypoint.moveType() == Node.MoveType.JUMP
            && partialSupportWaypoint
            && nearWaypoint;

        boolean waterEntry = isWaterEntryWaypoint(movementWaypoint);
        Node pathMovementWaypoint = waterEntry
            ? waterEntryMovementTarget(movementWaypoint, pursuitSegment)
            : movementWaypoint;

        applyPathMovement(playerPos, pathMovementWaypoint, distToFinal,
            nearStepUp || nearPartialSupportJump, pursuitSegment, waterEntry);

        if (applyWaterEntryMovement(waterEntry, sneakLatched)) {
            return;
        }

        applyParkourVelocityControl(playerPos, movementWaypoint, pursuitSegment);
        applyParkourLandingControl(playerPos, movementWaypoint, pursuitSegment);
        applyParkourTakeoffGuard(movementWaypoint, playerPos, pursuitSegment, jumpCooldown, allowJumps);

        if (!allowJumps) {
            input.setJumpDown(player.isInWater());
            return;
        }

        handleJump(executor, movementWaypoint, playerPos, jumpCooldown, lastProgressTime, pursuitSegment);
        applyWaterMovement(movementWaypoint, playerPos, distToFinal, pursuitSegment, sneakLatched);
        applyClimbMovement(movementWaypoint, sneakLatched);
    }

    private void applyPathMovement(Vec3d playerPos, Node targetWp,
                                   double distToFinal, boolean nearStepUp, int pursuitSegment,
                                   boolean forceForwardWhenCentered) {
        double dx = targetWp.position.centeredX() - playerPos.x();
        double dz = targetWp.position.centeredZ() - playerPos.z();
        double hDist = Math.sqrt(dx * dx + dz * dz);

        PathfinderSettings settings = PathfinderSettings.instance();
        double walkTargetDeadzone = settings.walkTargetDeadzone.value();
        if (hDist < walkTargetDeadzone
            && pursuitSegment + 2 < path.size()
            && !isParkourTransitionWindow(pursuitSegment)) {
            targetWp = path.get(pursuitSegment + 2);
            dx = targetWp.position.centeredX() - playerPos.x();
            dz = targetWp.position.centeredZ() - playerPos.z();
            hDist = Math.sqrt(dx * dx + dz * dz);
        }

        if (hDist < walkTargetDeadzone) {
            input.setForwardDown(forceForwardWhenCentered);
            input.setBackwardDown(false);
            input.setLeftDown(false);
            input.setRightDown(false);
            input.setSprintDown(false);
            return;
        }

        PathSteering.SteeringVector steering = PathSteering.steer(checker, path, player, playerPos, targetWp,
            pursuitSegment, nearStepUp || isParkourTransitionWindow(pursuitSegment));
        InputApplier.MovementAxes axes = InputApplier.applyStableRelativeMovement(
            player, input, movementMemory, steering.x(), steering.z(),
            settings.walkForwardDot.value(),
            settings.walkBackwardDot.value(),
            settings.walkStrafeDot.value());
        input.setSprintDown(axes.forward()
            && distToFinal > 2.0
            && !nearStepUp
            && (!steering.nearCorner() || !settings.cornerSteeringSlowdown.value()));
    }

    private boolean isParkourTransitionWindow(int pursuitSegment) {
        int start = Math.clamp(pursuitSegment, 0, path.size() - 1);
        int end = (int) Math.clamp(start + 2L, 0L, path.size() - 1L);
        for (int i = start; i <= end; i++) {
            if (path.get(i).moveType() == Node.MoveType.PARKOUR) {
                return true;
            }
        }
        return false;
    }

    private void applyParkourLandingControl(Vec3d playerPos, Node waypoint, int pursuitSegment) {
        if (waypoint.moveType() != Node.MoveType.PARKOUR || path.isEmpty()) {
            return;
        }

        ParkourJumpGeometry geometry = parkourJumpGeometry(waypoint, pursuitSegment, playerPos);
        if (geometry == null) {
            return;
        }

        Vec3d velocity = player.velocity();
        double velocityAlong = velocity.x() * geometry.dirX() + velocity.z() * geometry.dirZ();
        int distance = parkourDistanceBlocks(waypoint, pursuitSegment);
        boolean constrainedLanding = isConstrainedParkourLanding(waypoint, geometry.dirX(), geometry.dirZ());
        ParkourExecutionProfile profile = parkourExecutionProfile(
            pursuitSegment, distance, geometry.verticalDelta(), constrainedLanding);
        if (!profile.allowLandingBrake()) {
            return;
        }
        double brakeRemaining = landingBrakeRemaining(distance, geometry.verticalDelta(), constrainedLanding);
        boolean inLandingColumn = blockX(playerPos) == waypoint.position.flooredX()
            && blockZ(playerPos) == waypoint.position.flooredZ();
        boolean shouldBrake = false;
        if (!player.onGround() && geometry.remaining() <= brakeRemaining && velocityAlong > PARKOUR_MIN_BRAKE_SPEED) {
            ParkourAirPrediction prediction = predictParkourLanding(
                playerPos.y(), velocity.y(), waypoint.position.flooredY(), geometry.progress(), velocityAlong);
            double targetProgress = geometry.jumpLen() - landingAimOffset(distance, geometry.verticalDelta());
            double frontMargin = PARKOUR_LANDING_FRONT_MARGIN;
            if (profile.immediateChain() && !constrainedLanding) {
                frontMargin += PARKOUR_CHAIN_OVERSHOOT_MARGIN;
            }
            shouldBrake = prediction.progress() > targetProgress + frontMargin;
        }
        boolean shouldHoldLanding = profile.allowLandingHold() && player.onGround()
            && (inLandingColumn || geometry.remaining() <= PARKOUR_GROUNDED_BRAKE_OVERSHOOT);

        if (!shouldBrake && !shouldHoldLanding) {
            return;
        }

        input.setForwardDown(false);
        input.setBackwardDown(true);
        input.setSprintDown(false);
        if (Math.abs(geometry.lateral()) <= PARKOUR_LATERAL_CORRECTION) {
            input.setLeftDown(false);
            input.setRightDown(false);
        }
    }

    private void applyParkourVelocityControl(Vec3d playerPos, Node waypoint, int pursuitSegment) {
        if (waypoint.moveType() != Node.MoveType.PARKOUR || path.isEmpty()) {
            return;
        }

        ParkourJumpGeometry geometry = parkourJumpGeometry(waypoint, pursuitSegment, playerPos);
        if (geometry == null) {
            return;
        }

        Vec3d velocity = player.velocity();
        double speedAlong = velocity.x() * geometry.dirX() + velocity.z() * geometry.dirZ();
        int distance = parkourDistanceBlocks(waypoint, pursuitSegment);
        boolean constrainedLanding = isConstrainedParkourLanding(waypoint, geometry.dirX(), geometry.dirZ());
        ParkourExecutionProfile profile = parkourExecutionProfile(
            pursuitSegment, distance, geometry.verticalDelta(), constrainedLanding);
        ParkourVelocityState state = new ParkourVelocityState(
            geometry.dirX(), geometry.dirZ(), geometry.jumpLen(), geometry.progress(),
            geometry.remaining(), speedAlong, distance, geometry.verticalDelta(), profile);

        if (player.onGround()) {
            applyGroundParkourVelocityControl(state);
            return;
        }

        ParkourAirPrediction prediction = predictParkourLanding(
            playerPos.y(), velocity.y(), waypoint.position.flooredY(), geometry.progress(), speedAlong);
        applyAirParkourVelocityControl(playerPos, waypoint, state, prediction);
    }

    private void applyGroundParkourVelocityControl(ParkourVelocityState state) {
        double trigger = parkourJumpTriggerDistance(state.distance());
        double targetSpeed = targetTakeoffSpeed(state.distance(), state.verticalDelta());
        boolean shortAscentCarry = state.profile().shortAscentCarry();
        if (!shortAscentCarry && state.remaining() <= trigger + 0.35 && state.speedAlong() > targetSpeed + 0.025) {
            pressParkourBrake();
            return;
        }
        input.setSprintDown(state.distance() >= 3 && state.verticalDelta() >= -1.0);
        if (shortAscentCarry && state.speedAlong() < PARKOUR_SHORT_ASCENT_CARRY_SPEED) {
            applyParkourAxisInput(state.dirX(), state.dirZ(), true);
        }
    }

    private void applyAirParkourVelocityControl(Vec3d playerPos,
                                                Node waypoint,
                                                ParkourVelocityState state,
                                                ParkourAirPrediction prediction) {
        double targetProgress = state.jumpLen() - landingAimOffset(state.distance(), state.verticalDelta());
        double frontLimit = targetProgress + PARKOUR_LANDING_FRONT_MARGIN;
        double backLimit = targetProgress - PARKOUR_LANDING_BACK_MARGIN;
        boolean inLandingColumn = blockX(playerPos) == waypoint.position.flooredX()
            && blockZ(playerPos) == waypoint.position.flooredZ();
        if (prediction.progress() > frontLimit) {
            if (state.profile().shortAscentCarry() && state.remaining() <= 0.55) {
                applyParkourAxisInput(state.dirX(), state.dirZ(), true);
                return;
            }
            if (state.profile().immediateChain() && prediction.progress() <= frontLimit + PARKOUR_CHAIN_OVERSHOOT_MARGIN) {
                clearForwardBack();
                return;
            }
            applyParkourAxisInput(state.dirX(), state.dirZ(), false);
            return;
        }
        if (!inLandingColumn || prediction.progress() < backLimit || state.speedAlong() < targetAirSpeed(state.distance(), state.verticalDelta())) {
            applyParkourAxisInput(state.dirX(), state.dirZ(), true);
            input.setSprintDown(false);
            return;
        }
        if (state.profile().shortAscentCarry()) {
            applyParkourAxisInput(state.dirX(), state.dirZ(), true);
            return;
        }
        clearForwardBack();
    }

    private ParkourExecutionProfile parkourExecutionProfile(int pursuitSegment, int distance,
                                                            double verticalDelta, boolean constrainedLanding) {
        Node.MoveType nextMove = immediateMoveAfterLanding(pursuitSegment);
        boolean immediateChain = nextMove == Node.MoveType.PARKOUR;
        boolean shortAscentCarry = immediateChain && distance <= 2 && verticalDelta > 0.25;
        return new ParkourExecutionProfile(immediateChain, shortAscentCarry, verticalDelta < -0.25, constrainedLanding);
    }

    private Node.MoveType immediateMoveAfterLanding(int pursuitSegment) {
        int nextIndex = pursuitSegment + 2;
        if (nextIndex < 0 || nextIndex >= path.size()) {
            return Node.MoveType.WALK;
        }
        return path.get(nextIndex).moveType();
    }

    private boolean isConstrainedParkourLanding(Node landing, double dirX, double dirZ) {
        int stepX = Math.abs(dirX) > Math.abs(dirZ) ? (int) Math.signum(dirX) : 0;
        int stepZ = stepX == 0 ? (int) Math.signum(dirZ) : 0;
        int aheadX = landing.position.flooredX() + stepX;
        int aheadY = landing.position.flooredY();
        int aheadZ = landing.position.flooredZ() + stepZ;
        return !checker.isWalkable(aheadX, aheadY, aheadZ);
    }

    private void applyParkourTakeoffGuard(Node waypoint, Vec3d playerPos, int pursuitSegment,
                                          int jumpCooldown, boolean allowJumps) {
        if (waypoint.moveType() != Node.MoveType.PARKOUR || !player.onGround()) {
            return;
        }

        double dx = waypoint.position.centeredX() - playerPos.x();
        double dz = waypoint.position.centeredZ() - playerPos.z();
        double hDist = Math.sqrt(dx * dx + dz * dz);
        int distance = parkourDistanceBlocks(waypoint, pursuitSegment);
        double jumpZone = parkourJumpTriggerDistance(distance);
        if (hDist > jumpZone || (allowJumps && jumpCooldown == 0)) {
            return;
        }

        pressParkourBrake();
    }

    private void handleJump(PathExecutor executor, Node waypoint, Vec3d playerPos,
                            int jumpCooldown, long lastProgressTime, int pursuitSegment) {
        if (waypoint.moveType() == Node.MoveType.PARKOUR
            && player.onGround()
            && isGroundedOnParkourLanding(waypoint, playerPos, pursuitSegment)) {
            input.setJumpDown(false);
            return;
        }
        if (waypoint.moveType() == Node.MoveType.PARKOUR
            && player.onGround()
            && !isParkourTakeoffReady(waypoint, playerPos, pursuitSegment)) {
            input.setJumpDown(false);
            return;
        }

        boolean partialSupportAscent = isPartialSupportWaypoint(waypoint);
        boolean inStairSequence = isInStairSequence(pursuitSegment);
        double dx = waypoint.position.centeredX() - playerPos.x();
        double dz = waypoint.position.centeredZ() - playerPos.z();
        double dy = waypoint.position.flooredY() - playerPos.y();
        double hDist = Math.sqrt(dx * dx + dz * dz);
        double jumpDistance = waypoint.moveType() == Node.MoveType.PARKOUR
            ? parkourJumpRemainingBlocks(waypoint, playerPos)
            : hDist;
        long millisSinceProgress = System.currentTimeMillis() - lastProgressTime;
        PathfinderSettings settings = PathfinderSettings.instance();

        MovementExecutionContext context = new MovementExecutionContext(
            waypoint,
            playerPos,
            partialSupportAscent,
            inStairSequence,
            player.onGround(),
            jumpCooldown,
            millisSinceProgress,
            jumpDistance,
            dy,
            settings.stepUpTriggerDist.value(),
            settings.jumpTriggerDist.value(),
            1.2,
            parkourDistanceBlocks(waypoint, pursuitSegment),
            settings.jumpCooldownTicks.value(),
            settings.stallJumpProgressMs.value());
        ExecutionRegistries.executors().get(waypoint.moveType()).handleJump(context);
        input.setJumpDown(context.jumpPressed());
        if (shouldAssistSlimeAscent(waypoint, hDist, jumpCooldown)) {
            input.setJumpDown(true);
            executor.setJumpCooldown(settings.jumpCooldownTicks.value());
            return;
        }
        if (context.jumpCooldownConsumed()) {
            executor.setJumpCooldown(waypoint.moveType() == Node.MoveType.PARKOUR
                ? 0
                : settings.jumpCooldownTicks.value());
        }
        if (waypoint.moveType() == Node.MoveType.PARKOUR && context.jumpPressed()) {
            input.setBackwardDown(false);
            input.setForwardDown(true);
            input.setSprintDown(false);
        }
    }

    private boolean isParkourTakeoffReady(Node waypoint, Vec3d playerPos, int pursuitSegment) {
        ParkourJumpGeometry geometry = parkourJumpGeometry(waypoint, pursuitSegment, playerPos);
        if (geometry == null || Math.abs(geometry.lateral()) > PARKOUR_TAKEOFF_MAX_LATERAL) {
            return false;
        }
        int distance = parkourDistanceBlocks(waypoint, pursuitSegment);
        if (geometry.remaining() > parkourJumpTriggerDistance(distance) + 0.20) {
            return false;
        }
        if (distance <= 2) {
            return true;
        }
        Vec3d velocity = player.velocity();
        double speedAlong = velocity.x() * geometry.dirX() + velocity.z() * geometry.dirZ();
        return speedAlong >= targetTakeoffSpeed(distance, geometry.verticalDelta()) - PARKOUR_TAKEOFF_SPEED_TOLERANCE;
    }

    private boolean isGroundedOnParkourLanding(Node waypoint, Vec3d playerPos, int pursuitSegment) {
        if (path.isEmpty()) {
            return false;
        }

        ParkourJumpGeometry geometry = parkourJumpGeometry(waypoint, pursuitSegment, playerPos);
        if (geometry == null) {
            return false;
        }

        double landingDx = Math.abs(playerPos.x() - waypoint.position.centeredX());
        double landingDz = Math.abs(playerPos.z() - waypoint.position.centeredZ());
        boolean inLandingBlock = landingDx <= 0.86 && landingDz <= 0.86;
        boolean landingHeight = playerPos.y() >= waypoint.position.flooredY() - 0.20
            && playerPos.y() <= waypoint.position.flooredY() + 0.35;
        return landingHeight
            && inLandingBlock
            && geometry.progress() >= geometry.jumpLen() - 1.20
            && Math.abs(geometry.lateral()) <= 1.05;
    }

    private ParkourJumpGeometry parkourJumpGeometry(Node waypoint, int pursuitSegment, Vec3d playerPos) {
        Node takeoff = path.get(clampedSegmentIndex(pursuitSegment));
        double startX = takeoff.position.centeredX();
        double startZ = takeoff.position.centeredZ();
        double dirX = waypoint.position.centeredX() - startX;
        double dirZ = waypoint.position.centeredZ() - startZ;
        double jumpLen = Math.sqrt(dirX * dirX + dirZ * dirZ);
        if (jumpLen < 1.0e-6) {
            return null;
        }

        double normalizedDirX = dirX / jumpLen;
        double normalizedDirZ = dirZ / jumpLen;
        double fromStartX = playerPos.x() - startX;
        double fromStartZ = playerPos.z() - startZ;
        double progress = fromStartX * normalizedDirX + fromStartZ * normalizedDirZ;
        double lateral = fromStartX * -normalizedDirZ + fromStartZ * normalizedDirX;
        double verticalDelta = (double) waypoint.position.flooredY() - takeoff.position.flooredY();
        return new ParkourJumpGeometry(
            normalizedDirX, normalizedDirZ, jumpLen, progress, jumpLen - progress, lateral, verticalDelta);
    }

    private void applyWaterMovement(Node movementWaypoint, Vec3d playerPos,
                                    double distToFinal, int pursuitSegment, boolean sneakLatched) {
        if (!player.isInWater()) {
            return;
        }

        Node nextWaypoint = path.get((int) Math.clamp(pursuitSegment + 2L, 0L, path.size() - 1L));
        WaterMovementContext waterContext = new WaterMovementContext(
            movementWaypoint,
            nextWaypoint,
            playerPos,
            distToFinal,
            player.isInWater(),
            world.isHeadUnderWater(player.eyePosition()));
        ExecutionRegistries.executors().get(movementWaypoint.moveType()).handleWaterMovement(waterContext);
        if (waterContext.handled()) {
            input.setJumpDown(waterContext.jumpPressed());
            if (!sneakLatched) {
                input.setSneakDown(waterContext.shiftPressed());
            }
            input.setSprintDown(waterContext.sprintPressed());
        } else if (movementWaypoint.position.flooredY() > blockY(playerPos)) {
            input.setJumpDown(true);
        } else if (!sneakLatched) {
            input.setSneakDown(false);
        }
    }

    private boolean applyWaterEntryMovement(boolean waterEntry, boolean sneakLatched) {
        if (player.isInWater() || !waterEntry) {
            return false;
        }

        input.setJumpDown(false);
        if (!sneakLatched) {
            input.setSneakDown(false);
        }
        input.setSprintDown(false);
        return true;
    }

    private Node waterEntryMovementTarget(Node movementWaypoint, int pursuitSegment) {
        if (pursuitSegment + 2 >= path.size()) {
            return movementWaypoint;
        }

        Node next = path.get(pursuitSegment + 2);
        return isWaterEntryWaypoint(next) ? next : movementWaypoint;
    }

    private void applyClimbMovement(Node movementWaypoint, boolean sneakLatched) {
        Vec3d pos = player.position();
        if (checker.isClimbable(blockX(pos), blockY(pos), blockZ(pos))) {
            input.setForwardDown(true);
            input.setBackwardDown(false);
            if (movementWaypoint.position.flooredY() > blockY(pos)) {
                input.setJumpDown(true);
            } else if (movementWaypoint.position.flooredY() < blockY(pos)) {
                input.setJumpDown(false);
                input.setSneakDown(true);
            }
        } else if (!sneakLatched) {
            input.setSneakDown(false);
        }
    }

    private boolean isInStairSequence(int pursuitSegment) {
        int stairCount = 0;
        int start = Math.clamp(pursuitSegment, 0, path.size());
        int checkEnd = (int) Math.clamp(start + 3L, 0L, path.size());
        for (int i = start; i < checkEnd; i++) {
            Node wp = path.get(i);
            if (PathfindingRegistries.movementEvaluators().get(wp.moveType()).countsAsStairSequence() && isPartialSupportWaypoint(wp)) {
                stairCount++;
            }
            if (i > 0
                    && wp.position.flooredY() < path.get(i - 1).position.flooredY()
                    && world.isPartialSupport(
                    wp.position.flooredX(),
                    wp.position.flooredY(),
                    wp.position.flooredZ()
            )) {
                stairCount++;
            }
        }
        return stairCount >= 2;
    }

    private int parkourDistanceBlocks(Node waypoint, int pursuitSegment) {
        if (waypoint.moveType() != Node.MoveType.PARKOUR || path.isEmpty()) {
            return 0;
        }
        Node takeoff = path.get(clampedSegmentIndex(pursuitSegment));
        int dx = Math.abs(waypoint.position.flooredX() - takeoff.position.flooredX());
        int dz = Math.abs(waypoint.position.flooredZ() - takeoff.position.flooredZ());
        return Math.max(dx, dz);
    }

    private boolean isPartialSupportWaypoint(Node wp) {
        return checker.isLowPartialSupport(wp.position.flooredX(), wp.position.flooredY(), wp.position.flooredZ())
            || world.isPartialSupport(wp.position.flooredX(), wp.position.flooredY(), wp.position.flooredZ());
    }

    private boolean shouldAssistSlimeAscent(Node waypoint, double horizontalDistance, int jumpCooldown) {
        Vec3d pos = player.position();
        return player.onGround()
            && jumpCooldown == 0
            && isOnSlimeSupport(pos)
            && waypoint.position.flooredY() > blockY(pos)
            && horizontalDistance < SLIME_ASCENT_JUMP_TRIGGER_DIST;
    }

    private boolean isOnSlimeSupport(Vec3d pos) {
        int x = blockX(pos);
        int y = blockY(pos);
        int z = blockZ(pos);
        return world.isSlime(x, y, z) || world.isSlime(x, y - 1, z);
    }

    private boolean isWaterEntryWaypoint(Node waypoint) {
        Vec3d pos = player.position();
        int x = waypoint.position.flooredX();
        int y = waypoint.position.flooredY();
        int z = waypoint.position.flooredZ();
        if (checker.isWater(x, y, z)) {
            return true;
        }
        return waypoint.moveType() == Node.MoveType.SWIM
            && checker.isWater(x, y - 1, z)
            && pos.y() - y <= WATER_ENTRY_SURFACE_DROP;
    }

    private boolean isForwardPressed(double dx, double dz, double forwardDot) {
        float yawRad = (float) Math.toRadians(player.yaw());
        double forwardX = -Math.sin(yawRad);
        double forwardZ = Math.cos(yawRad);
        return dx * forwardX + dz * forwardZ > forwardDot;
    }

    private void applyParkourAxisInput(double dirX, double dirZ, boolean forward) {
        InputApplier.applyRelativeMovement(
            player,
            input,
            forward ? dirX : -dirX,
            forward ? dirZ : -dirZ,
            0.15,
            -0.15,
            0.65);
        input.setSprintDown(false);
    }

    private void pressParkourBrake() {
        input.setForwardDown(false);
        input.setBackwardDown(true);
        input.setSprintDown(false);
        input.setLeftDown(false);
        input.setRightDown(false);
    }

    private void clearForwardBack() {
        input.setForwardDown(false);
        input.setBackwardDown(false);
        input.setSprintDown(false);
    }

    private double parkourJumpRemainingBlocks(Node waypoint, Vec3d playerPos) {
        double remainingX = Math.abs(waypoint.position.centeredX() - playerPos.x());
        double remainingZ = Math.abs(waypoint.position.centeredZ() - playerPos.z());
        return Math.max(remainingX, remainingZ);
    }

    private int clampedSegmentIndex(int i) {
        return Math.clamp(i, 0, path.size() - 1);
    }

    private static double parkourJumpTriggerDistance(int distance) {
        double margin = PARKOUR_LONG_TAKEOFF_MARGIN;
        if (distance <= 2) {
            margin = PARKOUR_SHORT_TAKEOFF_MARGIN;
        } else if (distance == 3) {
            margin = PARKOUR_MEDIUM_TAKEOFF_MARGIN;
        }
        return Math.max(1.2, distance - margin);
    }

    private static double targetTakeoffSpeed(int distance, double verticalDelta) {
        if (distance <= 2) {
            return verticalDelta > 0.25 ? 0.18 : 0.13;
        }
        if (distance == 3) {
            return verticalDelta > 0.25 ? 0.28 : 0.24;
        }
        return 0.32;
    }

    private static double targetAirSpeed(int distance, double verticalDelta) {
        double speed = 0.16;
        if (distance <= 2) {
            speed = 0.08;
        } else if (distance == 3) {
            speed = 0.12;
        }
        if (verticalDelta < -1.0) {
            speed += 0.04;
        }
        return speed;
    }

    private static ParkourAirPrediction predictParkourLanding(double playerY, double velocityY,
                                                              int landingY, double progress, double speedAlong) {
        double y = playerY;
        double vy = velocityY;
        double projectedProgress = progress;
        double v = speedAlong;
        int ticks = 0;
        while (ticks < 40) {
            projectedProgress += v;
            v *= PARKOUR_AIR_DRAG;
            y += vy;
            vy = (vy - PARKOUR_GRAVITY) * PARKOUR_VERTICAL_DRAG;
            ticks++;
            if (vy <= 0.0 && y <= landingY + 0.08) {
                break;
            }
        }
        return new ParkourAirPrediction(ticks, projectedProgress);
    }

    private static double landingAimOffset(int distance, double verticalDelta) {
        double offset = 0.06;
        if (distance <= 2) {
            offset = 0.18;
        } else if (distance == 3) {
            offset = 0.12;
        }
        if (distance <= 2 && verticalDelta > 0.25) {
            offset = 0.04;
        }
        if (verticalDelta < -1.0) {
            offset += 0.10;
        }
        return offset;
    }

    private static double landingBrakeRemaining(int distance, double verticalDelta, boolean constrainedLanding) {
        double base = constrainedLanding ? PARKOUR_CONSTRAINED_BRAKE_REMAINING : PARKOUR_OPEN_BRAKE_REMAINING;
        if (distance <= 2) {
            base = Math.max(base, 0.72);
        } else if (distance == 3) {
            base = Math.max(base, 0.55);
        } else {
            base = Math.max(base, 0.38);
        }
        if (verticalDelta < -1.0) {
            base += 0.15;
        }
        return base;
    }

    private static int blockX(Vec3d pos) {
        return (int) Math.floor(pos.x());
    }

    private static int blockY(Vec3d pos) {
        return (int) Math.floor(pos.y());
    }

    private static int blockZ(Vec3d pos) {
        return (int) Math.floor(pos.z());
    }

    private record ParkourAirPrediction(int ticks, double progress) {
    }

    private record ParkourVelocityState(double dirX, double dirZ, double jumpLen, double progress,
                                        double remaining, double speedAlong, int distance, double verticalDelta,
                                        ParkourExecutionProfile profile) {
    }

    private record ParkourJumpGeometry(double dirX, double dirZ, double jumpLen, double progress,
                                       double remaining, double lateral, double verticalDelta) {
    }

    private record ParkourExecutionProfile(boolean immediateChain, boolean shortAscentCarry, boolean descending,
                                           boolean constrainedLanding) {
        boolean allowLandingBrake() {
            return !immediateChain || (descending && !constrainedLanding);
        }

        boolean allowLandingHold() {
            return constrainedLanding && !immediateChain;
        }
    }
}
