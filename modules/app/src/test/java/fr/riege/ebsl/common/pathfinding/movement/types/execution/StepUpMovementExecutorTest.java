package fr.riege.ebsl.common.pathfinding.movement.types.execution;

import fr.riege.ebsl.common.math.Vec3d;
import fr.riege.ebsl.common.pathfinding.Node;
import fr.riege.ebsl.common.pathfinding.wrapper.PathPosition;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class StepUpMovementExecutorTest {
    @Test
    void fullStepTriggersBeforeCenterDistanceFallsBelowOneBlock() {
        MovementExecutionContext context = context(false, 1.20, 1.0);

        new StepUpMovementExecutor().handleJump(context);

        assertTrue(context.jumpPressed());
        assertTrue(context.jumpCooldownConsumed());
    }

    @Test
    void partialSupportAscentDoesNotForceJump() {
        MovementExecutionContext context = context(true, 0.40, 0.5);

        new StepUpMovementExecutor().handleJump(context);

        assertFalse(context.jumpPressed());
        assertFalse(context.jumpCooldownConsumed());
    }

    private static MovementExecutionContext context(boolean partialSupportAscent,
                                                    double horizontalDistance,
                                                    double verticalDelta) {
        return new MovementExecutionContext(
            new Node(new PathPosition(1, 65, 0)),
            new Vec3d(0.5, 64.0, 0.5),
            partialSupportAscent,
            false,
            true,
            0,
            0L,
            horizontalDistance,
            verticalDelta,
            1.0,
            0.6,
            1.2,
            0,
            8,
            450L);
    }
}
