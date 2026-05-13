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

package fr.riege.ebsl.common.feature.aim;

import fr.riege.ebsl.common.domain.world.BlockSelector;
import fr.riege.ebsl.common.math.Vec3d;
import fr.riege.ebsl.common.pathfinding.movement.MovementTerrain;
import fr.riege.ebsl.common.pathfinding.movement.WalkabilityChecker;
import fr.riege.ebsl.common.pathfinding.wrapper.PathPosition;
import fr.riege.ebsl.common.platform.EbslPlatform;
import fr.riege.ebsl.common.world.layer.IWorldLayer;

@SuppressWarnings("java:S107")
public final class BlockInteractionTargeting {
    private static final double PLAYER_EYE_HEIGHT = 1.62;
    private static final double ABOVE_TARGET_PENALTY = 36.0;
    private static final double ON_TARGET_TOP_PENALTY = 48.0;
    private static final double BLOCK_SELECTION_WEIGHT = 0.08;

    private BlockInteractionTargeting() {
    }

    public static BlockInteractionTarget nearestReachable(EbslPlatform platform, String target, int searchRadius, int reachRadius) {
        IWorldLayer world = platform.world();
        BlockSelector selector = BlockSelector.parse(target);
        Vec3d playerPos = platform.player().position();
        BlockSearch search = BlockSearch.around(playerPos, searchRadius);
        BestInteractionTarget best = new BestInteractionTarget(null, Double.MAX_VALUE);

        for (int dy = -search.radius; dy <= search.radius; dy++) {
            best = scanTargetLayer(platform, world, selector, playerPos, search, reachRadius, dy, best);
        }
        return best.target;
    }

    public static PathPosition bestStandingPosition(IWorldLayer world, Vec3d playerPos, int blockX, int blockY, int blockZ, int reachRadius) {
        MovementTerrain checker = new WalkabilityChecker(world);
        int effectiveReachRadius = Math.max(1, reachRadius);
        double reachSquared = (double) effectiveReachRadius * effectiveReachRadius;
        BestStandingPosition best = new BestStandingPosition(null, Double.MAX_VALUE);

        for (int dy = -effectiveReachRadius; dy <= effectiveReachRadius; dy++) {
            best = scanStandingLayer(world, checker, playerPos, blockX, blockY, blockZ, effectiveReachRadius, reachSquared, dy, best);
        }
        return best.position;
    }

    private static BestInteractionTarget scanTargetLayer(EbslPlatform platform,
                                                         IWorldLayer world,
                                                         BlockSelector selector,
                                                         Vec3d playerPos,
                                                         BlockSearch search,
                                                         int reachRadius,
                                                         int dy,
                                                         BestInteractionTarget best) {
        int y = search.y + dy;
        for (int dx = -search.radius; dx <= search.radius; dx++) {
            int x = search.x + dx;
            best = scanTargetRow(platform, world, selector, playerPos, search, reachRadius, dx, dy, x, y, best);
        }
        return best;
    }

    private static BestInteractionTarget scanTargetRow(EbslPlatform platform,
                                                       IWorldLayer world,
                                                       BlockSelector selector,
                                                       Vec3d playerPos,
                                                       BlockSearch search,
                                                       int reachRadius,
                                                       int dx,
                                                       int dy,
                                                       int x,
                                                       int y,
                                                       BestInteractionTarget best) {
        for (int dz = -search.radius; dz <= search.radius; dz++) {
            int distanceSquared = dx * dx + dy * dy + dz * dz;
            if (distanceSquared <= search.radiusSquared) {
                best = chooseInteractionTarget(platform, world, selector, playerPos, reachRadius, x, y, search.z + dz, distanceSquared, best);
            }
        }
        return best;
    }

    private static BestInteractionTarget chooseInteractionTarget(EbslPlatform platform,
                                                                 IWorldLayer world,
                                                                 BlockSelector selector,
                                                                 Vec3d playerPos,
                                                                 int reachRadius,
                                                                 int x,
                                                                 int y,
                                                                 int z,
                                                                 int distanceSquared,
                                                                 BestInteractionTarget best) {
        if (!world.isLoaded(x, y, z) || !selector.matches(world.getBlock(x, y, z))) {
            return best;
        }
        Vec3d aimPoint = BlockAimTargeting.aimPoint(platform, x, y, z, false);
        PathPosition standing = bestStandingPosition(world, playerPos, x, y, z, reachRadius);
        if (aimPoint == null || standing == null) {
            return best;
        }
        double targetScore = score(playerPos, standing, x, y, z, distanceSquared);
        if (targetScore >= best.score) {
            return best;
        }
        return new BestInteractionTarget(new BlockInteractionTarget(new BlockAimTarget(x, y, z, aimPoint), standing), targetScore);
    }

