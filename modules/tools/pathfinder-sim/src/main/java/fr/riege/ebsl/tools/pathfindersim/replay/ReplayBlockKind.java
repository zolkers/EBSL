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

package fr.riege.ebsl.tools.pathfindersim.replay;

import fr.riege.ebsl.common.domain.world.BlockId;
import fr.riege.ebsl.common.navigation.runtime.headless.HeadlessBlockState;
import fr.riege.ebsl.common.navigation.runtime.headless.HeadlessPhysicsBlockType;

import java.util.Arrays;
import java.util.List;

public enum ReplayBlockKind {
    WATER("water", 0x2B6FB8, HeadlessPhysicsBlockType.WATER),
    CLIMBABLE("climbable", 0xBA8A48, HeadlessPhysicsBlockType.CLIMBABLE),
    DANGER("danger", 0xB43F37, HeadlessPhysicsBlockType.LAVA,
        PathRule.exact("fire", "soul_fire", "magma_block")),
    GRASS("grass", 0x528443, PathRule.exact("grass_block", "moss_block", "moss_carpet", "podzol")),
    LEAVES("leaves", 0x367037, PathRule.suffix("_leaves"), PathRule.exact("azalea", "flowering_azalea")),
    SLAB("slab", 0x9C927E, PathRule.suffix("_slab")),
    STAIR("stair", 0x8C7E68, PathRule.suffix("_stairs")),
    SAND("sand", 0xC2B171,
        PathRule.exact("sand", "red_sand"),
        PathRule.suffix("_sandstone", "_terracotta")),
    SNOW("snow", 0xD9E4E6, HeadlessPhysicsBlockType.ICE,
        PathRule.exact("snow", "snow_block", "powder_snow"),
        PathRule.suffix("_ice")),
    EARTH("earth", 0x6F5438,
        PathRule.exact("dirt", "coarse_dirt", "rooted_dirt", "mud", "clay", "gravel")),
    WOOD("wood", 0x825B35, PathRule.suffix("_log", "_planks", "_wood", "_stem", "_hyphae")),
    STONE("stone", 0x666C72,
        PathRule.exact("stone", "deepslate", "andesite", "diorite", "granite", "tuff"),
        PathRule.suffix("_ore", "_stone")),
    SOLID("solid", 0x5B6570);

    private final String key;
    private final int baseRgb;
    private final List<HeadlessPhysicsBlockType> physicsTypes;
    private final List<PathRule> pathRules;

    ReplayBlockKind(String key, int baseRgb) {
        this(key, baseRgb, List.of(), List.of());
    }

    ReplayBlockKind(String key, int baseRgb, HeadlessPhysicsBlockType physicsType) {
        this(key, baseRgb, List.of(physicsType), List.of());
    }

    ReplayBlockKind(String key, int baseRgb, PathRule... pathRules) {
        this(key, baseRgb, List.of(), Arrays.asList(pathRules));
    }

    ReplayBlockKind(String key, int baseRgb, HeadlessPhysicsBlockType physicsType, PathRule... pathRules) {
        this(key, baseRgb, List.of(physicsType), Arrays.asList(pathRules));
    }

    ReplayBlockKind(String key, int baseRgb, List<HeadlessPhysicsBlockType> physicsTypes, List<PathRule> pathRules) {
        this.key = key;
        this.baseRgb = baseRgb;
        this.physicsTypes = List.copyOf(physicsTypes);
        this.pathRules = List.copyOf(pathRules);
    }

    public static ReplayBlockKind classify(HeadlessBlockState state) {
        if (state.water()) {
            return WATER;
        }
        if (state.climbable()) {
            return CLIMBABLE;
        }
        if (state.dangerous() || state.lava()) {
            return DANGER;
        }
        for (ReplayBlockKind kind : values()) {
            if (kind.matches(state.id())) {
                return kind;
            }
        }
        return SOLID;
    }

    public String key() {
        return key;
    }

    public int baseRgb() {
        return baseRgb;
    }

    private boolean matches(BlockId id) {
        if (id == null) {
            return false;
        }
        for (HeadlessPhysicsBlockType physicsType : physicsTypes) {
            if (physicsType.matches(id)) {
                return true;
            }
        }
        for (PathRule rule : pathRules) {
            if (rule.matches(id.path())) {
                return true;
            }
        }
        return false;
    }

    private static final class PathRule {
        private final MatchMode mode;
        private final List<String> values;

        private PathRule(MatchMode mode, String... values) {
            this.mode = mode;
            this.values = List.copyOf(Arrays.asList(values));
        }

        static PathRule exact(String... values) {
            return new PathRule(MatchMode.EXACT, values);
        }

        static PathRule suffix(String... values) {
            return new PathRule(MatchMode.SUFFIX, values);
        }

        boolean matches(String path) {
            for (String value : values) {
                if (mode.matches(path, value)) {
                    return true;
                }
            }
            return false;
        }
    }

    private enum MatchMode {
        EXACT {
            @Override
            boolean matches(String path, String value) {
                return path.equals(value);
            }
        },
        SUFFIX {
            @Override
            boolean matches(String path, String value) {
                return path.endsWith(value);
            }
        };

        abstract boolean matches(String path, String value);
    }
}
