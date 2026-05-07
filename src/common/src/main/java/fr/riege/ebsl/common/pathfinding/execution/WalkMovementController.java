package fr.riege.ebsl.common.pathfinding.execution;

import fr.riege.ebsl.common.platform.layer.IPhysicsLayer;
import fr.riege.ebsl.common.platform.layer.IPlayerLayer;
import fr.riege.ebsl.common.platform.layer.IWorldLayer;
import fr.riege.ebsl.common.math.Vec3d;
import fr.riege.ebsl.common.pathfinding.Node;
import fr.riege.ebsl.common.pathfinding.movement.WalkabilityChecker;
import fr.riege.ebsl.common.pathfinding.movement.types.evaluation.MovementEvaluatorRegistry;
import fr.riege.ebsl.common.pathfinding.movement.types.evaluation.MovementValidationContext;
import fr.riege.ebsl.common.pathfinding.movement.types.evaluation.MovementValidationResult;
import fr.riege.ebsl.common.pathfinding.movement.types.execution.MovementExecutionContext;
import fr.riege.ebsl.common.pathfinding.movement.types.execution.MovementExecutorRegistry;
import fr.riege.ebsl.common.pathfinding.movement.types.execution.WaterMovementContext;
import fr.riege.ebsl.common.pathfinding.settings.PathfinderSettings;

