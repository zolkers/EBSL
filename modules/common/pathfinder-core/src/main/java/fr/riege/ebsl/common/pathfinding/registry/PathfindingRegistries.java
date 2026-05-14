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

package fr.riege.ebsl.common.pathfinding.registry;

public final class PathfindingRegistries {
    private static final NodeProcessorCatalog NODE_PROCESSORS = new NodeProcessorCatalog();
    private static final PathCheckCatalog PATH_CHECKS = new PathCheckCatalog();
    private static final MovementEvaluatorCatalog MOVEMENT_EVALUATORS = new MovementEvaluatorCatalog();
    private static final PathQualityCatalog PATH_QUALITY = new PathQualityCatalog();

    private PathfindingRegistries() {
    }

    public static NodeProcessorCatalog nodeProcessors() {
        return NODE_PROCESSORS;
    }

    public static PathCheckCatalog pathChecks() {
        return PATH_CHECKS;
    }

    public static MovementEvaluatorCatalog movementEvaluators() {
        return MOVEMENT_EVALUATORS;
    }

    public static PathQualityCatalog pathQuality() {
        return PATH_QUALITY;
    }
}
