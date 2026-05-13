/*
 * This file is part of EBSL.
 *
 * EBSL is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * EBSL is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package fr.riege.ebsl.common.pathfinding.util;

import fr.riege.ebsl.common.pathfinding.wrapper.PathPosition;

public final class RegionKey {
    private static final long MASK_Y = 0xFFFL;
    private static final long MASK_XZ = 0x3FFFFFFL;
    private static final int SHIFT_Z = 12;
    private static final int SHIFT_X = 38;

    private RegionKey() {}

    public static long pack(PathPosition pos) {
        return pack(pos.flooredX(), pos.flooredY(), pos.flooredZ());
    }

    public static long pack(int x, int y, int z) {
        return (x & MASK_XZ) << SHIFT_X
             | (z & MASK_XZ) << SHIFT_Z
             | (y & MASK_Y);
    }
}
