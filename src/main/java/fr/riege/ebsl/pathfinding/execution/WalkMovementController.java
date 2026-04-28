package fr.riege.ebsl.pathfinding.execution;

import fr.riege.ebsl.pathfinding.Node;
import fr.riege.ebsl.pathfinding.annotation.PathingStage;
import fr.riege.ebsl.pathfinding.movement.WalkabilityChecker;
import fr.riege.ebsl.pathfinding.movement.types.execution.MovementExecutionContext;
import fr.riege.ebsl.pathfinding.movement.types.evaluation.MovementEvaluatorRegistry;
import fr.riege.ebsl.pathfinding.movement.types.execution.MovementExecutorRegistry;
import fr.riege.ebsl.pathfinding.movement.types.evaluation.MovementValidationContext;
import fr.riege.ebsl.pathfinding.movement.types.evaluation.MovementValidationResult;
import fr.riege.ebsl.pathfinding.movement.types.execution.WaterMovementContext;
import fr.riege.ebsl.pathfinding.settings.PathfinderSettings;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

import java.util.List;

@PathingStage(PathingStage.Stage.EXECUTION)
final class WalkMovementController {
    private List<Node> path;
    private Level checkerLevel;
    private WalkabilityChecker checker;
    private static final double SLIME_ASCENT_JUMP_TRIGGER_DIST = 1.7;
    private static final double WATER_ENTRY_SURFACE_DROP = 0.6;
    private static final double PARKOUR_OPEN_BRAKE_REMAINING = 0.25;
    private static final double PARKOUR_CONSTRAINED_BRAKE_REMAINING = 0.45;
    private static final double PARKOUR_GROUNDED_BRAKE_OVERSHOOT = -0.12;
    private static final double PARKOUR_MIN_BRAKE_SPEED = 0.035;
    private static final double PARKOUR_LATERAL_CORRECTION = 0.18;

    WalkMovementController(List<Node> path) {
        this.path = path;
    }

    void setPath(List<Node> path) {
        this.path = path;
    }

    MovementValidationResult validateCurrentSegment(Minecraft mc, Vec3 playerPos, int pursuitSegment) {
        if (mc.level == null || path.size() < 2 || pursuitSegment + 1 >= path.size()) {
            return MovementValidationResult.ok();
        }
        int currentIndex = Math.max(0, Math.min(pursuitSegment, path.size() - 1));
        int targetIndex = Math.min(path.size() - 1, currentIndex + 1);
        Node from = path.get(currentIndex);
        Node target = path.get(targetIndex);
        Node next = path.get(Math.min(path.size() - 1, targetIndex + 1));
        MovementValidationContext context = new MovementValidationContext(
            mc,
            checker(mc),
            from,
            target,
            next,
            playerPos,
            pursuitSegment);
        return MovementEvaluatorRegistry.get(target.moveType).validate(context);
    }

