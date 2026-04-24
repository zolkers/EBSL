package fr.riege.ebsl.pathfinding;

import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LongRangePathSessionTest {
    @Test
    void plansSegmentTowardFarFinalGoalWithoutForgettingFinalGoal() {
        LongRangePathSession session = new LongRangePathSession();
        session.start(5000, 5000);

        LongRangePathSession.SegmentGoal firstSegment = session.planSegmentGoal(0.5, 0.5);

        assertTrue(firstSegment.segmented());
        assertEquals(5000, session.finalGoalX());
        assertEquals(5000, session.finalGoalZ());
        assertTrue(firstSegment.x() > 0 && firstSegment.x() < 5000);
        assertTrue(firstSegment.z() > 0 && firstSegment.z() < 5000);
    }

    @Test
    void usesFinalGoalWhenAlreadyWithinOneSegment() {
        LongRangePathSession session = new LongRangePathSession();
        session.start(90, 20);

        LongRangePathSession.SegmentGoal segment = session.planSegmentGoal(0.5, 0.5);

        assertFalse(segment.segmented());
        assertEquals(90, segment.x());
        assertEquals(20, segment.z());
    }

    @Test
    void queuesNormalContinuationBeforeEmergencyDistanceAndEmergencyOnlyWhenExecutionEnds() {
        LongRangePathSession session = new LongRangePathSession();
        session.start(5000, 5000);
        session.onSegmentStarted(120, 120, true, false);

        assertEquals(
            LongRangePathSession.SegmentQueueDecision.NONE,
            session.queueDecision(0.10, 100.0, false, 1000L));
        assertEquals(
            LongRangePathSession.SegmentQueueDecision.NORMAL,
            session.queueDecision(0.56, 100.0, false, 1000L));
        assertEquals(
            LongRangePathSession.SegmentQueueDecision.NORMAL,
            session.queueDecision(0.10, 13.0, false, 1000L));
        assertEquals(
            LongRangePathSession.SegmentQueueDecision.EMERGENCY_FROM_PLAYER,
            session.queueDecision(0.10, 13.0, true, 1000L));
    }

    @Test
    void reportsFinalGoalReachedByXZToleranceOnly() {
        LongRangePathSession session = new LongRangePathSession();
        session.start(5000, 5000);

        assertTrue(session.isFinalGoalReached(new Vec3(5000.5, 20.0, 5000.5)));
        assertFalse(session.isFinalGoalReached(new Vec3(4996.0, 20.0, 5000.5)));
    }
}
