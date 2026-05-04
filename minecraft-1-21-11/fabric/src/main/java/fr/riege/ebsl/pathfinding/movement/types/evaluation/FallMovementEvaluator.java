package fr.riege.ebsl.pathfinding.movement.types.evaluation;

import fr.riege.ebsl.pathfinding.Node;
import fr.riege.ebsl.pathfinding.movement.types.annotation.MovementHandler;
import fr.riege.ebsl.pathfinding.provider.NavigationPoint;
import fr.riege.ebsl.pathfinding.provider.impl.MinecraftNavigationProvider;

@MovementHandler(Node.MoveType.FALL)
final class FallMovementEvaluator extends WalkMovementEvaluator {
    @Override
    public MovementValidationResult validate(MovementValidationContext context) {
        int fromY = context.from().position.flooredY();
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
