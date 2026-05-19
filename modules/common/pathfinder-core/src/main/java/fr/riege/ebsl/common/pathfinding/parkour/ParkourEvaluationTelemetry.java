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

package fr.riege.ebsl.common.pathfinding.parkour;

import fr.riege.ebsl.common.pathfinding.diagnostics.PathfindingDiagnostics;
import fr.riege.ebsl.common.pathfinding.settings.PathfinderSettings;
import fr.riege.ebsl.common.pathfinding.wrapper.PathPosition;

import java.util.Locale;

public final class ParkourEvaluationTelemetry {
    private static final long MIN_INTERVAL_MS = 75;
    private static long lastRecordTime;

    private ParkourEvaluationTelemetry() {
    }

    public static void recordEvaluation(PathPosition from, PathPosition to, ParkourJumpPlan plan) {
        boolean debugEnabled = Boolean.TRUE.equals(PathfinderSettings.instance().showDebug.value());
        if (plan == null || plan.feasible() && !debugEnabled) {
            return;
        }
        long now = System.currentTimeMillis();
        if (now - lastRecordTime < MIN_INTERVAL_MS) {
            return;
        }
        lastRecordTime = now;

        int dx = to.flooredX() - from.flooredX();
        int dz = to.flooredZ() - from.flooredZ();
        int offset = ParkourGeometry.distanceBlocks(dx, dz);
        int gap = ParkourGeometry.isDiagonal(dx, dz)
            ? ParkourGeometry.diagonalGapBlocks(dx, dz)
            : ParkourGeometry.cardinalGapBlocks(dx, dz);

        String detail = plan.detail() == null || plan.detail().isBlank()
            ? ""
            : " detail=" + plan.detail();
        String message = String.format(Locale.ROOT,
            "%d,%d,%d -> %d,%d,%d gap=%d off=%d dy=%.2f approach=%d req=%.2f est=%.2f reason=%s%s",
            from.flooredX(), from.flooredY(), from.flooredZ(),
            to.flooredX(), to.flooredY(), to.flooredZ(),
            gap,
            offset,
            plan.verticalDelta(),
            plan.approachBlocks(),
            plan.requiredReach(),
            plan.estimatedReach(),
            plan.reason(),
            detail);
        PathfindingDiagnostics.recordTelemetry("parkour-eval", message);
    }
}
