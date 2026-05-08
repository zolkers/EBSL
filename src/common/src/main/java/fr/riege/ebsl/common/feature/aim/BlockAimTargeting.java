package fr.riege.ebsl.common.feature.aim;

import fr.riege.ebsl.common.domain.world.BlockId;
import fr.riege.ebsl.common.domain.world.BlockSelector;
import fr.riege.ebsl.common.math.Vec3d;
import fr.riege.ebsl.common.platform.EbslPlatform;
import fr.riege.ebsl.common.platform.layer.IWorldLayer;

public final class BlockAimTargeting {
    private static final double[][] AIM_OFFSETS = {
        {0.5, 0.5, 0.5},
        {0.5, 0.75, 0.5},
        {0.5, 0.25, 0.5},
        {0.5, 0.5, 0.08},
        {0.5, 0.5, 0.92},
        {0.08, 0.5, 0.5},
        {0.92, 0.5, 0.5}
    };

    private BlockAimTargeting() {
    }

    public static BlockAimTarget nearest(EbslPlatform platform, String target, int radius, boolean requireLineOfSight) {
        IWorldLayer world = platform.world();
        Vec3d pos = platform.player().position();
        int px = (int) Math.floor(pos.x());
        int py = (int) Math.floor(pos.y());
        int pz = (int) Math.floor(pos.z());
        int searchRadius = Math.max(1, radius);
        int radiusSquared = searchRadius * searchRadius;
        BlockAimTarget best = null;
        double bestDistance = Double.MAX_VALUE;

        for (int dy = -searchRadius; dy <= searchRadius; dy++) {
            int y = py + dy;
            for (int dx = -searchRadius; dx <= searchRadius; dx++) {
                int x = px + dx;
                for (int dz = -searchRadius; dz <= searchRadius; dz++) {
                    int distanceSquared = dx * dx + dy * dy + dz * dz;
                    if (distanceSquared > radiusSquared) {
                        continue;
                    }
                    int z = pz + dz;
                    if (!world.isLoaded(x, y, z) || !matches(world.getBlock(x, y, z), target)) {
                        continue;
                    }
                    Vec3d aimPoint = aimPoint(platform, x, y, z, requireLineOfSight);
                    if (aimPoint == null) {
                        continue;
                    }
                    if (distanceSquared < bestDistance) {
                        bestDistance = distanceSquared;
                        best = new BlockAimTarget(x, y, z, aimPoint);
                    }
                }
            }
        }
        return best;
    }

    public static Vec3d aimPoint(EbslPlatform platform, int x, int y, int z, boolean requireLineOfSight) {
        Vec3d eye = platform.player().eyePosition();
        Vec3d fallback = null;
        double fallbackDistance = Double.MAX_VALUE;
        for (double[] offset : AIM_OFFSETS) {
            Vec3d point = new Vec3d(x + offset[0], y + offset[1], z + offset[2]);
            double distance = eye.distanceToSq(point);
            if (distance < fallbackDistance) {
                fallbackDistance = distance;
                fallback = point;
            }
            if (!requireLineOfSight || platform.world().hasLineOfSight(eye, point)) {
                return point;
            }
        }
        return requireLineOfSight ? null : fallback;
    }

    public static boolean matches(BlockId id, String target) {
        if (id == null || target == null || target.isBlank()) {
            return false;
        }
        return BlockSelector.parse(target).matches(id);
    }
}
