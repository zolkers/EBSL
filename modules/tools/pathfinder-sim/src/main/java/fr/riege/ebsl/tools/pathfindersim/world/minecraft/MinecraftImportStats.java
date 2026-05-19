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

package fr.riege.ebsl.tools.pathfindersim.world.minecraft;

record MinecraftImportStats(
    int chunksRequested,
    int chunksLoaded,
    int sectionsLoaded,
    int sectionsWithPalette,
    int blocksImported,
    int missingRegionChunks,
    int emptyChunks,
    int minX,
    int minY,
    int minZ,
    int maxX,
    int maxY,
    int maxZ
) {
    String summary() {
        return "chunks=" + chunksLoaded + '/' + chunksRequested
            + ", sections=" + sectionsLoaded
            + ", palettes=" + sectionsWithPalette
            + ", blocks=" + blocksImported
            + ", missing=" + missingRegionChunks
            + ", empty=" + emptyChunks
            + ", bounds=" + boundsSummary();
    }

    private String boundsSummary() {
        if (blocksImported <= 0) {
            return "empty";
        }
        return '[' + Integer.toString(minX) + ',' + minY + ',' + minZ + " -> "
            + maxX + ',' + maxY + ',' + maxZ + ']';
    }

    static final class Builder {
        private int chunksRequested;
        private int chunksLoaded;
        private int sectionsLoaded;
        private int sectionsWithPalette;
        private int blocksImported;
        private int missingRegionChunks;
        private int emptyChunks;
        private int minX = Integer.MAX_VALUE;
        private int minY = Integer.MAX_VALUE;
        private int minZ = Integer.MAX_VALUE;
        private int maxX = Integer.MIN_VALUE;
        private int maxY = Integer.MIN_VALUE;
        private int maxZ = Integer.MIN_VALUE;

        void requestedChunk() {
            chunksRequested++;
        }

        void loadedChunk() {
            chunksLoaded++;
        }

        void missingRegionChunk() {
            missingRegionChunks++;
        }

        void emptyChunk() {
            emptyChunks++;
        }

        void loadedSection() {
            sectionsLoaded++;
        }

        void sectionWithPalette() {
            sectionsWithPalette++;
        }

        void importedBlock(int x, int y, int z) {
            blocksImported++;
            minX = Math.min(minX, x);
            minY = Math.min(minY, y);
            minZ = Math.min(minZ, z);
            maxX = Math.max(maxX, x);
            maxY = Math.max(maxY, y);
            maxZ = Math.max(maxZ, z);
        }

        MinecraftImportStats build() {
            return new MinecraftImportStats(
                chunksRequested,
                chunksLoaded,
                sectionsLoaded,
                sectionsWithPalette,
                blocksImported,
                missingRegionChunks,
                emptyChunks,
                minX == Integer.MAX_VALUE ? 0 : minX,
                minY == Integer.MAX_VALUE ? 0 : minY,
                minZ == Integer.MAX_VALUE ? 0 : minZ,
                maxX == Integer.MIN_VALUE ? 0 : maxX,
                maxY == Integer.MIN_VALUE ? 0 : maxY,
                maxZ == Integer.MIN_VALUE ? 0 : maxZ);
        }
    }
}