    void apply(PathExecutor executor, Minecraft mc, Vec3 playerPos, Node movementWaypoint,
               double distToFinal, boolean allowJumps, boolean sneakLatched,
               int pursuitSegment, int jumpCooldown, long lastProgressTime) {
        double dxWp = movementWaypoint.position.centeredX() - playerPos.x;
        double dzWp = movementWaypoint.position.centeredZ() - playerPos.z;
        boolean partialSupportWaypoint = isPartialSupportWaypoint(mc, movementWaypoint);
        boolean nearStepUp = MovementEvaluatorRegistry.get(movementWaypoint.moveType).reducesSprintNearWaypoint()
            && Math.sqrt(dxWp * dxWp + dzWp * dzWp) < 2.0;
        boolean nearPartialSupportJump = movementWaypoint.moveType == Node.MoveType.JUMP
            && partialSupportWaypoint
            && Math.sqrt(dxWp * dxWp + dzWp * dzWp) < 2.0;

        boolean waterEntry = isWaterEntryWaypoint(mc, movementWaypoint);
        Node pathMovementWaypoint = waterEntry
            ? waterEntryMovementTarget(mc, movementWaypoint, pursuitSegment)
            : movementWaypoint;

        applyPathMovement(mc, playerPos, pathMovementWaypoint, distToFinal,
            nearStepUp || nearPartialSupportJump, pursuitSegment, waterEntry);

        if (applyWaterEntryMovement(mc, waterEntry, sneakLatched)) {
            return;
        }

        String parkourDecision = "path";
        String landingDecision = applyParkourLandingControl(mc, playerPos, movementWaypoint, pursuitSegment);
        if (!"path".equals(landingDecision)) {
            parkourDecision = landingDecision;
        }
        String takeoffDecision = applyParkourTakeoffGuard(mc, movementWaypoint, playerPos, pursuitSegment, jumpCooldown, allowJumps);
        if (!"path".equals(takeoffDecision)) {
            parkourDecision = takeoffDecision;
        }

        if (!allowJumps) {
            mc.options.keyJump.setDown(mc.player != null && mc.player.isInWater());
            ParkourExecutionTelemetry.record(mc, path, playerPos, movementWaypoint, pursuitSegment,
                jumpCooldown, false, parkourDecision + ":jumps-off");
            return;
        }

        if (handleJump(executor, mc, movementWaypoint, playerPos, jumpCooldown, lastProgressTime, pursuitSegment)) {
            parkourDecision = "jump";
        }
        applyWaterMovement(mc, movementWaypoint, playerPos, distToFinal, pursuitSegment, sneakLatched);
        applyClimbMovement(mc, movementWaypoint, sneakLatched);
        ParkourExecutionTelemetry.record(mc, path, playerPos, movementWaypoint, pursuitSegment,
            jumpCooldown, allowJumps, parkourDecision);
    }

    private void applyPathMovement(Minecraft mc, Vec3 playerPos, Node targetWp,
                                   double distToFinal, boolean nearStepUp, int pursuitSegment,
                                   boolean forceForwardWhenCentered) {
        if (mc.player == null) {
            return;
        }

        double dx = targetWp.position.centeredX() - playerPos.x;
        double dz = targetWp.position.centeredZ() - playerPos.z;
        double hDist = Math.sqrt(dx * dx + dz * dz);

        if (hDist < PathfinderSettings.instance().walkTargetDeadzone.value() && pursuitSegment + 2 < path.size()) {
            targetWp = path.get(pursuitSegment + 2);
            dx = targetWp.position.centeredX() - playerPos.x;
            dz = targetWp.position.centeredZ() - playerPos.z;
            hDist = Math.sqrt(dx * dx + dz * dz);
        }

        if (hDist < PathfinderSettings.instance().walkTargetDeadzone.value()) {
            if (forceForwardWhenCentered) {
                mc.options.keyUp.setDown(true);
                mc.options.keyDown.setDown(false);
                mc.options.keyLeft.setDown(false);
                mc.options.keyRight.setDown(false);
                mc.options.keySprint.setDown(false);
                return;
            }
            mc.options.keyUp.setDown(false);
            mc.options.keyDown.setDown(false);
            mc.options.keyLeft.setDown(false);
            mc.options.keyRight.setDown(false);
            mc.options.keySprint.setDown(false);
            return;
        }

        PathSteering.SteeringVector steering = PathSteering.steer(
            checker(mc),
            path,
            playerPos,
            targetWp,
            pursuitSegment);
        InputApplier.applyRelativeMovement(
            mc, steering.x(), steering.z(), PathfinderSettings.instance().walkForwardDot.value(),
            PathfinderSettings.instance().walkBackwardDot.value(), PathfinderSettings.instance().walkStrafeDot.value());
        mc.options.keySprint.setDown(mc.options.keyUp.isDown()
            && !mc.options.keyDown.isDown()
            && distToFinal > 2.0
            && !nearStepUp
            && (!steering.nearCorner() || !PathfinderSettings.instance().cornerSteeringSlowdown.value()));
    }

