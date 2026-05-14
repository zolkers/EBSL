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
import fr.riege.ebsl.common.pathfinding.pathing.configuration.PathfinderConfiguration;
import fr.riege.ebsl.common.pathfinding.pathing.result.PathState;
import fr.riege.ebsl.common.pathfinding.pathing.result.PathfinderResults;
import fr.riege.ebsl.common.pathfinding.pathing.result.Paths;
import fr.riege.ebsl.common.pathfinding.quality.PathQualityGrade;
import fr.riege.ebsl.common.pathfinding.quality.PathQualityReport;
import fr.riege.ebsl.common.pathfinding.wrapper.PathPosition;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PathHandoffControllerTest {
    private final PathHandoffController controller = new PathHandoffController();

    @Test
    void acceptsShorterStableCandidate() {
        var decision = controller.evaluate(
            candidate(6.0, 0.80, node(0, 64, 0), node(6, 64, 0)),
            List.of(node(0, 64, 0), node(20, 64, 0)),
            20.0,
            new Vec3d(0.5, 64.0, 0.5),
            Node.MoveType.WALK,
            true);

        assertTrue(decision.accepted());
    }

    @Test
    void rejectsCandidateWhileMovementIsUnsafe() {
        var decision = controller.evaluate(
            candidate(6.0, 0.95, node(0, 64, 0), node(6, 64, 0)),
            List.of(node(0, 64, 0), node(20, 64, 0)),
            20.0,
            new Vec3d(0.5, 64.0, 0.5),
            Node.MoveType.PARKOUR,
            true);

        assertFalse(decision.accepted());
    }

    @Test
    void rejectsCandidateThatIsNotBetterEnough() {
        var decision = controller.evaluate(
            candidate(19.5, 0.72, node(0, 64, 0), node(20, 64, 0)),
            List.of(node(0, 64, 0), node(20, 64, 0)),
            20.0,
            new Vec3d(0.5, 64.0, 0.5),
            Node.MoveType.WALK,
            true);

        assertFalse(decision.accepted());
    }

    private static SpeculativePathCandidate candidate(double length, double quality, Node... nodes) {
        List<Node> path = List.of(nodes);
        PathPosition start = path.getFirst().position;
        PathPosition target = path.getLast().position;
        return new SpeculativePathCandidate(
            new PathPlan(
                PathfinderResults.of(PathState.FOUND, Paths.of(start, target, path.stream().map(node -> node.position).toList())),
                PathfinderConfiguration.DEFAULT,
                path.stream().map(node -> node.position).toList(),
                path,
                path,
                length,
                new PathQualityReport(quality, PathQualityGrade.GOOD, List.of())),
            start,
            target.flooredX(),
            target.flooredY(),
            target.flooredZ(),
            128);
    }

    private static Node node(int x, int y, int z) {
        return new Node(new PathPosition(x, y, z));
    }
}
