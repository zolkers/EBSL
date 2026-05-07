package fr.riege.ebsl.common.pathfinding.execution;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PathPitchStabilizerTest {

    @Test
    void convergesFromZeroTowardPositiveCandidate() {
        var s = new PathPitchStabilizer();
        s.reset(0f);
        float result = 0f;
        for (int i = 0; i < 40; i++) {
            result = s.tick(20f, false);
        }
        // Underdamped spring may micro-overshoot the clamp boundary; overall it must converge well above 10°
        assertTrue(result > 10f, "Should reach well above 10° after 40 ticks, got " + result);
    }

    @Test
    void decaysBackToZeroWhenCandidateIsZero() {
        var s = new PathPitchStabilizer();
        s.reset(0f);
        for (int i = 0; i < 30; i++) s.tick(20f, false);
        float peak = s.getStablePitch();
        assertTrue(peak > 5f, "Should have built up some pitch first, got " + peak);

        float result = peak;
        for (int i = 0; i < 60; i++) {
            result = s.tick(0f, false);
        }
        // Underdamped spring may oscillate through zero; overall it must decay significantly toward 0
        assertTrue(Math.abs(result) < 5f, "Should decay significantly toward 0, got " + result);
    }

    @Test
    void clampsToLandMaxAbsPitch() {
        var s = new PathPitchStabilizer();
        s.reset(0f);
        float result = 0f;
        for (int i = 0; i < 200; i++) result = s.tick(90f, false);
        // default pitchLandMaxAbsDeg = 22.0
        assertTrue(result <= 22.0f, "Should not exceed pitchLandMaxAbsDeg=22, got " + result);
    }

    @Test
    void resetClearsVelocity() {
        var s = new PathPitchStabilizer();
        s.reset(0f);
        for (int i = 0; i < 20; i++) s.tick(30f, false);

        s.reset(5f);
        assertEquals(5f, s.getStablePitch(), 0.001f, "reset should set stablePitch to initialPitch");

        // After reset velocity=0, first tick moves only stiffness*error (default stiffness=0.10)
        float after = s.tick(15f, false);
        // error = 10, stiffness = 0.10 → velocity = 1.0, stablePitch = 6.0 max
        assertTrue(after < 7f, "Should move slowly right after reset (velocity reset to 0), got " + after);
    }

    @Test
    void waterUsesWaterMaxAbsPitch() {
        var s = new PathPitchStabilizer();
        s.reset(0f);
        float result = 0f;
        for (int i = 0; i < 200; i++) result = s.tick(90f, true);
        // default pitchWaterMaxAbsDeg = 8.0
        assertTrue(result <= 8.0f, "Should not exceed pitchWaterMaxAbsDeg=8, got " + result);
    }
}
