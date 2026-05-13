package fr.riege.ebsl.common.pathfinding.movement.types.evaluation;

import fr.riege.ebsl.common.domain.world.BlockId;
import fr.riege.ebsl.common.math.Vec3d;
import fr.riege.ebsl.common.pathfinding.Node;
import fr.riege.ebsl.common.pathfinding.movement.WalkabilityChecker;
import fr.riege.ebsl.common.pathfinding.provider.LayerNavigationPointProvider;
import fr.riege.ebsl.common.pathfinding.wrapper.PathPosition;
import fr.riege.ebsl.common.world.layer.IWorldLayer;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StepDownMovementEvaluatorTest {
    @Test
    void rejectsBlockedHeadroomAtTarget() {
        FakeWorld world = new FakeWorld();
        world.solid(0, 63, 0);
        world.solid(1, 62, 0);
        world.solid(1, 64, 0);
        WalkabilityChecker checker = new WalkabilityChecker(world);
        StepDownMovementEvaluator evaluator = new StepDownMovementEvaluator();

        MovementValidationResult result = evaluator.validate(context(checker, new PathPosition(0, 64, 0), new PathPosition(1, 63, 0)));

        assertFalse(result.valid());
        assertTrue(result.reason().contains("headroom blocked"));
    }

    @Test
    void acceptsOpenStepDownLanding() {
        FakeWorld world = new FakeWorld();
        world.solid(0, 63, 0);
        world.solid(1, 62, 0);
        WalkabilityChecker checker = new WalkabilityChecker(world);
        StepDownMovementEvaluator evaluator = new StepDownMovementEvaluator();

        MovementValidationResult result = evaluator.validate(context(checker, new PathPosition(0, 64, 0), new PathPosition(1, 63, 0)));

        assertTrue(result.valid(), result.reason());
    }

    private static MovementValidationContext context(WalkabilityChecker checker, PathPosition from, PathPosition target) {
        Node fromNode = new Node(from);
        Node targetNode = new Node(target);
        targetNode.setMoveType(Node.MoveType.STEP_DOWN);
        return new MovementValidationContext(
            checker,
            new LayerNavigationPointProvider(checker),
            fromNode,
            targetNode,
            targetNode,
            new Vec3d(from.centeredX(), from.flooredY(), from.centeredZ()),
            0);
    }

    private static final class FakeWorld implements IWorldLayer {
        private final Set<String> solids = new HashSet<>();

        void solid(int x, int y, int z) {
            solids.add(key(x, y, z));
        }

        @Override public BlockId getBlock(int x, int y, int z) {
            return isSolid(x, y, z) ? new BlockId("test", "solid") : BlockId.AIR;
        }

        @Override public boolean isAir(int x, int y, int z) {
            return !isSolid(x, y, z);
        }

        @Override public boolean isSolid(int x, int y, int z) {
            return solids.contains(key(x, y, z));
        }

        @Override public boolean isWater(int x, int y, int z) {
            return false;
        }

        @Override public boolean isLava(int x, int y, int z) {
            return false;
        }

        @Override public boolean isLoaded(int x, int y, int z) {
            return true;
        }

        @Override public int getTopSolidY(int x, int z) {
            return 64;
        }

        @Override public double getBlockHeight(int x, int y, int z) {
            return isSolid(x, y, z) ? 1.0 : 0.0;
        }

        private static String key(int x, int y, int z) {
            return x + "," + y + "," + z;
        }
    }
}
