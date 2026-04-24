package fr.riege.ebsl.pathfinding.movement.types;

import fr.riege.ebsl.pathfinding.Node;
import net.minecraft.client.Minecraft;

public final class SwimMovement extends WalkMovement {

    @Override
    public Node.MoveType type() {
        return Node.MoveType.SWIM;
    }

    @Override
    public void handleJump(MovementExecutionContext context) {
        if (context.minecraft().player != null && context.minecraft().player.isInWater()) {
            context.pressJump();
            return;
        }
        super.handleJump(context);
    }

    @Override
    public void handleWaterMovement(WaterMovementContext context) {
        Minecraft mc = context.minecraft();
        if (mc.player == null || !mc.player.isInWater()) return;

        context.setVerticalControls(true, false);
        context.setSprintPressed(true);
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
}
