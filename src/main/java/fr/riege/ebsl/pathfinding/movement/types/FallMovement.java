package fr.riege.ebsl.pathfinding.movement.types;

import fr.riege.ebsl.pathfinding.Node;
import fr.riege.ebsl.pathfinding.provider.NavigationPoint;
import fr.riege.ebsl.pathfinding.provider.impl.MinecraftNavigationProvider;

public final class FallMovement extends WalkMovement {
    @Override
    public Node.MoveType type() {
        return Node.MoveType.FALL;
    }

    @Override
    public MovementValidationResult validate(MovementValidationContext context) {
        int fromX = context.from().position.flooredX();
        int fromY = context.from().position.flooredY();
        int fromZ = context.from().position.flooredZ();
        int x = context.targetX();
        int y = context.targetY();
        int z = context.targetZ();
        if (!context.checker().safeToFall(fromY, x, y, z)) {
            return MovementValidationResult.invalid("fall segment became unsafe at " + x + ", " + y + ", " + z);
        }
        NavigationPoint targetPoint = new MinecraftNavigationProvider(context.checker())
            .getNavigationPoint(context.target().position, null);
        if (targetPoint.isTraversable()) {
            return MovementValidationResult.ok();
        }
        return MovementValidationResult.invalid("fall landing is no longer traversable at "
            + x + ", " + y + ", " + z);
    }
}
