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

package fr.riege.ebsl.common.navigation.runtime.headless;

import fr.riege.ebsl.common.domain.world.BlockId;

public record HeadlessBlockState(
    BlockId id,
    boolean solid,
    boolean water,
    boolean lava,
    boolean dangerous,
    boolean climbable,
    double height
) {
    public static final HeadlessBlockState AIR = new HeadlessBlockState(BlockId.AIR, false, false, false, false, false, 0.0);
    public static final HeadlessBlockState STONE = solid(BlockId.of("minecraft:stone"));

    public static HeadlessBlockState solid(BlockId id) {
        return new HeadlessBlockState(id, true, false, false, false, false, 1.0);
    }

    public static HeadlessBlockState slab(BlockId id, double height) {
        return new HeadlessBlockState(id, true, false, false, false, false, Math.clamp(height, 0.0, 1.0));
    }

    public static HeadlessBlockState climbable(BlockId id) {
        return new HeadlessBlockState(id, false, false, false, false, true, 0.0);
    }

    public boolean isAir() {
        return !solid && !water && !lava && height <= 0.0 && BlockId.AIR.equals(id);
    }
}
