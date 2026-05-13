package fr.riege.ebsl.common.pathfinding;

import fr.riege.ebsl.common.pathfinding.pathing.configuration.PathfinderConfiguration;
import fr.riege.ebsl.common.pathfinding.wrapper.PathPosition;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class WalkPathProcessorTest {
    @Test
    void processUsesConfiguredMovementClassifierForRawNodes() {
        PathfinderConfiguration configuration = PathfinderConfiguration.builder()
            .movementClassifier(context -> Node.MoveType.JUMP)
            .build();

        ProcessedPath path = WalkPathProcessor.process(List.of(
            new PathPosition(0, 64, 0),
            new PathPosition(1, 64, 0)
        ), configuration, null);

        assertEquals(Node.MoveType.JUMP, path.rawNodes().get(1).moveType());
    }
}
