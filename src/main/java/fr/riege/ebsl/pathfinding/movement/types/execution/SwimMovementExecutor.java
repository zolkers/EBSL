package fr.riege.ebsl.pathfinding.movement.types.execution;

import fr.riege.ebsl.pathfinding.Node;
import fr.riege.ebsl.pathfinding.movement.types.annotation.MovementHandler;
import net.minecraft.client.Minecraft;

@MovementHandler(Node.MoveType.SWIM)
final class SwimMovementExecutor extends WalkMovementExecutor {
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
}
