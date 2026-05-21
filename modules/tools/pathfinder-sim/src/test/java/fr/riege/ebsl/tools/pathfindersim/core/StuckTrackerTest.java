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

package fr.riege.ebsl.tools.pathfindersim.core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StuckTrackerTest {
    @Test
    void pathProgressPreventsFalseStuckWhenGoalDistanceTemporarilyGrows() {
        StuckTracker tracker = new StuckTracker(3, 0.01);

        assertFalse(tracker.update(10.0, 0.0));
        assertFalse(tracker.update(10.5, 0.5));
        assertFalse(tracker.update(11.0, 1.0));
        assertFalse(tracker.update(11.5, 1.5));
    }

    @Test
    void stagnantPathProgressTriggersStuckEvent() {
        StuckTracker tracker = new StuckTracker(3, 0.01);

        assertFalse(tracker.update(10.0, 0.0));
        assertFalse(tracker.update(10.1, 0.0));
        assertFalse(tracker.update(10.2, 0.0));
        assertTrue(tracker.update(10.3, 0.0));
        assertTrue(tracker.consumeStuckEvent());
    }
}
