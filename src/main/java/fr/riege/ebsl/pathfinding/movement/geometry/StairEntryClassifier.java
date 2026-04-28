package fr.riege.ebsl.pathfinding.movement.geometry;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Half;

public final class StairEntryClassifier {
    private StairEntryClassifier() {
    }

    public static boolean requiresJump(BlockState supportState, int moveDx, int moveDz) {
        if (!(supportState.getBlock() instanceof StairBlock)) {
            return false;
        }
        Direction facing = supportState.getValue(StairBlock.FACING);
        boolean bottomHalf = supportState.getValue(StairBlock.HALF) == Half.BOTTOM;
        int dot = Integer.signum(moveDx) * facing.getStepX()
            + Integer.signum(moveDz) * facing.getStepZ();
        return bottomHalf ? dot <= 0 : dot >= 0;
    }
}
