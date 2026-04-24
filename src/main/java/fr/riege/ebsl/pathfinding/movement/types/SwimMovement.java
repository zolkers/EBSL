package fr.riege.ebsl.pathfinding.movement.types;

import fr.riege.ebsl.pathfinding.Node;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public final class SwimMovement extends WalkMovement {
    private static final double SURFACE_FEET_OFFSET = 0.35;
    private static final double SUBMERGED_FEET_OFFSET = 0.15;
    private static final double SURFACE_LOCK_RANGE = 2.5;
    private static final double VERTICAL_UP_THRESHOLD = 0.18;
    private static final double VERTICAL_DOWN_THRESHOLD = 0.28;
    private static final double LOOK_RAY_DIST = 1.8;

    @Override
    public Node.MoveType type() {
        return Node.MoveType.SWIM;
    }

    @Override
    public void handleJump(MovementExecutionContext context) {
        if (context.minecraft().player != null && context.minecraft().player.isInWater()) {
            context.releaseJump();
            return;
        }
        super.handleJump(context);
    }

    @Override
    public void handleWaterMovement(WaterMovementContext context) {
        Minecraft mc = context.minecraft();
        if (mc.player == null || mc.level == null || !mc.player.isInWater()) {
            return;
        }

        Node waypoint = context.waypoint();
        Node nextWaypoint = context.nextWaypoint();
        Vec3 playerPos = context.playerPos();

        boolean waypointInWater = isWaterColumn(mc.level, waypoint.position.flooredX(), waypoint.position.flooredY(), waypoint.position.flooredZ());
        boolean nextInWater = nextWaypoint != null
            && isWaterColumn(mc.level, nextWaypoint.position.flooredX(), nextWaypoint.position.flooredY(), nextWaypoint.position.flooredZ());
        boolean exitWaterSoon = !waypointInWater || !nextInWater;
        boolean keepSurface = exitWaterSoon
            || isSurfaceWaypoint(mc.level, waypoint)
            || context.distToFinal() <= SURFACE_LOCK_RANGE;

        double targetFeetY = computeTargetFeetY(mc.level, playerPos, waypoint, keepSurface);
        double dy = targetFeetY - playerPos.y;

        boolean jump = false;
        boolean shift = false;
        if (dy > VERTICAL_UP_THRESHOLD) {
            jump = true;
        } else if (dy < -VERTICAL_DOWN_THRESHOLD && !keepSurface) {
            shift = true;
        }

        Obstruction obstruction = traceWaterObstruction(mc, playerPos, waypoint);
        if (keepSurface) {
            if (obstruction.feetBlocked && !obstruction.headBlocked) {
                jump = true;
                shift = false;
            } else if (obstruction.headBlocked && obstruction.feetBlocked) {
                jump = false;
                shift = true;
            }
        } else {
            if (obstruction.headBlocked && !obstruction.feetBlocked) {
                shift = true;
                jump = false;
            } else if (obstruction.feetBlocked && !obstruction.headBlocked) {
                jump = true;
                shift = false;
            }
        }

        // When we just need to skim the surface, keep the head from diving back under.
        if (keepSurface && isHeadUnderWater(mc, playerPos)) {
            jump = true;
            shift = false;
        }

        context.setVerticalControls(jump, shift);
        context.setSprintPressed(false);
    }

    @Override
    public MovementValidationResult validate(MovementValidationContext context) {
        int x = context.targetX();
        int y = context.targetY();
        int z = context.targetZ();
        if (context.checker().isWater(x, y, z)
            || context.checker().isWater(x, y - 1, z)
            || context.checker().isWalkable(x, y, z)) {
            return MovementValidationResult.ok();
        }
        return MovementValidationResult.invalid("swim segment lost water/exit at " + x + ", " + y + ", " + z);
    }

    private static double computeTargetFeetY(Level level, Vec3 playerPos, Node waypoint, boolean keepSurface) {
        if (!keepSurface) {
            return waypoint.position.flooredY() + SUBMERGED_FEET_OFFSET;
        }

        double playerSurface = resolveSurfaceY(level, playerPos.x, playerPos.y, playerPos.z);
        double waypointSurface = resolveSurfaceY(level,
            waypoint.position.centeredX(),
            waypoint.position.flooredY() + 0.5,
            waypoint.position.centeredZ());
        double surface = Double.isNaN(waypointSurface) ? playerSurface : waypointSurface;
        if (Double.isNaN(surface)) {
            surface = playerPos.y + 0.8;
        }
        return surface - SURFACE_FEET_OFFSET;
    }

    private static boolean isSurfaceWaypoint(Level level, Node waypoint) {
        int x = waypoint.position.flooredX();
        int y = waypoint.position.flooredY();
        int z = waypoint.position.flooredZ();
        return isWaterColumn(level, x, y, z) && !isWaterColumn(level, x, y + 1, z);
    }

    private static boolean isWaterColumn(Level level, int x, int y, int z) {
        return level.getFluidState(new BlockPos(x, y, z)).is(FluidTags.WATER);
    }

    private static double resolveSurfaceY(Level level, double x, double y, double z) {
        int blockX = BlockPos.containing(x, y, z).getX();
        int blockZ = BlockPos.containing(x, y, z).getZ();
        int startY = BlockPos.containing(x, y, z).getY();

        int waterY = Integer.MIN_VALUE;
        for (int scanY = startY + 2; scanY >= startY - 4; scanY--) {
            if (isWaterColumn(level, blockX, scanY, blockZ)) {
                waterY = scanY;
                break;
            }
        }
        if (waterY == Integer.MIN_VALUE) {
            return Double.NaN;
        }

        while (isWaterColumn(level, blockX, waterY + 1, blockZ)) {
            waterY++;
        }

        BlockPos surfacePos = new BlockPos(blockX, waterY, blockZ);
        FluidState fluidState = level.getFluidState(surfacePos);
        if (fluidState.isEmpty()) {
            return Double.NaN;
        }
        return waterY + fluidState.getHeight(level, surfacePos);
    }

    private static boolean isHeadUnderWater(Minecraft mc, Vec3 playerPos) {
        if (mc.player == null || mc.level == null) {
            return false;
        }
        BlockPos eyePos = BlockPos.containing(playerPos.x, mc.player.getEyeY() - 0.1, playerPos.z);
        return mc.level.getFluidState(eyePos).is(FluidTags.WATER);
    }

    private static Obstruction traceWaterObstruction(Minecraft mc, Vec3 playerPos, Node waypoint) {
        if (mc.player == null || mc.level == null) {
            return Obstruction.NONE;
        }

        double dx = waypoint.position.centeredX() - playerPos.x;
        double dz = waypoint.position.centeredZ() - playerPos.z;
        double horiz = Math.sqrt(dx * dx + dz * dz);
        if (horiz < 1.0e-4) {
            return Obstruction.NONE;
        }

        double dirX = dx / horiz;
        double dirZ = dz / horiz;
        Vec3 chestStart = playerPos.add(0.0, 0.9, 0.0);
        Vec3 headStart = playerPos.add(0.0, 1.45, 0.0);
        Vec3 chestEnd = chestStart.add(dirX * LOOK_RAY_DIST, 0.0, dirZ * LOOK_RAY_DIST);
        Vec3 headEnd = headStart.add(dirX * LOOK_RAY_DIST, 0.0, dirZ * LOOK_RAY_DIST);

        HitResult chestTrace = mc.level.clip(new ClipContext(
            chestStart, chestEnd,
            ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, mc.player));
        HitResult headTrace = mc.level.clip(new ClipContext(
            headStart, headEnd,
            ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, mc.player));

        return new Obstruction(
            chestTrace.getType() == HitResult.Type.BLOCK,
            headTrace.getType() == HitResult.Type.BLOCK
        );
    }

    private record Obstruction(boolean feetBlocked, boolean headBlocked) {
        private static final Obstruction NONE = new Obstruction(false, false);
    }
}