    private String applyParkourLandingControl(Minecraft mc, Vec3 playerPos, Node waypoint, int pursuitSegment) {
        if (mc.player == null || waypoint.moveType != Node.MoveType.PARKOUR || path.isEmpty()) {
            return "path";
        }

        Node takeoff = path.get(Math.max(0, Math.min(pursuitSegment, path.size() - 1)));
        double startX = takeoff.position.centeredX();
        double startZ = takeoff.position.centeredZ();
        double targetX = waypoint.position.centeredX();
        double targetZ = waypoint.position.centeredZ();
        double dirX = targetX - startX;
        double dirZ = targetZ - startZ;
        double jumpLen = Math.sqrt(dirX * dirX + dirZ * dirZ);
        if (jumpLen < 1.0e-6) {
            return "path";
        }

        dirX /= jumpLen;
        dirZ /= jumpLen;
        double fromStartX = playerPos.x - startX;
        double fromStartZ = playerPos.z - startZ;
        double progress = fromStartX * dirX + fromStartZ * dirZ;
        double remaining = jumpLen - progress;
        double lateral = fromStartX * -dirZ + fromStartZ * dirX;
        double velocityAlong = mc.player.getDeltaMovement().x * dirX + mc.player.getDeltaMovement().z * dirZ;
        boolean constrainedLanding = isConstrainedParkourLanding(mc, waypoint, dirX, dirZ);
        double brakeRemaining = constrainedLanding ? PARKOUR_CONSTRAINED_BRAKE_REMAINING : PARKOUR_OPEN_BRAKE_REMAINING;
        boolean inLandingColumn = mc.player.getBlockX() == waypoint.position.flooredX()
            && mc.player.getBlockZ() == waypoint.position.flooredZ();
        boolean shouldBrake = !mc.player.onGround() && remaining <= brakeRemaining && velocityAlong > PARKOUR_MIN_BRAKE_SPEED;
        boolean shouldHoldLanding = constrainedLanding && mc.player.onGround()
            && (inLandingColumn || remaining <= PARKOUR_GROUNDED_BRAKE_OVERSHOOT);

        if (!shouldBrake && !shouldHoldLanding) {
            return "path";
        }

        mc.options.keyUp.setDown(false);
        mc.options.keyDown.setDown(true);
        mc.options.keySprint.setDown(false);
        if (Math.abs(lateral) <= PARKOUR_LATERAL_CORRECTION) {
            mc.options.keyLeft.setDown(false);
            mc.options.keyRight.setDown(false);
        }
        return shouldHoldLanding ? "landing-hold" : "landing-brake";
    }

    private boolean isConstrainedParkourLanding(Minecraft mc, Node landing, double dirX, double dirZ) {
        if (mc.level == null) {
            return false;
        }

        int stepX = Math.abs(dirX) > Math.abs(dirZ) ? (int) Math.signum(dirX) : 0;
        int stepZ = stepX == 0 ? (int) Math.signum(dirZ) : 0;
        int aheadX = landing.position.flooredX() + stepX;
        int aheadY = landing.position.flooredY();
        int aheadZ = landing.position.flooredZ() + stepZ;
        return !checker(mc).isWalkable(aheadX, aheadY, aheadZ);
    }

    private String applyParkourTakeoffGuard(Minecraft mc, Node waypoint, Vec3 playerPos, int pursuitSegment,
                                          int jumpCooldown, boolean allowJumps) {
        if (mc.player == null || waypoint.moveType != Node.MoveType.PARKOUR || !mc.player.onGround()) {
            return "path";
        }

        double dx = waypoint.position.centeredX() - playerPos.x;
        double dz = waypoint.position.centeredZ() - playerPos.z;
        double hDist = Math.sqrt(dx * dx + dz * dz);
        int distance = parkourDistanceBlocks(waypoint, pursuitSegment);
        double jumpZone = Math.max(1.2, distance - 0.45);
        if (hDist > jumpZone || (allowJumps && jumpCooldown == 0)) {
            return "path";
        }

        mc.options.keyUp.setDown(false);
        mc.options.keyDown.setDown(true);
        mc.options.keySprint.setDown(false);
        mc.options.keyLeft.setDown(false);
        mc.options.keyRight.setDown(false);
        return "takeoff-guard";
    }