import java.util.List;

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

    private final IWorldLayer world;
    private final IPlayerLayer player;
    private final IPhysicsLayer physics;
    private final WalkabilityChecker checker;
    private List<Node> path;

    WalkMovementController(IWorldLayer world, IPlayerLayer player, IPhysicsLayer physics, WalkabilityChecker checker) {
        this.world = world;
        this.player = player;
        this.physics = physics;
        this.checker = checker;
    }

    void setPath(List<Node> path) {
        this.path = path;
    }

    MovementValidationResult validateCurrentSegment(Vec3d playerPos, int pursuitSegment) {
        if (path == null || path.size() < 2 || pursuitSegment + 1 >= path.size()) {
            return MovementValidationResult.ok();
        }
        int currentIndex = Math.clamp(pursuitSegment, 0, path.size() - 1);
        int targetIndex = Math.min(path.size() - 1, currentIndex + 1);
        Node from = path.get(currentIndex);
        Node target = path.get(targetIndex);
        Node next = path.get(Math.min(path.size() - 1, targetIndex + 1));
        MovementValidationContext context = new MovementValidationContext(
            checker,
            from,
            target,
            next,
            playerPos,
            pursuitSegment);
        return MovementEvaluatorRegistry.get(target.moveType).validate(context);
    }

    void apply(PathExecutor executor, Vec3d playerPos, Node movementWaypoint,
               double distToFinal, boolean allowJumps, boolean sneakLatched,
               int pursuitSegment, int jumpCooldown, long lastProgressTime) {
        double dxWp = movementWaypoint.position.centeredX() - playerPos.x();
        double dzWp = movementWaypoint.position.centeredZ() - playerPos.z();
        boolean partialSupportWaypoint = isPartialSupportWaypoint(movementWaypoint);
        boolean nearWaypoint = Math.sqrt(dxWp * dxWp + dzWp * dzWp) < 2.0;
        boolean nearStepUp = MovementEvaluatorRegistry.get(movementWaypoint.moveType).reducesSprintNearWaypoint()
            && nearWaypoint;
        boolean nearPartialSupportJump = movementWaypoint.moveType == Node.MoveType.JUMP
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
            physics.setJump(player.isInWater());
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

        if (hDist < PathfinderSettings.instance().walkTargetDeadzone.value()
            && pursuitSegment + 2 < path.size()
            && !isParkourTransitionWindow(pursuitSegment)) {
            targetWp = path.get(pursuitSegment + 2);
            dx = targetWp.position.centeredX() - playerPos.x();
            dz = targetWp.position.centeredZ() - playerPos.z();
            hDist = Math.sqrt(dx * dx + dz * dz);
        }

        if (hDist < PathfinderSettings.instance().walkTargetDeadzone.value()) {
            physics.setForward(forceForwardWhenCentered);
            physics.setBackward(false);
            physics.setLeft(false);
            physics.setRight(false);
            physics.setSprint(false);
            return;
        }

        PathSteering.SteeringVector steering = PathSteering.steer(checker, path, playerPos, targetWp, pursuitSegment);
        InputApplier.applyRelativeMovement(
            player, physics, steering.x(), steering.z(),
            PathfinderSettings.instance().walkForwardDot.value(),
            PathfinderSettings.instance().walkBackwardDot.value(),
            PathfinderSettings.instance().walkStrafeDot.value());
        physics.setSprint(isForwardPressed(steering.x(), steering.z())
            && distToFinal > 2.0
            && !nearStepUp
            && (!steering.nearCorner() || !PathfinderSettings.instance().cornerSteeringSlowdown.value()));
    }

    private boolean isParkourTransitionWindow(int pursuitSegment) {
        int start = Math.max(0, pursuitSegment);
        int end = Math.min(path.size() - 1, start + 2);
        for (int i = start; i <= end; i++) {
            if (path.get(i).moveType == Node.MoveType.PARKOUR) {
                return true;
            }
        }
        return false;
    }

    private void applyParkourLandingControl(Vec3d playerPos, Node waypoint, int pursuitSegment) {
        if (waypoint.moveType != Node.MoveType.PARKOUR || path.isEmpty()) {
            return;
        }

        Node takeoff = path.get(clampedSegmentIndex(pursuitSegment));
        double startX = takeoff.position.centeredX();
        double startZ = takeoff.position.centeredZ();
        double targetX = waypoint.position.centeredX();
        double targetZ = waypoint.position.centeredZ();
        double dirX = targetX - startX;
        double dirZ = targetZ - startZ;
        double jumpLen = Math.sqrt(dirX * dirX + dirZ * dirZ);
        if (jumpLen < 1.0e-6) {
            return;
        }

        dirX /= jumpLen;
        dirZ /= jumpLen;
        double fromStartX = playerPos.x() - startX;
        double fromStartZ = playerPos.z() - startZ;
        double progress = fromStartX * dirX + fromStartZ * dirZ;
        double remaining = jumpLen - progress;
        double lateral = fromStartX * -dirZ + fromStartZ * dirX;
        Vec3d velocity = player.velocity();
        double velocityAlong = velocity.x() * dirX + velocity.z() * dirZ;
        double verticalDelta = (double) waypoint.position.flooredY() - takeoff.position.flooredY();
        int distance = parkourDistanceBlocks(waypoint, pursuitSegment);
        boolean constrainedLanding = isConstrainedParkourLanding(waypoint, dirX, dirZ);
        ParkourExecutionProfile profile = parkourExecutionProfile(pursuitSegment, distance, verticalDelta, constrainedLanding);
        if (!profile.allowLandingBrake()) {
            return;
        }
        double brakeRemaining = landingBrakeRemaining(distance, verticalDelta, constrainedLanding);
        boolean inLandingColumn = blockX(playerPos) == waypoint.position.flooredX()
            && blockZ(playerPos) == waypoint.position.flooredZ();
        boolean shouldBrake = false;
        if (!player.onGround() && remaining <= brakeRemaining && velocityAlong > PARKOUR_MIN_BRAKE_SPEED) {
            ParkourAirPrediction prediction = predictParkourLanding(
                playerPos.y(), velocity.y(), waypoint.position.flooredY(), progress, velocityAlong);
            double targetProgress = jumpLen - landingAimOffset(distance, verticalDelta);
            double frontMargin = PARKOUR_LANDING_FRONT_MARGIN;
            if (profile.immediateChain() && !constrainedLanding) {
                frontMargin += PARKOUR_CHAIN_OVERSHOOT_MARGIN;
            }
            shouldBrake = prediction.progress() > targetProgress + frontMargin;
        }
        boolean shouldHoldLanding = profile.allowLandingHold() && player.onGround()
            && (inLandingColumn || remaining <= PARKOUR_GROUNDED_BRAKE_OVERSHOOT);

        if (!shouldBrake && !shouldHoldLanding) {
            return;
        }

        physics.setForward(false);
        physics.setBackward(true);
        physics.setSprint(false);
        if (Math.abs(lateral) <= PARKOUR_LATERAL_CORRECTION) {
            physics.setLeft(false);
            physics.setRight(false);
        }
    }

    private void applyParkourVelocityControl(Vec3d playerPos, Node waypoint, int pursuitSegment) {
        if (waypoint.moveType != Node.MoveType.PARKOUR || path.isEmpty()) {
            return;
        }

        Node takeoff = path.get(clampedSegmentIndex(pursuitSegment));
        double startX = takeoff.position.centeredX();
        double startZ = takeoff.position.centeredZ();
        double targetX = waypoint.position.centeredX();
        double targetZ = waypoint.position.centeredZ();
        double dirX = targetX - startX;
        double dirZ = targetZ - startZ;
        double jumpLen = Math.sqrt(dirX * dirX + dirZ * dirZ);
        if (jumpLen < 1.0e-6) {
            return;
        }

        dirX /= jumpLen;
        dirZ /= jumpLen;
        double fromStartX = playerPos.x() - startX;
        double fromStartZ = playerPos.z() - startZ;
        double progress = fromStartX * dirX + fromStartZ * dirZ;
        double remaining = jumpLen - progress;
        Vec3d velocity = player.velocity();
        double speedAlong = velocity.x() * dirX + velocity.z() * dirZ;
        int distance = parkourDistanceBlocks(waypoint, pursuitSegment);
        double verticalDelta = (double) waypoint.position.flooredY() - takeoff.position.flooredY();
        boolean constrainedLanding = isConstrainedParkourLanding(waypoint, dirX, dirZ);
        ParkourExecutionProfile profile = parkourExecutionProfile(pursuitSegment, distance, verticalDelta, constrainedLanding);

        if (player.onGround()) {
            double trigger = parkourJumpTriggerDistance(distance);
            double targetSpeed = targetTakeoffSpeed(distance, verticalDelta);
            boolean shortAscentCarry = profile.shortAscentCarry();
            if (!shortAscentCarry && remaining <= trigger + 0.35 && speedAlong > targetSpeed + 0.025) {
                pressParkourBrake();
                return;
            }
            physics.setSprint(distance >= 3 && verticalDelta >= -1.0);
            if (shortAscentCarry && speedAlong < PARKOUR_SHORT_ASCENT_CARRY_SPEED) {
                applyParkourAxisInput(dirX, dirZ, true);
            }
            return;
        }

        ParkourAirPrediction prediction = predictParkourLanding(
            playerPos.y(), velocity.y(), waypoint.position.flooredY(), progress, speedAlong);
        double targetProgress = jumpLen - landingAimOffset(distance, verticalDelta);
        double frontLimit = targetProgress + PARKOUR_LANDING_FRONT_MARGIN;
        double backLimit = targetProgress - PARKOUR_LANDING_BACK_MARGIN;
        boolean inLandingColumn = blockX(playerPos) == waypoint.position.flooredX()
            && blockZ(playerPos) == waypoint.position.flooredZ();
        if (prediction.progress() > frontLimit) {
            if (profile.shortAscentCarry() && remaining <= 0.55) {
                applyParkourAxisInput(dirX, dirZ, true);
                return;
            }
            if (profile.immediateChain() && prediction.progress() <= frontLimit + PARKOUR_CHAIN_OVERSHOOT_MARGIN) {
                clearForwardBack();
                return;
            }
            applyParkourAxisInput(dirX, dirZ, false);
            return;
        }
        if (!inLandingColumn || prediction.progress() < backLimit || speedAlong < targetAirSpeed(distance, verticalDelta)) {
            applyParkourAxisInput(dirX, dirZ, true);
            physics.setSprint(false);
            return;
        }
        if (profile.shortAscentCarry()) {
            applyParkourAxisInput(dirX, dirZ, true);
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
        return path.get(nextIndex).moveType;
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
        if (waypoint.moveType != Node.MoveType.PARKOUR || !player.onGround()) {
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
        if (waypoint.moveType == Node.MoveType.PARKOUR
            && player.onGround()
            && isGroundedOnParkourLanding(waypoint, playerPos, pursuitSegment)) {
            physics.setJump(false);
            return;
        }

        boolean partialSupportAscent = isPartialSupportWaypoint(waypoint);
        boolean inStairSequence = isInStairSequence(pursuitSegment);
        double dx = waypoint.position.centeredX() - playerPos.x();
        double dz = waypoint.position.centeredZ() - playerPos.z();
        double dy = waypoint.position.flooredY() - playerPos.y();
        double hDist = Math.sqrt(dx * dx + dz * dz);
        double jumpDistance = waypoint.moveType == Node.MoveType.PARKOUR
            ? parkourJumpRemainingBlocks(waypoint, playerPos)
            : hDist;
        long millisSinceProgress = System.currentTimeMillis() - lastProgressTime;

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
            PathfinderSettings.instance().stepUpTriggerDist.value(),
            PathfinderSettings.instance().jumpTriggerDist.value(),
            1.2,
            parkourDistanceBlocks(waypoint, pursuitSegment),
            PathfinderSettings.instance().jumpCooldownTicks.value(),
            PathfinderSettings.instance().stallJumpProgressMs.value());
        MovementExecutorRegistry.get(waypoint.moveType).handleJump(context);
        physics.setJump(context.jumpPressed());
        if (shouldAssistSlimeAscent(waypoint, hDist, jumpCooldown)) {
            physics.setJump(true);
            executor.setJumpCooldown(PathfinderSettings.instance().jumpCooldownTicks.value());
            return;
        }
        if (context.jumpCooldownConsumed()) {
            executor.setJumpCooldown(waypoint.moveType == Node.MoveType.PARKOUR
                ? 0
                : PathfinderSettings.instance().jumpCooldownTicks.value());
        }
        if (waypoint.moveType == Node.MoveType.PARKOUR && context.jumpPressed()) {
            physics.setBackward(false);
            physics.setForward(true);
            physics.setSprint(false);
        }
    }

    private boolean isGroundedOnParkourLanding(Node waypoint, Vec3d playerPos, int pursuitSegment) {
        if (path.isEmpty()) {
            return false;
        }

        Node takeoff = path.get(clampedSegmentIndex(pursuitSegment));
        double startX = takeoff.position.centeredX();
        double startZ = takeoff.position.centeredZ();
        double targetX = waypoint.position.centeredX();
        double targetZ = waypoint.position.centeredZ();
        double dirX = targetX - startX;
        double dirZ = targetZ - startZ;
        double len = Math.sqrt(dirX * dirX + dirZ * dirZ);
        if (len < 1.0e-6) {
            return false;
        }

        dirX /= len;
        dirZ /= len;
        double fromStartX = playerPos.x() - startX;
        double fromStartZ = playerPos.z() - startZ;
        double progress = fromStartX * dirX + fromStartZ * dirZ;
        double lateral = fromStartX * -dirZ + fromStartZ * dirX;
        double landingDx = Math.abs(playerPos.x() - waypoint.position.centeredX());
        double landingDz = Math.abs(playerPos.z() - waypoint.position.centeredZ());
        boolean inLandingBlock = landingDx <= 0.86 && landingDz <= 0.86;
        boolean landingHeight = playerPos.y() >= waypoint.position.flooredY() - 0.20
            && playerPos.y() <= waypoint.position.flooredY() + 0.35;
        return landingHeight && inLandingBlock && progress >= len - 1.20 && Math.abs(lateral) <= 1.05;
    }

    private void applyWaterMovement(Node movementWaypoint, Vec3d playerPos,
                                    double distToFinal, int pursuitSegment, boolean sneakLatched) {
        if (!player.isInWater()) {
            return;
        }

        Node nextWaypoint = path.get(Math.min(path.size() - 1, pursuitSegment + 2));
        WaterMovementContext waterContext = new WaterMovementContext(
            movementWaypoint,
            nextWaypoint,
            playerPos,
            distToFinal,
            player.isInWater(),
            world.isHeadUnderWater(player.eyePosition()));
        MovementExecutorRegistry.get(movementWaypoint.moveType).handleWaterMovement(waterContext);
        if (waterContext.handled()) {
            physics.setJump(waterContext.jumpPressed());
            if (!sneakLatched) {
                physics.setSneak(waterContext.shiftPressed());
            }
            physics.setSprint(waterContext.sprintPressed());
        } else if (movementWaypoint.position.flooredY() > blockY(playerPos)) {
            physics.setJump(true);
        } else if (!sneakLatched) {
            physics.setSneak(false);
        }
    }

    private boolean applyWaterEntryMovement(boolean waterEntry, boolean sneakLatched) {
        if (player.isInWater() || !waterEntry) {
            return false;
        }

        physics.setJump(false);
        if (!sneakLatched) {
            physics.setSneak(false);
        }
        physics.setSprint(false);
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
            physics.setForward(true);
            physics.setBackward(false);
            if (movementWaypoint.position.flooredY() > blockY(pos)) {
                physics.setJump(true);
            } else if (movementWaypoint.position.flooredY() < blockY(pos)) {
                physics.setJump(false);
                physics.setSneak(true);
            }
        } else if (!sneakLatched) {
            physics.setSneak(false);
        }
    }

    private boolean isInStairSequence(int pursuitSegment) {
        int stairCount = 0;
        int checkEnd = Math.min(path.size(), pursuitSegment + 3);
        for (int i = pursuitSegment; i < checkEnd; i++) {
            Node wp = path.get(i);
            if (MovementEvaluatorRegistry.get(wp.moveType).countsAsStairSequence() && isPartialSupportWaypoint(wp)) {
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
        if (waypoint.moveType != Node.MoveType.PARKOUR || path.isEmpty()) {
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
        return waypoint.moveType == Node.MoveType.SWIM
            && checker.isWater(x, y - 1, z)
            && pos.y() - y <= WATER_ENTRY_SURFACE_DROP;
    }

    private boolean isForwardPressed(double dx, double dz) {
        float yawRad = (float) Math.toRadians(player.yaw());
        double forwardX = -Math.sin(yawRad);
        double forwardZ = Math.cos(yawRad);
        return dx * forwardX + dz * forwardZ > PathfinderSettings.instance().walkForwardDot.value();
    }

    private void applyParkourAxisInput(double dirX, double dirZ, boolean forward) {
        InputApplier.applyRelativeMovement(
            player,
            physics,
            forward ? dirX : -dirX,
            forward ? dirZ : -dirZ,
            0.15,
            -0.15,
            0.65);
        physics.setSprint(false);
    }

    private void pressParkourBrake() {
        physics.setForward(false);
        physics.setBackward(true);
        physics.setSprint(false);
        physics.setLeft(false);
        physics.setRight(false);
    }

    private void clearForwardBack() {
        physics.setForward(false);
        physics.setBackward(false);
        physics.setSprint(false);
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
        double margin = distance <= 2
            ? PARKOUR_SHORT_TAKEOFF_MARGIN
            : (distance == 3 ? PARKOUR_MEDIUM_TAKEOFF_MARGIN : PARKOUR_LONG_TAKEOFF_MARGIN);
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
        double speed = distance <= 2 ? 0.08 : (distance == 3 ? 0.12 : 0.16);
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
        double offset = distance <= 2 ? 0.18 : (distance == 3 ? 0.12 : 0.06);
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
