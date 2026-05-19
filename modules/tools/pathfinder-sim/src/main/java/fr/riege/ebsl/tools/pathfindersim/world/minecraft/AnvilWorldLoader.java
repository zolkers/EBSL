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

import fr.riege.ebsl.common.navigation.runtime.headless.HeadlessBlockState;
import fr.riege.ebsl.common.navigation.runtime.headless.HeadlessWorldLayer;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class AnvilWorldLoader {
    ImportedMinecraftWorld load(MinecraftWorldImportOptions options) throws IOException {
        HeadlessWorldLayer world = new HeadlessWorldLayer().heightRange(-64, 384);
        MinecraftImportStats.Builder stats = new MinecraftImportStats.Builder();
        Path regionDirectory = options.worldDirectory().resolve("region");
        int startChunkX = blockToChunk((int) Math.floor(options.start().x()));
        int startChunkZ = blockToChunk((int) Math.floor(options.start().z()));
        int goalChunkX = options.goalExplicit() ? blockToChunk(options.goalX()) : startChunkX;
        int goalChunkZ = options.goalExplicit() ? blockToChunk(options.goalZ()) : startChunkZ;
        int minChunkX = Math.min(startChunkX, goalChunkX) - options.radiusChunks();
        int maxChunkX = Math.max(startChunkX, goalChunkX) + options.radiusChunks();
        int minChunkZ = Math.min(startChunkZ, goalChunkZ) - options.radiusChunks();
        int maxChunkZ = Math.max(startChunkZ, goalChunkZ) + options.radiusChunks();

        for (int chunkX = minChunkX; chunkX <= maxChunkX; chunkX++) {
            for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; chunkZ++) {
                stats.requestedChunk();
                loadChunk(regionDirectory, chunkX, chunkZ, world, stats);
            }
        }
        return new ImportedMinecraftWorld(world, stats.build());
    }

    private static void loadChunk(Path regionDirectory,
                                  int chunkX,
                                  int chunkZ,
                                  HeadlessWorldLayer world,
                                  MinecraftImportStats.Builder stats) throws IOException {
        int regionX = Math.floorDiv(chunkX, 32);
        int regionZ = Math.floorDiv(chunkZ, 32);
        Optional<RegionFileReader> reader = RegionFileReader.open(regionDirectory, regionX, regionZ);
        if (reader.isEmpty()) {
            stats.missingRegionChunk();
            return;
        }
        try (RegionFileReader region = reader.get()) {
            Optional<Map<String, Object>> chunk = region.readChunk(chunkX, chunkZ);
            if (chunk.isPresent()) {
                stats.loadedChunk();
                loadChunkSections(chunk.get(), chunkX, chunkZ, world, stats);
            } else {
                stats.emptyChunk();
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static void loadChunkSections(Map<String, Object> chunk,
                                          int chunkX,
                                          int chunkZ,
                                          HeadlessWorldLayer world,
                                          MinecraftImportStats.Builder stats) {
        Object sectionsValue = chunk.get("sections");
        if (!(sectionsValue instanceof List<?> sections)) {
            Object level = chunk.get("Level");
            if (level instanceof Map<?, ?> levelMap) {
                sectionsValue = ((Map<String, Object>) levelMap).get("Sections");
            }
        }
        if (!(sectionsValue instanceof List<?> sections)) {
            return;
        }
        for (Object sectionValue : sections) {
            if (sectionValue instanceof Map<?, ?> section) {
                stats.loadedSection();
                loadSection((Map<String, Object>) section, chunkX, chunkZ, world, stats);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static void loadSection(Map<String, Object> section,
                                    int chunkX,
                                    int chunkZ,
                                    HeadlessWorldLayer world,
                                    MinecraftImportStats.Builder stats) {
        Object blockStatesValue = section.get("block_states");
        if (!(blockStatesValue instanceof Map<?, ?> blockStates)) {
            blockStatesValue = section.get("BlockStates");
        }
        if (!(blockStatesValue instanceof Map<?, ?> blockStates)) {
            return;
        }
        Object paletteValue = ((Map<String, Object>) blockStates).get("palette");
        Object dataValue = ((Map<String, Object>) blockStates).get("data");
        if (!(paletteValue instanceof List<?> palette) || palette.isEmpty()) {
            return;
        }
        stats.sectionWithPalette();
        int sectionY = number(section.getOrDefault("Y", section.get("y"))).intValue();
        long[] data = dataValue instanceof long[] longs ? longs : new long[0];
        List<String> names = palette.stream()
            .map(AnvilWorldLoader::paletteName)
            .toList();
        int bits = Math.max(4, 32 - Integer.numberOfLeadingZeros(names.size() - 1));
        int valuesPerLong = Math.max(1, 64 / bits);
        int baseX = chunkX * 16;
        int baseZ = chunkZ * 16;
        int baseY = sectionY * 16;
        for (int index = 0; index < 4096; index++) {
            int paletteIndex = paletteIndex(index, bits, valuesPerLong, data);
            if (paletteIndex < 0 || paletteIndex >= names.size()) {
                continue;
            }
            HeadlessBlockState state = MinecraftBlockMapper.map(names.get(paletteIndex));
            if (state.isAir()) {
                continue;
            }
            int x = index & 15;
            int z = (index >> 4) & 15;
            int y = (index >> 8) & 15;
            int worldX = baseX + x;
            int worldY = baseY + y;
            int worldZ = baseZ + z;
            world.set(worldX, worldY, worldZ, state);
            stats.importedBlock(worldX, worldY, worldZ);
        }
    }

    @SuppressWarnings("unchecked")
    private static String paletteName(Object value) {
        if (value instanceof Map<?, ?> map) {
            Object name = ((Map<String, Object>) map).get("Name");
            if (name == null) {
                name = ((Map<String, Object>) map).get("name");
            }
            return String.valueOf(name);
        }
        return "minecraft:air";
    }

    private static int paletteIndex(int index, int bits, int valuesPerLong, long[] data) {
        if (data.length == 0) {
            return 0;
        }
        int longIndex = index / valuesPerLong;
        if (longIndex >= data.length) {
            return -1;
        }
        int bitOffset = (index % valuesPerLong) * bits;
        long mask = (1L << bits) - 1L;
        return (int) ((data[longIndex] >>> bitOffset) & mask);
    }

    private static Number number(Object value) {
        return value instanceof Number number ? number : 0;
    }

    private static int blockToChunk(int block) {
        return Math.floorDiv(block, 16);
    }
}
