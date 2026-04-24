package fr.riege.ebsl.pathfinding.execution;

import fr.riege.ebsl.pathfinding.Node;
import fr.riege.ebsl.pathfinding.movement.WalkabilityChecker;
import fr.riege.ebsl.pathfinding.movement.types.MovementExecutionContext;
import fr.riege.ebsl.pathfinding.movement.types.MovementRegistry;
import fr.riege.ebsl.pathfinding.movement.types.MovementValidationContext;
import fr.riege.ebsl.pathfinding.movement.types.MovementValidationResult;
import fr.riege.ebsl.pathfinding.movement.types.WaterMovementContext;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.List;

final class WalkMovementController {
    private final List<Node> path;
    private Level checkerLevel;
    private WalkabilityChecker checker;

    WalkMovementController(List<Node> path) {
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
        return MovementRegistry.get(target.moveType).validate(context);
    }

    void apply(PathExecutor executor, Minecraft mc, Vec3 playerPos, Node movementWaypoint,
               double distToFinal, boolean allowJumps, boolean sneakLatched,
               int pursuitSegment, int jumpCooldown, long lastProgressTime) {
        double dxWp = movementWaypoint.position.centeredX() - playerPos.x;
        double dzWp = movementWaypoint.position.centeredZ() - playerPos.z;
        boolean nearStepUp = MovementRegistry.get(movementWaypoint.moveType).reducesSprintNearWaypoint()
            && Math.sqrt(dxWp * dxWp + dzWp * dzWp) < 2.0;

        applyPathMovement(mc, playerPos, movementWaypoint, distToFinal, nearStepUp, pursuitSegment);

        if (!allowJumps) {
            mc.options.keyJump.setDown(mc.player != null && mc.player.isInWater());
            return;
        }

        handleJump(executor, mc, movementWaypoint, playerPos, jumpCooldown, lastProgressTime, pursuitSegment);
        applyWaterMovement(mc, movementWaypoint, playerPos, distToFinal, pursuitSegment, sneakLatched);
        applyClimbMovement(mc, movementWaypoint, sneakLatched);
    }

    private void applyPathMovement(Minecraft mc, Vec3 playerPos, Node targetWp,
                                   double distToFinal, boolean nearStepUp, int pursuitSegment) {
        if (mc.player == null) {
            return;
        }

        double dx = targetWp.position.centeredX() - playerPos.x;
        double dz = targetWp.position.centeredZ() - playerPos.z;
        double hDist = Math.sqrt(dx * dx + dz * dz);

        if (hDist < PathExecutor.WALK_TARGET_DEADZONE && pursuitSegment + 2 < path.size()) {
            targetWp = path.get(pursuitSegment + 2);
            dx = targetWp.position.centeredX() - playerPos.x;
            dz = targetWp.position.centeredZ() - playerPos.z;
            hDist = Math.sqrt(dx * dx + dz * dz);
        }

        if (hDist < PathExecutor.WALK_TARGET_DEADZONE) {
            mc.options.keyUp.setDown(false);
            mc.options.keyDown.setDown(false);
            mc.options.keyLeft.setDown(false);
            mc.options.keyRight.setDown(false);
            mc.options.keySprint.setDown(false);
            return;
        }

        double desiredX = dx / hDist;
        double desiredZ = dz / hDist;
        MovementInputController.applyRelativeMovement(
            mc, desiredX, desiredZ, PathExecutor.WALK_FORWARD_DOT,
            PathExecutor.WALK_BACKWARD_DOT, PathExecutor.WALK_STRAFE_DOT);
        mc.options.keySprint.setDown(mc.options.keyUp.isDown()
            && !mc.options.keyDown.isDown()
            && distToFinal > 2.0
            && !nearStepUp);
    }

    private void handleJump(PathExecutor executor, Minecraft mc, Node waypoint, Vec3 playerPos,
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
            PathExecutor.STEP_UP_TRIGGER_DIST,
            PathExecutor.JUMP_TRIGGER_DIST,
            1.2,
            PathExecutor.JUMP_COOLDOWN_TICKS,
            PathExecutor.STALL_JUMP_PROGRESS_MS
        );
        MovementRegistry.get(waypoint.moveType).handleJump(context);
        mc.options.keyJump.setDown(context.jumpPressed());
        if (context.jumpCooldownConsumed()) {
            executor.setJumpCooldown(PathExecutor.JUMP_COOLDOWN_TICKS);
        }
    }

    private void applyWaterMovement(Minecraft mc, Node movementWaypoint, Vec3 playerPos,
                                    double distToFinal, int pursuitSegment, boolean sneakLatched) {
        if (!mc.player.isInWater()) {
            return;
        }

        Node nextWaypoint = path.get(Math.min(path.size() - 1, pursuitSegment + 2));
        WaterMovementContext waterContext = new WaterMovementContext(
            mc, movementWaypoint, nextWaypoint, playerPos, distToFinal);
        MovementRegistry.get(movementWaypoint.moveType).handleWaterMovement(waterContext);
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
            if (MovementRegistry.get(wp.moveType).countsAsStairSequence() && isPartialSupportWaypoint(mc, wp)) {
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

    private boolean isPartialSupportWaypoint(Minecraft mc, Node wp) {
        if (mc.level == null) {
            return false;
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
