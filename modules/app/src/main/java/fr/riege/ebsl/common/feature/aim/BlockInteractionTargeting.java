package fr.riege.ebsl.common.feature.aim;

import fr.riege.ebsl.common.math.Vec3d;
import fr.riege.ebsl.common.domain.world.BlockSelector;
import fr.riege.ebsl.common.pathfinding.movement.WalkabilityChecker;
import fr.riege.ebsl.common.pathfinding.wrapper.PathPosition;
import fr.riege.ebsl.common.platform.EbslPlatform;
import fr.riege.ebsl.common.world.layer.IWorldLayer;

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
        int px = (int) Math.floor(playerPos.x());
        int py = (int) Math.floor(playerPos.y());
        int pz = (int) Math.floor(playerPos.z());
        int effectiveSearchRadius = Math.max(1, searchRadius);
        int radiusSquared = effectiveSearchRadius * effectiveSearchRadius;
        BlockInteractionTarget best = null;
        double bestScore = Double.MAX_VALUE;

        for (int dy = -effectiveSearchRadius; dy <= effectiveSearchRadius; dy++) {
            int y = py + dy;
            for (int dx = -effectiveSearchRadius; dx <= effectiveSearchRadius; dx++) {
                int x = px + dx;
                for (int dz = -effectiveSearchRadius; dz <= effectiveSearchRadius; dz++) {
                    int searchDistanceSquared = dx * dx + dy * dy + dz * dz;
                    if (searchDistanceSquared > radiusSquared) {
                        continue;
                    }
                    int z = pz + dz;
                    if (!world.isLoaded(x, y, z) || !selector.matches(world.getBlock(x, y, z))) {
                        continue;
                    }
                    Vec3d aimPoint = BlockAimTargeting.aimPoint(platform, x, y, z, false);
                    if (aimPoint == null) {
                        continue;
                    }
                    PathPosition standing = bestStandingPosition(world, playerPos, x, y, z, reachRadius);
                    if (standing == null) {
                        continue;
                    }
                    double score = score(playerPos, standing, x, y, z, searchDistanceSquared);
                    if (score < bestScore) {
                        bestScore = score;
                        best = new BlockInteractionTarget(new BlockAimTarget(x, y, z, aimPoint), standing);
                    }
                }
            }
        }
        return best;
    }

    public static PathPosition bestStandingPosition(IWorldLayer world, Vec3d playerPos, int blockX, int blockY, int blockZ, int reachRadius) {
        WalkabilityChecker checker = new WalkabilityChecker(world);
        int effectiveReachRadius = Math.max(1, reachRadius);
        double reachSquared = effectiveReachRadius * effectiveReachRadius;
        PathPosition best = null;
        double bestScore = Double.MAX_VALUE;

        for (int dy = -effectiveReachRadius; dy <= effectiveReachRadius; dy++) {
            int y = blockY + dy;
            for (int dx = -effectiveReachRadius; dx <= effectiveReachRadius; dx++) {
                int x = blockX + dx;
                for (int dz = -effectiveReachRadius; dz <= effectiveReachRadius; dz++) {
                    int z = blockZ + dz;
                    if (!world.isLoaded(x, y, z) || !checker.isWalkable(x, y, z)) {
                        continue;
                    }
                    double interactionDistanceSquared = interactionDistanceSquared(x, y, z, blockX, blockY, blockZ);
                    if (interactionDistanceSquared > reachSquared) {
                        continue;
                    }
                    PathPosition candidate = new PathPosition(x, y, z);
                    double score = score(playerPos, candidate, blockX, blockY, blockZ, 0);
                    if (score < bestScore) {
                        bestScore = score;
                        best = candidate;
                    }
                }
            }
        }
        return best;
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
}
