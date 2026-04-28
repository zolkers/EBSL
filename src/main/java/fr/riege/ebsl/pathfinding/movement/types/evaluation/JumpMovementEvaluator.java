package fr.riege.ebsl.pathfinding.movement.types.evaluation;

import fr.riege.ebsl.pathfinding.Node;
import fr.riege.ebsl.pathfinding.movement.types.annotation.MovementHandler;

@MovementHandler(Node.MoveType.JUMP)
final class JumpMovementEvaluator implements MovementEvaluator {
    @Override
    public boolean countsAsAscendingDifficulty() {
        return true;
    }
}
