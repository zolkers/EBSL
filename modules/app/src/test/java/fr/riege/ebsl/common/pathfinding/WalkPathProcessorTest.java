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

package fr.riege.ebsl.common.pathfinding;

import fr.riege.ebsl.common.domain.world.BlockId;
import fr.riege.ebsl.common.pathfinding.movement.MovementTerrain;
import fr.riege.ebsl.common.pathfinding.pathing.configuration.PathfinderConfiguration;
import fr.riege.ebsl.common.pathfinding.wrapper.PathPosition;
import fr.riege.ebsl.common.world.layer.IWorldLayer;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class WalkPathProcessorTest {
    @Test
    void processUsesConfiguredMovementClassifierForRawNodes() {
        PathfinderConfiguration configuration = PathfinderConfiguration.builder()
            .movementClassifier(context -> Node.MoveType.JUMP)
            .build();

        ProcessedPath path = WalkPathProcessor.process(List.of(
            new PathPosition(0, 64, 0),
            new PathPosition(1, 64, 0)
        ), configuration, null);

        assertEquals(Node.MoveType.JUMP, path.rawNodes().get(1).moveType());
    }

    @Test
    void processPreservesClimbTypeDuringRelabelingWithoutProvider() {
        ProcessedPath path = WalkPathProcessor.process(List.of(
            new PathPosition(0, 64, 0),
            new PathPosition(0, 65, 0)
        ), PathfinderConfiguration.DEFAULT, new ClimbTerrain());

        assertEquals(Node.MoveType.CLIMB, path.navigationPath().get(1).moveType());
    }

    @Test
    void processAllowsRawPathWhenNoTerrainIsAvailable() {
        ProcessedPath path = WalkPathProcessor.process(List.of(
            new PathPosition(0, 64, 0),
            new PathPosition(1, 64, 0),
            new PathPosition(2, 64, 0)
        ), PathfinderConfiguration.DEFAULT, null);

        assertEquals(3, path.rawNodes().size());
        assertEquals(3, path.navigationPath().size());
    }

    private static final class ClimbTerrain implements MovementTerrain {
        private static final IWorldLayer WORLD = new ClimbWorld();

        @Override public IWorldLayer world() { return WORLD; }
        @Override public void clearCache() {}
        @Override public boolean isWalkable(int x, int y, int z) { return y >= 64; }
        @Override public boolean isPassable(int x, int y, int z) { return true; }
        @Override public boolean isAir(int x, int y, int z) { return y >= 64; }
        @Override public boolean isSolid(int x, int y, int z) { return y < 64; }
        @Override public boolean isWater(int x, int y, int z) { return false; }
        @Override public boolean isClimbable(int x, int y, int z) { return x == 0 && z == 0 && y >= 64 && y <= 65; }
        @Override public boolean isDangerous(int x, int y, int z) { return false; }
        @Override public boolean hasWalkableTop(int x, int y, int z) { return y < 64; }
        @Override public double getTopY(int x, int y, int z) { return y < 64 ? 1.0 : 0.0; }
        @Override public boolean isLowPartialSupport(int x, int y, int z) { return false; }
        @Override public boolean isFullWallBlock(int x, int y, int z) { return false; }
        @Override public boolean isFullWall(int x, int y, int z) { return false; }
        @Override public boolean wouldSuffocate(int x, int y, int z) { return false; }
        @Override public boolean safeToFall(int fromY, int toX, int toY, int toZ) { return true; }
        @Override public boolean canOcclude(int x, int y, int z) { return false; }
        @Override public BlockId getBlock(int x, int y, int z) { return BlockId.AIR; }
        @Override public boolean isBlacklisted(int x, int y, int z) { return false; }
    }

    private static final class ClimbWorld implements IWorldLayer {
        @Override public BlockId getBlock(int x, int y, int z) { return BlockId.AIR; }
        @Override public boolean isAir(int x, int y, int z) { return y >= 64; }
        @Override public boolean isSolid(int x, int y, int z) { return y < 64; }
        @Override public boolean isWater(int x, int y, int z) { return false; }
        @Override public boolean isLava(int x, int y, int z) { return false; }
        @Override public boolean isClimbable(int x, int y, int z) { return x == 0 && z == 0 && y >= 64 && y <= 65; }
        @Override public boolean isLoaded(int x, int y, int z) { return true; }
        @Override public int getTopSolidY(int x, int z) { return 63; }
        @Override public double getBlockHeight(int x, int y, int z) { return y < 64 ? 1.0 : 0.0; }
    }
}
