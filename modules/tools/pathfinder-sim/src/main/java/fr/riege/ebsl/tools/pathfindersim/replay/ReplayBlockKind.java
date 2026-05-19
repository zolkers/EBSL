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
    WATER("water", HeadlessPhysicsBlockType.WATER),
    CLIMBABLE("climbable", HeadlessPhysicsBlockType.CLIMBABLE),
    DANGER("danger", HeadlessPhysicsBlockType.LAVA, "fire", "magma_block"),
    GRASS("grass", "grass_block", "moss", "podzol"),
    LEAVES("leaves", "leaves", "azalea"),
    SAND("sand", "sand", "sandstone", "terracotta"),
    SNOW("snow", HeadlessPhysicsBlockType.ICE, "snow"),
    EARTH("earth", "dirt", "mud", "clay", "gravel"),
    WOOD("wood", "log", "planks", "wood", "stem", "hyphae"),
    STONE("stone", "ore", "stone", "deepslate", "andesite", "diorite", "granite", "tuff"),
    SOLID("solid");

    private final String key;
    private final HeadlessPhysicsBlockType[] physicsTypes;
    private final String[] pathTokens;

    ReplayBlockKind(String key) {
        this(key, new HeadlessPhysicsBlockType[0], new String[0]);
    }

    ReplayBlockKind(String key, HeadlessPhysicsBlockType physicsType) {
        this(key, new HeadlessPhysicsBlockType[] { physicsType }, new String[0]);
    }

    ReplayBlockKind(String key, String... pathTokens) {
        this(key, new HeadlessPhysicsBlockType[0], pathTokens);
    }

    ReplayBlockKind(String key, HeadlessPhysicsBlockType physicsType, String... pathTokens) {
        this(key, new HeadlessPhysicsBlockType[] { physicsType }, pathTokens);
    }

    ReplayBlockKind(String key, HeadlessPhysicsBlockType[] physicsTypes, String[] pathTokens) {
        this.key = key;
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
