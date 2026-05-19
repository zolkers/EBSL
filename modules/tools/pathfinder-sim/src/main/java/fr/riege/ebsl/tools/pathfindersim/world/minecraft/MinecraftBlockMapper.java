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

package fr.riege.ebsl.tools.pathfindersim.world.minecraft;

import fr.riege.ebsl.common.domain.world.BlockId;
import fr.riege.ebsl.common.navigation.runtime.headless.HeadlessBlockState;

final class MinecraftBlockMapper {
    private MinecraftBlockMapper() {
    }

    static HeadlessBlockState map(String blockName) {
        String name = blockName == null ? "minecraft:air" : blockName;
        if (name.endsWith(":air") || name.endsWith(":cave_air") || name.endsWith(":void_air")) {
            return HeadlessBlockState.AIR;
        }
        if (name.contains("water")) {
            return new HeadlessBlockState(BlockId.of(name), false, true, false, false, false, 0.0);
        }
        if (name.contains("lava") || name.contains("fire") || name.contains("magma_block")) {
            return new HeadlessBlockState(BlockId.of(name), true, false, name.contains("lava"), true, false, 1.0);
        }
        if (name.contains("ladder") || name.contains("vine") || name.contains("scaffolding")) {
            return HeadlessBlockState.climbable(BlockId.of(name));
        }
        if (name.contains("slab")) {
            return HeadlessBlockState.slab(BlockId.of(name), 0.5);
        }
        if (name.contains("carpet") || name.contains("snow")) {
            return HeadlessBlockState.slab(BlockId.of(name), 0.125);
        }
        if (name.contains("torch") || name.contains("flower") || name.contains("grass") || name.contains("button")) {
            return HeadlessBlockState.AIR;
        }
        return HeadlessBlockState.solid(BlockId.of(name));
    }
}