    private boolean handleJump(PathExecutor executor, Minecraft mc, Node waypoint, Vec3 playerPos,
                            int jumpCooldown, long lastProgressTime, int pursuitSegment) {
        boolean partialSupportAscent = isPartialSupportWaypoint(mc, waypoint);
        boolean inStairSequence = isInStairSequence(mc, pursuitSegment);
        double dx = waypoint.position.centeredX() - playerPos.x;
        double dz = waypoint.position.centeredZ() - playerPos.z;
        double dy = waypoint.position.flooredY() - mc.player.getY();
        double hDist = Math.sqrt(dx * dx + dz * dz);
        long millisSinceProgress = System.currentTimeMillis() - lastProgressTime;

        MovementExecutionContext context = new MovementExecutionContext(
            mc,
            waypoint,
            playerPos,
            partialSupportAscent,
            inStairSequence,
            mc.player.onGround(),
            jumpCooldown,
            millisSinceProgress,
            hDist,
            dy,
            PathfinderSettings.instance().stepUpTriggerDist.value(),
            PathfinderSettings.instance().jumpTriggerDist.value(),
            1.2,
            parkourDistanceBlocks(waypoint, pursuitSegment),
            PathfinderSettings.instance().jumpCooldownTicks.value(),
            PathfinderSettings.instance().stallJumpProgressMs.value()
        );
        MovementExecutorRegistry.get(waypoint.moveType).handleJump(context);
        mc.options.keyJump.setDown(context.jumpPressed());
        if (shouldAssistSlimeAscent(mc, waypoint, hDist, jumpCooldown)) {
            mc.options.keyJump.setDown(true);
            executor.setJumpCooldown(PathfinderSettings.instance().jumpCooldownTicks.value());
            return true;
        }
        if (context.jumpCooldownConsumed()) {
            executor.setJumpCooldown(waypoint.moveType == Node.MoveType.PARKOUR
                ? 0
                : PathfinderSettings.instance().jumpCooldownTicks.value());
        }
        return context.jumpPressed();
    }

    private void applyWaterMovement(Minecraft mc, Node movementWaypoint, Vec3 playerPos,
                                    double distToFinal, int pursuitSegment, boolean sneakLatched) {
        if (!mc.player.isInWater()) {
            return;
        }

        Node nextWaypoint = path.get(Math.min(path.size() - 1, pursuitSegment + 2));
        WaterMovementContext waterContext = new WaterMovementContext(
            mc, movementWaypoint, nextWaypoint, playerPos, distToFinal);
        MovementExecutorRegistry.get(movementWaypoint.moveType).handleWaterMovement(waterContext);
        if (waterContext.handled()) {
            mc.options.keyJump.setDown(waterContext.jumpPressed());
            if (!sneakLatched) {
                mc.options.keyShift.setDown(waterContext.shiftPressed());
            }
            mc.options.keySprint.setDown(waterContext.sprintPressed());
        } else if (movementWaypoint.position.flooredY() > mc.player.getBlockY()) {
            mc.options.keyJump.setDown(true);
        } else if (!sneakLatched) {
            mc.options.keyShift.setDown(false);
        }
    }

    private boolean applyWaterEntryMovement(Minecraft mc, boolean waterEntry, boolean sneakLatched) {
        if (mc.player == null || mc.level == null || mc.player.isInWater()) {
            return false;
        }
        if (!waterEntry) {
            return false;
        }

        mc.options.keyJump.setDown(false);
        if (!sneakLatched) {
            mc.options.keyShift.setDown(false);
        }
        mc.options.keySprint.setDown(false);
        return true;
    }

    private Node waterEntryMovementTarget(Minecraft mc, Node movementWaypoint, int pursuitSegment) {
        if (pursuitSegment + 2 >= path.size()) {
            return movementWaypoint;
        }

        Node next = path.get(pursuitSegment + 2);
        return isWaterEntryWaypoint(mc, next) ? next : movementWaypoint;
    }

    private void applyClimbMovement(Minecraft mc, Node movementWaypoint, boolean sneakLatched) {
        if (isOnClimbable(mc)) {
            mc.options.keyUp.setDown(true);
            mc.options.keyDown.setDown(false);
            if (movementWaypoint.position.flooredY() > mc.player.getBlockY()) {
                mc.options.keyJump.setDown(true);
            } else if (movementWaypoint.position.flooredY() < mc.player.getBlockY()) {
                mc.options.keyJump.setDown(false);
                mc.options.keyShift.setDown(true);
            }
        } else if (!sneakLatched) {
            mc.options.keyShift.setDown(false);
        }
    }

