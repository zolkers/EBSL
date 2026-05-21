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

import java.util.Arrays;
import java.util.function.Predicate;

public enum HeadlessPhysicsBlockType {
    ICE(HeadlessPhysicsProfile.ICE, "ice", "packed_ice", "blue_ice", "frosted_ice"),
    SOUL_SAND(HeadlessPhysicsProfile.SOUL_SAND, "soul_sand"),
    HONEY(HeadlessPhysicsProfile.HONEY, "honey_block"),
    POWDER_SNOW(HeadlessPhysicsProfile.POWDER_SNOW, "powder_snow"),
    COBWEB(HeadlessPhysicsProfile.COBWEB, "cobweb"),
    WATER(HeadlessPhysicsProfile.WATER, "water"),
    LAVA(HeadlessPhysicsProfile.LAVA, "lava"),
    CLIMBABLE(HeadlessPhysicsProfile.CLIMBABLE, "ladder", "vine", "weeping_vines", "twisting_vines");

    private final HeadlessPhysicsProfile profile;
    private final String[] paths;

    HeadlessPhysicsBlockType(HeadlessPhysicsProfile profile, String... paths) {
        this.profile = profile;
        this.paths = Arrays.copyOf(paths, paths.length);
    }

    public HeadlessPhysicsProfile profile() {
        return profile;
    }

    public Predicate<BlockId> matcher() {
        return this::matches;
    }

    public boolean matches(BlockId id) {
        if (id == null) {
            return false;
        }
        for (String path : paths) {
            if (id.path().equals(path)) {
                return true;
            }
        }
        return false;
    }
}
