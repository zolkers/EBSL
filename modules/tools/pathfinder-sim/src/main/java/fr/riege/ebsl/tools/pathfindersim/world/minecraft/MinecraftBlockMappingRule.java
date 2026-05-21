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
import fr.riege.ebsl.tools.pathfindersim.world.BlockPathRule;

import java.util.function.Function;

enum MinecraftBlockMappingRule {
    AIR(BlockPathRule.exact("air", "cave_air", "void_air"), ignored -> HeadlessBlockState.AIR),
    WATER(BlockPathRule.exact("water"), HeadlessBlockState::water),
    LAVA(BlockPathRule.exact("lava"), HeadlessBlockState::lava),
    DANGER(BlockPathRule.exact("fire", "soul_fire", "magma_block"), HeadlessBlockState::danger),
    PASSABLE_DANGER(BlockPathRule.exact("sweet_berry_bush"), HeadlessBlockState::passableDanger),
    PASSABLE_SLOW(BlockPathRule.exact("powder_snow", "cobweb"), HeadlessBlockState::passable),
    CLIMBABLE(
        BlockPathRule.exact("ladder", "vine", "weeping_vines", "twisting_vines", "scaffolding"),
        HeadlessBlockState::climbable),
    HALF_SLAB(BlockPathRule.suffix("_slab"), id -> HeadlessBlockState.slab(id, 0.5)),
    SNOW_LAYER(BlockPathRule.exact("snow"), id -> HeadlessBlockState.slab(id, 0.125)),
    THIN_LAYER(BlockPathRule.suffix("_carpet", "_pressure_plate"), id -> HeadlessBlockState.slab(id, 0.125)),
    NON_COLLIDING_DECORATION(BlockPathRule.exactOrSuffix(
        "button",
        "torch",
        "redstone_torch",
        "soul_torch",
        "lever",
        "short_grass",
        "tall_grass",
        "fern",
        "large_fern",
        "dead_bush",
        "seagrass",
        "tall_seagrass",
        "kelp",
        "kelp_plant",
        "flower",
        "mushroom",
        "sapling",
        "roots",
        "sprouts"), ignored -> HeadlessBlockState.AIR);

    private final BlockPathRule matcher;
    private final Function<BlockId, HeadlessBlockState> mapper;

    MinecraftBlockMappingRule(BlockPathRule matcher, Function<BlockId, HeadlessBlockState> mapper) {
        this.matcher = matcher;
        this.mapper = mapper;
    }

    boolean matches(BlockId id) {
        return matcher.matches(id);
    }

    HeadlessBlockState map(BlockId id) {
        return mapper.apply(id);
    }
}
