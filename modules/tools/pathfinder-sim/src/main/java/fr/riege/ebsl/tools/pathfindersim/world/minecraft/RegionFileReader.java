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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterInputStream;

final class RegionFileReader implements AutoCloseable {
    private static final int SECTOR_BYTES = 4096;

    private final RandomAccessFile file;

    RegionFileReader(Path path) throws IOException {
        this.file = new RandomAccessFile(path.toFile(), "r");
    }

    static Optional<RegionFileReader> open(Path regionDirectory, int regionX, int regionZ) throws IOException {
        Path file = regionDirectory.resolve("r." + regionX + "." + regionZ + ".mca");
        if (!Files.isRegularFile(file)) {
            return Optional.empty();
        }
        return Optional.of(new RegionFileReader(file));
    }

    Optional<Map<String, Object>> readChunk(int chunkX, int chunkZ) throws IOException {
        int localX = Math.floorMod(chunkX, 32);
        int localZ = Math.floorMod(chunkZ, 32);
        int index = localX + localZ * 32;
        file.seek(index * 4L);
        int location = file.readInt();
        int sectorOffset = location >>> 8;
        int sectorCount = location & 0xFF;
        if (sectorOffset == 0 || sectorCount == 0) {
            return Optional.empty();
        }
        file.seek((long) sectorOffset * SECTOR_BYTES);
        int length = file.readInt();
        int compression = file.readUnsignedByte();
        byte[] payload = new byte[Math.max(0, length - 1)];
        file.readFully(payload);
        try (InputStream stream = decompress(compression, payload)) {
            return Optional.of(NbtReader.readCompound(stream));
        }
    }

    private static InputStream decompress(int compression, byte[] payload) throws IOException {
        ByteArrayInputStream input = new ByteArrayInputStream(payload);
        return switch (compression) {
            case 1 -> new GZIPInputStream(input);
            case 2 -> new InflaterInputStream(input);
            case 3 -> input;
            default -> throw new IOException("Unsupported Anvil compression type: " + compression);
        };
    }

    @Override
    public void close() throws IOException {
        file.close();
    }
}