    private static BestStandingPosition scanStandingLayer(IWorldLayer world,
                                                          MovementTerrain checker,
                                                          Vec3d playerPos,
                                                          int blockX,
                                                          int blockY,
                                                          int blockZ,
                                                          int radius,
                                                          double reachSquared,
                                                          int dy,
                                                          BestStandingPosition best) {
        int y = blockY + dy;
        for (int dx = -radius; dx <= radius; dx++) {
            int x = blockX + dx;
            best = scanStandingRow(world, checker, playerPos, blockX, blockY, blockZ, radius, reachSquared, x, y, best);
        }
        return best;
    }

    private static BestStandingPosition scanStandingRow(IWorldLayer world,
                                                        MovementTerrain checker,
                                                        Vec3d playerPos,
                                                        int blockX,
                                                        int blockY,
                                                        int blockZ,
                                                        int radius,
                                                        double reachSquared,
                                                        int x,
                                                        int y,
                                                        BestStandingPosition best) {
        for (int dz = -radius; dz <= radius; dz++) {
            best = chooseStandingPosition(world, checker, playerPos, blockX, blockY, blockZ, reachSquared, x, y, blockZ + dz, best);
        }
        return best;
    }

    private static BestStandingPosition chooseStandingPosition(IWorldLayer world,
                                                               MovementTerrain checker,
                                                               Vec3d playerPos,
                                                               int blockX,
                                                               int blockY,
                                                               int blockZ,
                                                               double reachSquared,
                                                               int x,
                                                               int y,
                                                               int z,
                                                               BestStandingPosition best) {
        if (!world.isLoaded(x, y, z) || !checker.isWalkable(x, y, z)) {
            return best;
        }
        if (interactionDistanceSquared(x, y, z, blockX, blockY, blockZ) > reachSquared) {
            return best;
        }
        PathPosition candidate = new PathPosition(x, y, z);
        double candidateScore = score(playerPos, candidate, blockX, blockY, blockZ, 0);
        return candidateScore < best.score ? new BestStandingPosition(candidate, candidateScore) : best;
    }

    private static double interactionDistanceSquared(int standX, int standY, int standZ, int blockX, int blockY, int blockZ) {
        double dx = (blockX + 0.5) - (standX + 0.5);
        double dy = (blockY + 0.5) - (standY + PLAYER_EYE_HEIGHT);
        double dz = (blockZ + 0.5) - (standZ + 0.5);
        return dx * dx + dy * dy + dz * dz;
    }

    private static double score(Vec3d playerPos, PathPosition standing, int blockX, int blockY, int blockZ, int blockDistanceSquared) {
        double dx = (standing.centeredX()) - playerPos.x();
        double dy = standing.flooredY() - playerPos.y();
        double dz = (standing.centeredZ()) - playerPos.z();
        double score = dx * dx + dy * dy + dz * dz + blockDistanceSquared * BLOCK_SELECTION_WEIGHT;
        int standingY = standing.flooredY();
        if (standingY > blockY) {
            score += (standingY - blockY) * ABOVE_TARGET_PENALTY;
        }
        if (standingY == blockY + 1 && standing.flooredX() == blockX && standing.flooredZ() == blockZ) {
            score += ON_TARGET_TOP_PENALTY;
        }
        return score;
    }

    private record BlockSearch(int x, int y, int z, int radius, int radiusSquared) {
        private static BlockSearch around(Vec3d pos, int radius) {
            int searchRadius = Math.max(1, radius);
            return new BlockSearch(
                (int) Math.floor(pos.x()),
                (int) Math.floor(pos.y()),
                (int) Math.floor(pos.z()),
                searchRadius,
                searchRadius * searchRadius);
        }
    }

    private record BestInteractionTarget(BlockInteractionTarget target, double score) {
    }

    private record BestStandingPosition(PathPosition position, double score) {
    }
}
