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

import fr.riege.ebsl.common.math.Vec3d;
import fr.riege.ebsl.common.pathfinding.Node;
import fr.riege.ebsl.common.pathfinding.settings.PathfinderSettings;

import java.util.List;

final class PathHandoffController {
    HandoffDecision evaluate(SpeculativePathCandidate candidate,
                             List<Node> activePath,
                             double activeRemainingDistance,
                             Vec3d playerPos,
                             Node.MoveType currentMoveType,
                             boolean playerStable) {
        if (candidate == null || !candidate.usable() || activePath == null || activePath.size() < 2) {
            return HandoffDecision.reject("missing path");
        }
        if (!playerStable || !isSafeMoveType(currentMoveType)) {
            return HandoffDecision.reject("unstable movement");
        }
        double startDistance = distanceToStart(candidate, playerPos);
        if (startDistance > PathfinderSettings.instance().speculativeHandoffStartDistance.value()) {
            return HandoffDecision.reject("candidate start too far");
        }
        double candidateLength = candidate.plan().pathLength();
        double lengthGain = activeRemainingDistance - candidateLength;
        double minLengthGain = Math.max(1.0,
            activeRemainingDistance * PathfinderSettings.instance().speculativeMinLengthImprovement.value());
        double qualityGain = candidate.plan().quality().score() - activeQuality(activePath);
        if (lengthGain >= minLengthGain) {
            return HandoffDecision.accept("shorter speculative path");
        }
        if (qualityGain >= PathfinderSettings.instance().speculativeMinQualityGain.value()
            && candidateLength <= activeRemainingDistance * 1.10) {
            return HandoffDecision.accept("higher quality speculative path");
        }
        return HandoffDecision.reject("not better enough");
    }

    private static boolean isSafeMoveType(Node.MoveType moveType) {
        return moveType == null
            || moveType == Node.MoveType.WALK
            || moveType == Node.MoveType.WALK_DIAGONAL
            || moveType == Node.MoveType.SWIM
            || moveType == Node.MoveType.CLIMB;
    }

    private static double distanceToStart(SpeculativePathCandidate candidate, Vec3d playerPos) {
        Node start = candidate.nodes().getFirst();
        double dx = start.position.centeredX() - playerPos.x();
        double dy = start.position.flooredY() - playerPos.y();
        double dz = start.position.centeredZ() - playerPos.z();
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    private static double activeQuality(List<Node> activePath) {
        int risky = 0;
        int total = 0;
        for (Node node : activePath) {
            if (node == null || node.moveType() == null) {
                continue;
            }
            total++;
            if (node.moveType() == Node.MoveType.PARKOUR
                || node.moveType() == Node.MoveType.FALL
                || node.moveType() == Node.MoveType.JUMP) {
                risky++;
            }
        }
        if (total == 0) {
            return 0.0;
        }
        return 1.0 - risky / (double) total;
    }

    record HandoffDecision(boolean accepted, String reason) {
        static HandoffDecision accept(String reason) {
            return new HandoffDecision(true, reason);
        }

        static HandoffDecision reject(String reason) {
            return new HandoffDecision(false, reason);
        }
    }
}
