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

package fr.riege.ebsl.common.automation.aim;

import fr.riege.ebsl.common.domain.world.BlockId;
import fr.riege.ebsl.common.domain.world.BlockSelector;
import fr.riege.ebsl.common.math.Vec3d;
import fr.riege.ebsl.common.platform.EbslPlatform;
import fr.riege.ebsl.common.world.layer.IWorldLayer;

@SuppressWarnings("java:S107")
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
        BlockSelector selector = BlockSelector.parse(target);
        BlockSearch search = BlockSearch.around(platform.player().position(), radius);
        BestAimTarget best = new BestAimTarget(null, Double.MAX_VALUE);

        for (int dy = -search.radius; dy <= search.radius; dy++) {
            best = scanAimLayer(platform, world, selector, requireLineOfSight, search, dy, best);
        }
        return best.target;
    }

    private static BestAimTarget scanAimLayer(EbslPlatform platform,
                                              IWorldLayer world,
                                              BlockSelector selector,
                                              boolean requireLineOfSight,
                                              BlockSearch search,
                                              int dy,
                                              BestAimTarget best) {
        int y = search.y + dy;
        for (int dx = -search.radius; dx <= search.radius; dx++) {
            int x = search.x + dx;
            best = scanAimRow(platform, world, selector, requireLineOfSight, search, dx, dy, x, y, best);
        }
        return best;
    }

    private static BestAimTarget scanAimRow(EbslPlatform platform,
                                            IWorldLayer world,
                                            BlockSelector selector,
                                            boolean requireLineOfSight,
                                            BlockSearch search,
                                            int dx,
                                            int dy,
                                            int x,
                                            int y,
                                            BestAimTarget best) {
        for (int dz = -search.radius; dz <= search.radius; dz++) {
            int distanceSquared = dx * dx + dy * dy + dz * dz;
            if (distanceSquared <= search.radiusSquared) {
                best = chooseAimTarget(platform, world, selector, requireLineOfSight, search, x, y, dz, distanceSquared, best);
            }
        }
        return best;
    }

    private static BestAimTarget chooseAimTarget(EbslPlatform platform,
                                                 IWorldLayer world,
                                                 BlockSelector selector,
                                                 boolean requireLineOfSight,
                                                 BlockSearch search,
                                                 int x,
                                                 int y,
                                                 int dz,
                                                 int distanceSquared,
                                                 BestAimTarget best) {
        int z = search.z + dz;
        if (!world.isLoaded(x, y, z) || !selector.matches(world.getBlock(x, y, z))) {
            return best;
        }
        Vec3d aimPoint = aimPoint(platform, x, y, z, requireLineOfSight);
        if (aimPoint == null || distanceSquared >= best.distanceSquared) {
            return best;
        }
        return new BestAimTarget(new BlockAimTarget(x, y, z, aimPoint), distanceSquared);
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

    private record BestAimTarget(BlockAimTarget target, double distanceSquared) {
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
            if (!requireLineOfSight || platform.world().canRayTraceBlock(eye, point, x, y, z)) {
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
