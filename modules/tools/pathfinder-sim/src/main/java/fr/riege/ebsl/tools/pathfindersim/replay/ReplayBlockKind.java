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

public enum ReplayBlockKind {
    WATER("water", 0x2B6FB8, HeadlessPhysicsBlockType.WATER),
    CLIMBABLE("climbable", 0xBA8A48, HeadlessPhysicsBlockType.CLIMBABLE),
    DANGER("danger", 0xB43F37, HeadlessPhysicsBlockType.LAVA, "fire", "magma_block"),
    GRASS("grass", 0x528443, "grass_block", "moss", "podzol"),
    LEAVES("leaves", 0x367037, "leaves", "azalea"),
    SLAB("slab", 0x9C927E, "slab"),
    STAIR("stair", 0x8C7E68, "stairs"),
    SAND("sand", 0xC2B171, "sand", "sandstone", "terracotta"),
    SNOW("snow", 0xD9E4E6, HeadlessPhysicsBlockType.ICE, "snow"),
    EARTH("earth", 0x6F5438, "dirt", "mud", "clay", "gravel"),
    WOOD("wood", 0x825B35, "log", "planks", "wood", "stem", "hyphae"),
    STONE("stone", 0x666C72, "ore", "stone", "deepslate", "andesite", "diorite", "granite", "tuff"),
    SOLID("solid", 0x5B6570);

    private final String key;
    private final int baseRgb;
    private final HeadlessPhysicsBlockType[] physicsTypes;
    private final String[] pathTokens;

    ReplayBlockKind(String key, int baseRgb) {
        this(key, baseRgb, new HeadlessPhysicsBlockType[0], new String[0]);
    }

    ReplayBlockKind(String key, int baseRgb, HeadlessPhysicsBlockType physicsType) {
        this(key, baseRgb, new HeadlessPhysicsBlockType[] { physicsType }, new String[0]);
    }

    ReplayBlockKind(String key, int baseRgb, String... pathTokens) {
        this(key, baseRgb, new HeadlessPhysicsBlockType[0], pathTokens);
    }

    ReplayBlockKind(String key, int baseRgb, HeadlessPhysicsBlockType physicsType, String... pathTokens) {
        this(key, baseRgb, new HeadlessPhysicsBlockType[] { physicsType }, pathTokens);
    }

    ReplayBlockKind(String key, int baseRgb, HeadlessPhysicsBlockType[] physicsTypes, String[] pathTokens) {
        this.key = key;
        this.baseRgb = baseRgb;
        this.physicsTypes = Arrays.copyOf(physicsTypes, physicsTypes.length);
        this.pathTokens = Arrays.copyOf(pathTokens, pathTokens.length);
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
        for (String token : pathTokens) {
            if (id.path().contains(token)) {
                return true;
            }
        }
        return false;
    }
}
