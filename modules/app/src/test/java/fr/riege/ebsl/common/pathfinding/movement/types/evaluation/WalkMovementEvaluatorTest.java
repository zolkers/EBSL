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

package fr.riege.ebsl.common.pathfinding.movement.types.evaluation;

import fr.riege.ebsl.common.domain.world.BlockId;
import fr.riege.ebsl.common.math.Vec3d;
import fr.riege.ebsl.common.pathfinding.Node;
import fr.riege.ebsl.common.pathfinding.movement.MovementTerrain;
import fr.riege.ebsl.common.pathfinding.movement.WalkabilityChecker;
import fr.riege.ebsl.common.pathfinding.provider.NavigationPointProviders;
import fr.riege.ebsl.common.pathfinding.wrapper.PathPosition;
import fr.riege.ebsl.common.world.layer.IWorldLayer;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;

class WalkMovementEvaluatorTest {
    @Test
    void rejectsWalkTargetThatBecameBlocked() {
        FakeWorld world = new FakeWorld();
        world.solid(0, 63, 0);
        world.solid(1, 63, 0);
        world.solid(1, 64, 0);
        MovementTerrain checker = new WalkabilityChecker(world);

        MovementValidationResult result = new WalkMovementEvaluator().validate(
            context(checker, new PathPosition(0, 64, 0), new PathPosition(1, 64, 0)));

        assertFalse(result.valid());
    }

    private static MovementValidationContext context(MovementTerrain checker, PathPosition from, PathPosition target) {
        Node fromNode = new Node(from);
        Node targetNode = new Node(target);
        targetNode.setMoveType(Node.MoveType.WALK);
        return new MovementValidationContext(
            checker,
            NavigationPointProviders.worldBacked(checker),
            fromNode,
            targetNode,
            targetNode,
            new Vec3d(from.centeredX(), from.flooredY(), from.centeredZ()),
            0);
    }

    private static final class FakeWorld implements IWorldLayer {
        private final Set<String> solids = new HashSet<>();

        void solid(int x, int y, int z) {
            solids.add(key(x, y, z));
        }

        @Override public BlockId getBlock(int x, int y, int z) {
            return isSolid(x, y, z) ? new BlockId("test", "solid") : BlockId.AIR;
        }

        @Override public boolean isAir(int x, int y, int z) {
            return !isSolid(x, y, z);
        }

        @Override public boolean isSolid(int x, int y, int z) {
            return solids.contains(key(x, y, z));
        }

        @Override public boolean isWater(int x, int y, int z) {
            return false;
        }

        @Override public boolean isLava(int x, int y, int z) {
            return false;
        }

        @Override public boolean isLoaded(int x, int y, int z) {
            return true;
        }

        @Override public int getTopSolidY(int x, int z) {
            return 63;
        }

        @Override public double getBlockHeight(int x, int y, int z) {
            return isSolid(x, y, z) ? 1.0 : 0.0;
        }

        private static String key(int x, int y, int z) {
            return x + "," + y + "," + z;
        }
    }
}
