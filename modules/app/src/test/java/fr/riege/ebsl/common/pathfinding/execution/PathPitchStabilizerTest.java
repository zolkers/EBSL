/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 *
 * Copyright (C) 2026 Victor Riegert and EBSL contributors
 *
 * This file is part of EBSL.
 *
 * EBSL is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * EBSL is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public
 * License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with EBSL. If not, see <https://www.gnu.org/licenses/>.
 */

package fr.riege.ebsl.common.pathfinding.execution;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PathPitchStabilizerTest {

    @Test
    void convergesFromZeroTowardPositiveCandidate() {
        var s = new PathPitchStabilizer();
        s.reset(0f);

        for (int i = 0; i < 80; i++) s.tick(20f, false);
        float settled = s.getStablePitch();
        assertTrue(settled > 15f, "Should settle near 20° after 80 ticks, got " + settled);
        assertTrue(settled <= 22f, "Should not exceed land max of 22°, got " + settled);
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

        assertTrue(Math.abs(result) < 5f, "Should decay significantly toward 0, got " + result);
    }

    @Test
    void clampsToLandMaxAbsPitch() {
        var s = new PathPitchStabilizer();
        s.reset(0f);
        float result = 0f;
        for (int i = 0; i < 200; i++) result = s.tick(90f, false);

        assertTrue(result <= 22.0f, "Should not exceed pitchLandMaxAbsDeg=22, got " + result);
    }

    @Test
    void resetClearsVelocity() {
        var s = new PathPitchStabilizer();
        s.reset(0f);
        for (int i = 0; i < 20; i++) s.tick(30f, false);

        s.reset(5f);
        assertEquals(5f, s.getStablePitch(), 0.001f, "reset should set stablePitch to initialPitch");


        float after = s.tick(15f, false);

        assertTrue(after < 7f, "Should move slowly right after reset (velocity reset to 0), got " + after);
    }

    @Test
    void waterUsesWaterMaxAbsPitch() {
        var s = new PathPitchStabilizer();
        s.reset(0f);
        float result = 0f;
        for (int i = 0; i < 200; i++) result = s.tick(90f, true);

        assertTrue(result <= 8.0f, "Should not exceed pitchWaterMaxAbsDeg=8, got " + result);
    }

    @Test
    void enteringWaterDoesNotSnapPitchToWaterLimit() {
        var s = new PathPitchStabilizer();
        s.reset(20f);

        float firstWaterTick = s.tick(0f, true);

        assertTrue(firstWaterTick > 8.0f, "Should ease toward water pitch limit instead of snapping, got " + firstWaterTick);
        assertTrue(firstWaterTick < 20.0f, "Should still move toward the water-safe pitch range, got " + firstWaterTick);
    }
}
