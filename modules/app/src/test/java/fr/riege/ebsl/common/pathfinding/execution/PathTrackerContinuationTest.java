package fr.riege.ebsl.common.pathfinding.execution;

import fr.riege.ebsl.common.math.Vec3d;
import fr.riege.ebsl.common.pathfinding.Node;
import fr.riege.ebsl.common.pathfinding.wrapper.PathPosition;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PathTrackerContinuationTest {
    @Test
    void rejectsContinuationThatCannotAttachToCurrentPath() {
        PathTracker tracker = new PathTracker();
        tracker.start(List.of(
            node(0, 64, 0),
            node(1, 64, 0),
            node(2, 64, 0)));
        tracker.advancePursuit(new Vec3d(0.5, 64.0, 0.5), System.currentTimeMillis());

        tracker.continueWith(List.of(
            node(200, 64, 0),
            node(201, 64, 0)));

        List<Node> snapshot = tracker.getPathSnapshot();
        assertEquals(3, snapshot.size(), "far continuation must not create a void bridge");
        assertEquals(new PathPosition(2, 64, 0), snapshot.getLast().position);
    }

    @Test
    void trimsContinuationToNearestAttachPoint() {
        PathTracker tracker = new PathTracker();
        tracker.start(List.of(
            node(0, 64, 0),
            node(1, 64, 0),
            node(2, 64, 0)));

        tracker.continueWith(List.of(
            node(50, 64, 0),
            node(2, 64, 0),
            node(3, 64, 0),
            node(4, 64, 0)));

        List<Node> snapshot = tracker.getPathSnapshot();
        assertEquals(new PathPosition(4, 64, 0), snapshot.getLast().position);
        assertEquals(5, snapshot.size(), "continuation should start at the attach point, not the stray prefix");
    }

    private static Node node(int x, int y, int z) {
        return new Node(new PathPosition(x, y, z));
    }
}
