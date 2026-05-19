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

import fr.riege.ebsl.common.math.Vec3d;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import java.util.zip.GZIPInputStream;

final class MinecraftWorldAnchors {
    private MinecraftWorldAnchors() {
    }

    static List<Vec3d> load(Path worldDirectory) throws IOException {
        List<Vec3d> anchors = new ArrayList<>();
        anchors.addAll(playerPositions(worldDirectory.resolve("playerdata")));
        levelSpawn(worldDirectory.resolve("level.dat")).ifPresent(anchors::add);
        return List.copyOf(anchors);
    }

    private static List<Vec3d> playerPositions(Path playerDataDirectory) throws IOException {
        if (!Files.isDirectory(playerDataDirectory)) {
            return List.of();
        }
        try (Stream<Path> files = Files.list(playerDataDirectory)) {
            List<Path> playerFiles = files
                .filter(path -> path.getFileName().toString().endsWith(".dat"))
                .sorted(Comparator.comparingLong(MinecraftWorldAnchors::lastModified).reversed())
                .toList();
            List<Vec3d> positions = new ArrayList<>();
            for (Path playerFile : playerFiles) {
                readGzippedCompound(playerFile).flatMap(MinecraftWorldAnchors::position).ifPresent(positions::add);
            }
            return List.copyOf(positions);
        }
    }

    @SuppressWarnings("unchecked")
    private static Optional<Vec3d> levelSpawn(Path levelDat) throws IOException {
        Optional<Map<String, Object>> root = readGzippedCompound(levelDat);
        if (root.isEmpty()) {
            return Optional.empty();
        }
        Object data = root.get().get("Data");
        Map<String, Object> values = data instanceof Map<?, ?> map ? (Map<String, Object>) map : root.get();
        Number x = number(values.get("SpawnX"));
        Number y = number(values.get("SpawnY"));
        Number z = number(values.get("SpawnZ"));
        if (x == null || y == null || z == null) {
            return Optional.empty();
        }
        return Optional.of(new Vec3d(x.doubleValue() + 0.5, y.doubleValue(), z.doubleValue() + 0.5));
    }

    private static Optional<Map<String, Object>> readGzippedCompound(Path file) throws IOException {
        if (!Files.isRegularFile(file)) {
            return Optional.empty();
        }
        try (InputStream input = new GZIPInputStream(Files.newInputStream(file))) {
            return Optional.of(NbtReader.readCompound(input));
        }
    }

    private static Optional<Vec3d> position(Map<String, Object> values) {
        Object pos = values.get("Pos");
        if (!(pos instanceof List<?> list) || list.size() < 3) {
            return Optional.empty();
        }
        Number x = number(list.get(0));
        Number y = number(list.get(1));
        Number z = number(list.get(2));
        if (x == null || y == null || z == null) {
            return Optional.empty();
        }
        return Optional.of(new Vec3d(x.doubleValue(), y.doubleValue(), z.doubleValue()));
    }

    private static Number number(Object value) {
        return value instanceof Number number ? number : null;
    }

    private static long lastModified(Path path) {
        try {
            return Files.getLastModifiedTime(path).toMillis();
        } catch (IOException ignored) {
            return 0L;
        }
    }
}