    private boolean isInStairSequence(Minecraft mc, int pursuitSegment) {
        if (mc.level == null) {
            return false;
        }
        int stairCount = 0;
        int checkEnd = Math.min(path.size(), pursuitSegment + 3);
        for (int i = pursuitSegment; i < checkEnd; i++) {
            Node wp = path.get(i);
            if (MovementEvaluatorRegistry.get(wp.moveType).countsAsStairSequence() && isPartialSupportWaypoint(mc, wp)) {
                stairCount++;
            }
            if (i > 0 && wp.position.flooredY() < path.get(i - 1).position.flooredY()) {
                BlockPos belowWp = new BlockPos(
                    wp.position.flooredX(),
                    wp.position.flooredY() - 1,
                    wp.position.flooredZ());
                var belowState = mc.level.getBlockState(belowWp);
                if (!belowState.isAir() && !belowState.isCollisionShapeFullBlock(mc.level, belowWp)) {
                    stairCount++;
                }
            }
        }
        return stairCount >= 2;
    }

    private int parkourDistanceBlocks(Node waypoint, int pursuitSegment) {
        if (waypoint.moveType != Node.MoveType.PARKOUR || path.isEmpty()) {
            return 0;
        }
        Node takeoff = path.get(Math.max(0, Math.min(pursuitSegment, path.size() - 1)));
        int dx = Math.abs(waypoint.position.flooredX() - takeoff.position.flooredX());
        int dz = Math.abs(waypoint.position.flooredZ() - takeoff.position.flooredZ());
        return Math.max(dx, dz);
    }

    private boolean isPartialSupportWaypoint(Minecraft mc, Node wp) {
        if (mc.level == null) {
            return false;
        }
        if (checker(mc).isLowPartialSupport(
            wp.position.flooredX(),
            wp.position.flooredY(),
            wp.position.flooredZ())) {
            return true;
        }
        BlockPos support = new BlockPos(
            wp.position.flooredX(),
            wp.position.flooredY() - 1,
            wp.position.flooredZ());
        var supportState = mc.level.getBlockState(support);
        var shape = supportState.getCollisionShape(mc.level, support);
        if (shape.isEmpty()) {
            return false;
        }
        return !supportState.isCollisionShapeFullBlock(mc.level, support);
    }

    private boolean shouldAssistSlimeAscent(Minecraft mc, Node waypoint, double horizontalDistance, int jumpCooldown) {
        return mc.player != null
            && mc.player.onGround()
            && jumpCooldown == 0
            && isOnSlimeSupport(mc)
            && waypoint.position.flooredY() > mc.player.getBlockY()
            && horizontalDistance < SLIME_ASCENT_JUMP_TRIGGER_DIST;
    }

    private static boolean isOnSlimeSupport(Minecraft mc) {
        if (mc.player == null || mc.level == null) {
            return false;
        }
        BlockPos feet = mc.player.blockPosition();
        return mc.level.getBlockState(feet).is(Blocks.SLIME_BLOCK)
            || mc.level.getBlockState(feet.below()).is(Blocks.SLIME_BLOCK);
    }

    private boolean isWaterEntryWaypoint(Minecraft mc, Node waypoint) {
        if (mc.player == null || mc.level == null) {
            return false;
        }
        int x = waypoint.position.flooredX();
        int y = waypoint.position.flooredY();
        int z = waypoint.position.flooredZ();
        if (checker(mc).isWater(x, y, z)) {
            return true;
        }
        return waypoint.moveType == Node.MoveType.SWIM
            && checker(mc).isWater(x, y - 1, z)
            && mc.player.getY() - y <= WATER_ENTRY_SURFACE_DROP;
    }

    private static boolean isOnClimbable(Minecraft mc) {
        if (mc.player == null || mc.level == null) {
            return false;
        }
        BlockPos pos = mc.player.blockPosition();
        return mc.level.getBlockState(pos).is(BlockTags.CLIMBABLE);
    }

    private WalkabilityChecker checker(Minecraft mc) {
        if (mc.level == null) {
            return checker;
        }
        if (checker == null || checkerLevel != mc.level) {
            checkerLevel = mc.level;
            checker = new WalkabilityChecker(mc.level);
        }
        return checker;
    }
}
