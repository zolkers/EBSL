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

package fr.riege.ebsl.common.pathfinding.quality;

import fr.riege.ebsl.common.pathfinding.Node;
import fr.riege.ebsl.common.pathfinding.settings.PathfinderSettings;

public final class MovementRiskScorer {
    private MovementRiskScorer() {
    }

    public static double risk(Node.MoveType type) {
        PathfinderSettings settings = PathfinderSettings.instance();
        return switch (type) {
            case WALK -> settings.qualityWalkRisk.value();
            case WALK_DIAGONAL -> settings.qualityDiagonalRisk.value();
            case STEP_DOWN -> settings.qualityStepDownRisk.value();
            case STEP_UP -> settings.qualityStepUpRisk.value();
            case SWIM -> settings.qualitySwimRisk.value();
            case CLIMB -> settings.qualityClimbRisk.value();
            case JUMP -> settings.qualityJumpRisk.value();
            case FALL -> settings.qualityFallRisk.value();
            case PARKOUR -> settings.qualityParkourRisk.value();
            case FLY -> settings.qualityFlyRisk.value();
        };
    }

    public static double planningPenalty(Node.MoveType type) {
        double risk = risk(type);
        if (risk <= 0.0) {
            return 0.0;
        }
        return risk + risk * risk * 2.0;
    }
}
