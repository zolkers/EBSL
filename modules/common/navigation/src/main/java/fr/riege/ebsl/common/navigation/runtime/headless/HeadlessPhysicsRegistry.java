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

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Predicate;

public final class HeadlessPhysicsRegistry {
    private final Map<BlockId, HeadlessPhysicsProfile> exact = new LinkedHashMap<>();
    private final Map<Predicate<BlockId>, HeadlessPhysicsProfile> predicates = new LinkedHashMap<>();
    private HeadlessPhysicsProfile fallback = HeadlessPhysicsProfile.DEFAULT;

    public static HeadlessPhysicsRegistry vanillaLike() {
        HeadlessPhysicsRegistry registry = new HeadlessPhysicsRegistry();
        registry.registerContains("ice", HeadlessPhysicsProfile.ICE);
        registry.registerContains("packed_ice", HeadlessPhysicsProfile.ICE);
        registry.registerContains("blue_ice", HeadlessPhysicsProfile.ICE);
        registry.registerContains("soul_sand", HeadlessPhysicsProfile.SOUL_SAND);
        registry.registerContains("honey_block", HeadlessPhysicsProfile.HONEY);
        registry.registerContains("water", HeadlessPhysicsProfile.WATER);
        registry.registerContains("lava", HeadlessPhysicsProfile.LAVA);
        registry.registerContains("ladder", HeadlessPhysicsProfile.CLIMBABLE);
        registry.registerContains("vine", HeadlessPhysicsProfile.CLIMBABLE);
        return registry;
    }

    public static HeadlessPhysicsRegistry vanillaLikeDefault() {
        return vanillaLike();
    }

    public HeadlessPhysicsRegistry fallback(HeadlessPhysicsProfile profile) {
        fallback = profile == null ? HeadlessPhysicsProfile.DEFAULT : profile;
        return this;
    }

    public HeadlessPhysicsRegistry register(BlockId id, HeadlessPhysicsProfile profile) {
        if (id != null && profile != null) {
            exact.put(id, profile);
        }
        return this;
    }

    public HeadlessPhysicsRegistry register(Predicate<BlockId> predicate, HeadlessPhysicsProfile profile) {
        if (predicate != null && profile != null) {
            predicates.put(predicate, profile);
        }
        return this;
    }

    public HeadlessPhysicsRegistry registerContains(String token, HeadlessPhysicsProfile profile) {
        String query = token == null ? "" : token;
        return register(id -> id != null && id.toString().contains(query), profile);
    }

    public HeadlessPhysicsProfile profile(HeadlessBlockState state) {
        if (state == null) {
            return fallback;
        }
        HeadlessPhysicsProfile direct = exact.get(state.id());
        if (direct != null) {
            return direct;
        }
        for (Map.Entry<Predicate<BlockId>, HeadlessPhysicsProfile> entry : predicates.entrySet()) {
            if (entry.getKey().test(state.id())) {
                return entry.getValue();
            }
        }
        if (state.water()) {
            return HeadlessPhysicsProfile.WATER;
        }
        if (state.lava()) {
            return HeadlessPhysicsProfile.LAVA;
        }
        if (state.climbable()) {
            return HeadlessPhysicsProfile.CLIMBABLE;
        }
        return fallback;
    }
}
