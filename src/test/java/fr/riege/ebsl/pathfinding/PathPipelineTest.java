package fr.riege.ebsl.pathfinding;

import fr.riege.ebsl.pathfinding.wrapper.PathPosition;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PathPipelineTest {
    @Test
    void mergePathPrefixWithTailKeepsSingleJoinNode() {
        List<Node> prefix = List.of(node(0, 64, 0), node(1, 64, 0), node(2, 64, 0));
        List<Node> tail = List.of(node(2, 64, 0), node(3, 64, 0), node(4, 64, 0));

        List<Node> merged = PathPipeline.mergePathPrefixWithTail(prefix, tail);

        assertEquals(5, merged.size());
        assertEquals(new PathPosition(0, 64, 0), merged.get(0).position);
        assertEquals(new PathPosition(2, 64, 0), merged.get(2).position);
        assertEquals(new PathPosition(4, 64, 0), merged.get(4).position);
    }

    @Test
    void mergePathPrefixWithTailIgnoresNullCollectionsAndNodes() {
        List<Node> merged = PathPipeline.mergePathPrefixWithTail(
            null,
            java.util.Arrays.asList(null, node(7, 64, 7), null));

        assertEquals(1, merged.size());
        assertEquals(new PathPosition(7, 64, 7), merged.getFirst().position);
    }

    private static Node node(int x, int y, int z) {
        return new Node(new PathPosition(x, y, z));
    }
}
