package fr.riege.ebsl.pathfinding.goal;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GoalContractsTest {
    @Test
    void radiusGoalsRejectInvalidRadius() {
        assertThrows(IllegalArgumentException.class, () -> new GoalNear(0, 64, 0, -0.1));
        assertThrows(IllegalArgumentException.class, () -> new GoalNear(0, 64, 0, Double.NaN));
        assertThrows(IllegalArgumentException.class, () -> new GoalColumn(0, 0, Double.POSITIVE_INFINITY));
    }

    @Test
    void compositeGoalRequiresNonEmptyNonNullGoalsAndDefensivelyCopies() {
        assertThrows(IllegalArgumentException.class, () -> new GoalCompositeAny(List.of()));
        assertThrows(IllegalArgumentException.class, () -> new GoalCompositeAny(null));
        assertThrows(IllegalArgumentException.class, () -> new GoalCompositeAny(java.util.Arrays.asList(new GoalBlock(0, 64, 0), null)));

        ArrayList<Goal> source = new ArrayList<>();
        source.add(new GoalBlock(1, 64, 1));
        GoalCompositeAny composite = new GoalCompositeAny(source);
        source.clear();

        assertEquals(1, composite.goals().size());
        assertTrue(composite.isInGoal(1, 64, 1));
    }

    @Test
    void navigationRequestRejectsInvalidCoreFields() {
        assertThrows(NullPointerException.class, () -> NavigationRequest.builder(null));
        assertThrows(NullPointerException.class, () -> NavigationRequest.builder(new GoalBlock(0, 64, 0)).mode(null));
        assertThrows(IllegalArgumentException.class, () -> NavigationRequest.builder(new GoalBlock(0, 64, 0))
            .preciseGoalTolerance(Double.NaN)
            .build());
        assertThrows(IllegalArgumentException.class, () -> NavigationRequest.builder(new GoalBlock(0, 64, 0))
            .preciseGoalTolerance(-1.0)
            .build());
    }
}
