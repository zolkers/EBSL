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

record IterativeDepthProfile(int depth, int maxIterations, int maxLength,
                             int maxCalculationTimeMs, double qualityScale) {
    IterativeDepthProfile {
        depth = Math.max(1, depth);
        maxIterations = Math.max(1, maxIterations);
        maxLength = Math.max(1, maxLength);
        maxCalculationTimeMs = Math.max(0, maxCalculationTimeMs);
        qualityScale = Math.max(1.0, qualityScale);
    }
}
