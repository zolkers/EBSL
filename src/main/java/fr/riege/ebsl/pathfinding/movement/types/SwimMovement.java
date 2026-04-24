package fr.riege.ebsl.pathfinding.movement.types;

import fr.riege.ebsl.pathfinding.Node;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public final class SwimMovement extends WalkMovement {

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
        if (mc.player == null || !mc.player.isInWater()) return;

        int waypointY = context.waypoint().position.flooredY();
        int playerY   = Mth.floor(context.playerPos().y);

        boolean needUp   = waypointY > playerY || isHeadUnderWater(mc, context.playerPos());
        boolean needDown = waypointY < playerY - 1;

        context.setVerticalControls(needUp && !needDown, needDown && !needUp);
        context.setSprintPressed(!needDown);
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

    private static boolean isHeadUnderWater(Minecraft mc, Vec3 playerPos) {
        if (mc.player == null || mc.level == null) {
            return false;
        }
        BlockPos eyePos = BlockPos.containing(playerPos.x, mc.player.getEyeY() - 0.1, playerPos.z);
        return mc.level.getFluidState(eyePos).is(FluidTags.WATER);
    }
}
