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





public final class BlockPosUtil {

    private static final int X_OFFSET = 33554432;
    private static final int Z_OFFSET = 33554432;
    private static final int Y_OFFSET = 2048;

    private static final long X_MASK = 0x3FFFFFFL;
    private static final long Y_MASK = 0xFFFL;
    private static final long Z_MASK = 0x3FFFFFFL;

    private static final int Y_SHIFT = 26;
    private static final int X_SHIFT = 38;

    private BlockPosUtil() {}

    public static long pack(int x, int y, int z) {
        long lx = ((long) x + X_OFFSET) & X_MASK;
        long ly = ((long) y + Y_OFFSET) & Y_MASK;
        long lz = ((long) z + Z_OFFSET) & Z_MASK;
        return (lx << X_SHIFT) | (ly << Y_SHIFT) | lz;
    }

    public static int unpackX(long key) {
        return (int)((key >> X_SHIFT) & X_MASK) - X_OFFSET;
    }

    public static int unpackY(long key) {
        return (int)((key >> Y_SHIFT) & Y_MASK) - Y_OFFSET;
    }

    public static int unpackZ(long key) {
        return (int)(key & Z_MASK) - Z_OFFSET;
    }
}
