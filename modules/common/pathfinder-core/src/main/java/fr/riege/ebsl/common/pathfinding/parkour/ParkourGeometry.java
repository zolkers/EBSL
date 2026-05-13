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
package fr.riege.ebsl.common.pathfinding.parkour;

public final class ParkourGeometry {
    private static final int MIN_CARDINAL_GAP = 1;
    private static final int MAX_CARDINAL_GAP = 3;
    private static final int MIN_DIAGONAL_GAP = 1;
    private static final int MAX_DIAGONAL_GAP = 2;

    private ParkourGeometry() {
    }

    public static boolean isCandidateOffset(int dx, int dz) {
        int absDx = Math.abs(dx);
        int absDz = Math.abs(dz);
        if (absDx == 0 && absDz == 0) {
            return false;
        }
        if (absDx == 0 || absDz == 0) {
            int gapBlocks = cardinalGapBlocks(dx, dz);
            return gapBlocks >= MIN_CARDINAL_GAP && gapBlocks <= MAX_CARDINAL_GAP;
        }
        int gapBlocks = diagonalGapBlocks(dx, dz);
        return gapBlocks >= MIN_DIAGONAL_GAP && gapBlocks <= MAX_DIAGONAL_GAP;
    }

    public static int distanceBlocks(int dx, int dz) {
        return Math.max(Math.abs(dx), Math.abs(dz));
    }

    public static int cardinalGapBlocks(int dx, int dz) {
        return Math.abs(dx) + Math.abs(dz) - 1;
    }

    public static int diagonalGapBlocks(int dx, int dz) {
        return Math.max(Math.abs(dx), Math.abs(dz)) - 1;
    }

    public static int gapBlocks(int dx, int dz) {
        return isDiagonal(dx, dz) ? diagonalGapBlocks(dx, dz) : cardinalGapBlocks(dx, dz);
    }

    public static boolean isDiagonal(int dx, int dz) {
        return dx != 0 && dz != 0;
    }
}
