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
import fr.riege.ebsl.tools.pathfindersim.world.BlockPathRule;

import java.util.List;

public enum ReplayBlockKind {
    WATER("water", 0x2B6FB8, HeadlessPhysicsBlockType.WATER),
    CLIMBABLE("climbable", 0xBA8A48, HeadlessPhysicsBlockType.CLIMBABLE),
    DANGER("danger", 0xB43F37, HeadlessPhysicsBlockType.LAVA,
        BlockPathRule.exact("fire", "soul_fire", "magma_block")),
    GRASS("grass", 0x528443, BlockPathRule.exact("grass_block", "moss_block", "moss_carpet", "podzol")),
    LEAVES("leaves", 0x367037, BlockPathRule.suffix("_leaves"), BlockPathRule.exact("azalea", "flowering_azalea")),
    SLAB("slab", 0x9C927E, BlockPathRule.suffix("_slab")),
    STAIR("stair", 0x8C7E68, BlockPathRule.suffix("_stairs")),
    SAND("sand", 0xC2B171,
        BlockPathRule.exact("sand", "red_sand"),
        BlockPathRule.suffix("_sandstone", "_terracotta")),
    SNOW("snow", 0xD9E4E6, HeadlessPhysicsBlockType.ICE,
        BlockPathRule.exact("snow", "snow_block", "powder_snow"),
        BlockPathRule.suffix("_ice")),
    EARTH("earth", 0x6F5438,
        BlockPathRule.exact("dirt", "coarse_dirt", "rooted_dirt", "mud", "clay", "gravel")),
    WOOD("wood", 0x825B35, BlockPathRule.suffix("_log", "_planks", "_wood", "_stem", "_hyphae")),
    STONE("stone", 0x666C72,
        BlockPathRule.exact("stone", "deepslate", "andesite", "diorite", "granite", "tuff"),
        BlockPathRule.suffix("_ore", "_stone")),
    SOLID("solid", 0x5B6570);

    private final String key;
    private final int baseRgb;
    private final List<HeadlessPhysicsBlockType> physicsTypes;
    private final List<BlockPathRule> pathRules;

    ReplayBlockKind(String key, int baseRgb) {
        this(key, baseRgb, List.of(), List.of());
    }

    ReplayBlockKind(String key, int baseRgb, HeadlessPhysicsBlockType physicsType) {
        this(key, baseRgb, List.of(physicsType), List.of());
    }

    ReplayBlockKind(String key, int baseRgb, BlockPathRule... pathRules) {
        this(key, baseRgb, List.of(), List.of(pathRules));
    }

    ReplayBlockKind(String key, int baseRgb, HeadlessPhysicsBlockType physicsType, BlockPathRule... pathRules) {
        this(key, baseRgb, List.of(physicsType), List.of(pathRules));
    }

    ReplayBlockKind(String key, int baseRgb, List<HeadlessPhysicsBlockType> physicsTypes, List<BlockPathRule> pathRules) {
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

    public static ReplayBlockKind fromKey(String key) {
        for (ReplayBlockKind kind : values()) {
            if (kind.key.equals(key)) {
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
        for (BlockPathRule rule : pathRules) {
            if (rule.matches(id.path())) {
                return true;
            }
        }
        return false;
    }
}
