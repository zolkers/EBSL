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

package fr.riege.ebsl.common.navigation;

final class DepthPlanSelector {
    private DepthPlanSelector() {
    }

    static PathPlan select(PathPlan primary, PathPlan candidate, double requiredImprovement) {
        if (!isUsable(candidate)) {
            return primary;
        }
        if (!isUsable(primary)) {
            return candidate;
        }
        if (primary.complete() && !candidate.complete()) {
            return primary;
        }
        if (!primary.complete() && candidate.complete()) {
            return candidate;
        }
        double improvement = candidate.quality().score() - primary.quality().score();
        return improvement >= Math.clamp(requiredImprovement, 0.0, 1.0) ? candidate : primary;
    }

    static boolean shouldReplace(PathPlan primary, PathPlan candidate, double requiredImprovement) {
        return select(primary, candidate, requiredImprovement) == candidate;
    }

    private static boolean isUsable(PathPlan plan) {
        return plan != null && plan.usable();
    }
}
