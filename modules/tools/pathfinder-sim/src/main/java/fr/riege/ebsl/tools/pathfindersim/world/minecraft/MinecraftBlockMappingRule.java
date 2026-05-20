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

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

enum MinecraftBlockMappingRule {
    AIR(MinecraftBlockMatcher.exact("air", "cave_air", "void_air"), ignored -> HeadlessBlockState.AIR),
    WATER(MinecraftBlockMatcher.exact("water"), HeadlessBlockState::water),
    LAVA(MinecraftBlockMatcher.exact("lava"), HeadlessBlockState::lava),
    DANGER(MinecraftBlockMatcher.exact("fire", "soul_fire", "magma_block"), HeadlessBlockState::danger),
    CLIMBABLE(
        MinecraftBlockMatcher.exact("ladder", "vine", "weeping_vines", "twisting_vines", "scaffolding"),
        HeadlessBlockState::climbable),
    HALF_SLAB(MinecraftBlockMatcher.suffix("_slab"), id -> HeadlessBlockState.slab(id, 0.5)),
    SNOW_LAYER(MinecraftBlockMatcher.exact("snow"), id -> HeadlessBlockState.slab(id, 0.125)),
    THIN_LAYER(MinecraftBlockMatcher.suffix("_carpet", "_pressure_plate"), id -> HeadlessBlockState.slab(id, 0.125)),
    NON_COLLIDING_DECORATION(MinecraftBlockMatcher.nonCollidingDecoration(), ignored -> HeadlessBlockState.AIR);

    private final MinecraftBlockMatcher matcher;
    private final Function<BlockId, HeadlessBlockState> mapper;

    MinecraftBlockMappingRule(MinecraftBlockMatcher matcher, Function<BlockId, HeadlessBlockState> mapper) {
        this.matcher = matcher;
        this.mapper = mapper;
    }

    boolean matches(BlockId id) {
        return matcher.matches(id);
    }

    HeadlessBlockState map(BlockId id) {
        return mapper.apply(id);
    }

    private static final class MinecraftBlockMatcher {
        private final MatchMode mode;
        private final List<String> paths;

        private MinecraftBlockMatcher(MatchMode mode, String... paths) {
            this.mode = mode;
            this.paths = List.copyOf(Arrays.asList(paths));
        }

        static MinecraftBlockMatcher exact(String... paths) {
            return new MinecraftBlockMatcher(MatchMode.EXACT, paths);
        }

        static MinecraftBlockMatcher suffix(String... paths) {
            return new MinecraftBlockMatcher(MatchMode.SUFFIX, paths);
        }

        static MinecraftBlockMatcher nonCollidingDecoration() {
            return new MinecraftBlockMatcher(MatchMode.EXACT_OR_SUFFIX,
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
                "sprouts");
        }

        boolean matches(BlockId id) {
            if (id == null) {
                return false;
            }
            for (String path : paths) {
                if (mode.matches(id.path(), path)) {
                    return true;
                }
            }
            return false;
        }

    }

    private enum MatchMode {
        EXACT {
            @Override
            boolean matches(String actual, String expected) {
                return actual.equals(expected);
            }
        },
        SUFFIX {
            @Override
            boolean matches(String actual, String expected) {
                return actual.endsWith(expected);
            }
        },
        EXACT_OR_SUFFIX {
            @Override
            boolean matches(String actual, String expected) {
                return actual.equals(expected) || actual.endsWith("_" + expected);
            }
        };

        abstract boolean matches(String actual, String expected);
    }
}
