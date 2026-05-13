/*
 * This file is part of EBSL.
 *
 * EBSL is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * EBSL is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package fr.riege.ebsl.common.pathfinding.check;

import fr.riege.ebsl.common.core.registry.MapRegistry;
import fr.riege.ebsl.common.pathfinding.annotation.PathingStage;

@PathingStage(PathingStage.Stage.RECOVERY)
public final class PathCheckRegistry {
    private static final MapRegistry<String, PathCheck> CHECKS = new MapRegistry<>(null);

    static {
        register("anomalous_cutoff", new AnomalousPathCutoffCheck());
        register("huge_deviation", new HugeDeviationCheck());
        register("sustained_off_path", new SustainedOffPathCheck());
        register("smart_cutoff", new SmartCutoffCheck());
    }

    private PathCheckRegistry() {
    }

    public static PathCheckResult evaluate(PathCheckContext context) {
        PathCheckResult movementResult = MovementPathCheckRegistry.evaluate(context);
        if (movementResult.requiresAction()) {
            return movementResult;
        }
        for (PathCheck check : CHECKS.values()) {
            PathCheckResult result = check.evaluate(context);
            if (result.requiresAction()) {
                return result;
            }
        }
        return PathCheckResult.none();
    }

    private static void register(String id, PathCheck check) {
        CHECKS.register(id, check);
    }
}
