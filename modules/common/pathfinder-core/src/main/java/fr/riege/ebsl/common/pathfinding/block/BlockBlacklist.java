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

package fr.riege.ebsl.common.pathfinding.block;

import fr.riege.ebsl.common.domain.world.BlockId;

import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

public final class BlockBlacklist {
    private static final AtomicReference<Set<BlockId>> IDS = new AtomicReference<>(Set.of());
    private static volatile boolean enabled;

    private BlockBlacklist() {
    }

    public static void update(boolean enabled, Set<BlockId> ids) {
        BlockBlacklist.enabled = enabled;
        IDS.set(ids == null ? Set.of() : Set.copyOf(ids));
    }

    public static boolean isBlacklisted(BlockId id) {
        return enabled && id != null && IDS.get().contains(id);
    }
}
