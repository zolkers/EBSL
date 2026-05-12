package fr.riege.ebsl.common.pathfinding.movement.types.evaluation;

import fr.riege.ebsl.common.pathfinding.Node;
import fr.riege.ebsl.common.pathfinding.movement.types.annotation.MovementHandler;

@MovementHandler(Node.MoveType.JUMP)
final class JumpMovementEvaluator implements MovementEvaluator {
    @Override
    public boolean countsAsAscendingDifficulty() {
        return true;
    }
}
