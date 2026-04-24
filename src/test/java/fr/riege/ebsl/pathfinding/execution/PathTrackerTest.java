package fr.riege.ebsl.pathfinding.execution;

import fr.riege.ebsl.pathfinding.Node;
import fr.riege.ebsl.pathfinding.wrapper.PathPosition;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PathTrackerTest {
    @Test
    void repairRequestUsesExistingJoinNodeAndOriginalTail() {
        PathTracker tracker = new PathTracker();
        tracker.start(path(0, 1, 2, 3, 4));

        PathRepairRequest request = tracker.createRepairRequest(2, "blocked").orElseThrow();

        assertEquals(new PathPosition(2, 64, 0), request.joinNode().position);
        assertEquals(3, request.remainingPath().size());
        assertEquals(new PathPosition(2, 64, 0), request.remainingPath().getFirst().position);
        assertEquals(new PathPosition(4, 64, 0), request.remainingPath().getLast().position);
        assertEquals("blocked", request.reason());
    }

    @Test
    void repairRequestClampsToLastValidSegment() {
        PathTracker tracker = new PathTracker();
        tracker.start(path(0, 1, 2));

        PathRepairRequest request = tracker.createRepairRequest(99, "late").orElseThrow();

        assertEquals(new PathPosition(1, 64, 0), request.joinNode().position);
        assertEquals(2, request.remainingPath().size());
        assertEquals(new PathPosition(2, 64, 0), request.remainingPath().getLast().position);
    }

    @Test
    void smartCutoffKeepsContinuableTailAndResetsPursuit() {
        PathTracker tracker = new PathTracker();
        tracker.start(path(0, 1, 2, 3, 4));

        assertTrue(tracker.applySmartCutoff(2));

        assertEquals(0, tracker.getPursuitSegment());
        assertEquals(3, tracker.getPath().size());
        assertEquals(new PathPosition(2, 64, 0), tracker.getPath().getFirst().position);
        assertEquals(new PathPosition(4, 64, 0), tracker.getPath().getLast().position);
    }

    @Test
    void remainingDistanceUsesCurrentPursuitSegmentTail() {
        PathTracker tracker = new PathTracker();
        tracker.start(path(0, 1, 2));

        double remaining = tracker.getRemainingDistance(new Vec3(0.5, 64.0, 0.5));

        assertEquals(2.0, remaining, 1.0e-6);
    }

    private static List<Node> path(int... xs) {
        return java.util.Arrays.stream(xs)
            .mapToObj(x -> new Node(new PathPosition(x, 64, 0)))
            .toList();
    }
}
