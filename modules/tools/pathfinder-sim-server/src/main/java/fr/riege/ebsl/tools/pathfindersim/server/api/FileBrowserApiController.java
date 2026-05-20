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

package fr.riege.ebsl.tools.pathfindersim.server.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@RestController
@RequestMapping("/api/files")
public class FileBrowserApiController {
    private static final String APPDATA = "APPDATA";
    private static final String USER_HOME = "user.home";

    @GetMapping("/roots")
    public List<DirectoryRoot> roots() {
        Map<Path, String> roots = new LinkedHashMap<>();
        for (Path root : FileSystems.getDefault().getRootDirectories()) {
            roots.put(normalize(root), root.toString());
        }
        addShortcut(roots, Path.of("").toAbsolutePath(), "Project");
        addShortcut(roots, Path.of("run", "saves").toAbsolutePath(), "Project saves");
        addShortcut(roots, Path.of(System.getProperty(USER_HOME)), "Home");
        String appData = System.getenv(APPDATA);
        if (appData != null && !appData.isBlank()) {
            addShortcut(roots, Path.of(appData, ".minecraft", "saves"), "Minecraft saves");
        }
        return roots.entrySet().stream()
            .map(entry -> new DirectoryRoot(entry.getValue(), entry.getKey().toString()))
            .toList();
    }

    @GetMapping("/directories")
    public DirectoryListing directories(@RequestParam String path) throws IOException {
        Path directory = normalize(Path.of(path));
        if (!Files.isDirectory(directory)) {
            throw new IOException("Directory does not exist: " + directory);
        }
        return new DirectoryListing(
            directory.toString(),
            parentPath(directory),
            children(directory));
    }

    private static void addShortcut(Map<Path, String> roots, Path path, String label) {
        Path normalized = normalize(path);
        if (Files.isDirectory(normalized)) {
            roots.put(normalized, label);
        }
    }

    private static List<DirectoryEntry> children(Path directory) throws IOException {
        try (Stream<Path> stream = Files.list(directory)) {
            return stream
                .filter(Files::isDirectory)
                .filter(FileBrowserApiController::isReadable)
                .map(FileBrowserApiController::entry)
                .sorted(Comparator.comparing(DirectoryEntry::world).reversed()
                    .thenComparing(DirectoryEntry::name, String.CASE_INSENSITIVE_ORDER))
                .toList();
        }
    }

    private static DirectoryEntry entry(Path path) {
        Path normalized = normalize(path);
        return new DirectoryEntry(fileName(normalized), normalized.toString(), isMinecraftWorld(normalized));
    }

    private static boolean isMinecraftWorld(Path directory) {
        return Files.isRegularFile(directory.resolve("level.dat")) || Files.isDirectory(directory.resolve("region"));
    }

    private static boolean isReadable(Path path) {
        try {
            return Files.isReadable(path);
        } catch (SecurityException ignored) {
            return false;
        }
    }

    private static String fileName(Path path) {
        Path fileName = path.getFileName();
        return fileName == null ? path.toString() : fileName.toString();
    }

    private static String parentPath(Path path) {
        Path parent = path.getParent();
        return parent == null ? null : normalize(parent).toString();
    }

    private static Path normalize(Path path) {
        try {
            return path.toAbsolutePath().normalize();
        } catch (InvalidPathException exception) {
            throw new IllegalArgumentException("Invalid path", exception);
        }
    }

    public record DirectoryRoot(String name, String path) {
    }

    public record DirectoryListing(String path, String parent, List<DirectoryEntry> entries) {
        public DirectoryListing {
            entries = List.copyOf(entries);
        }
    }

    public record DirectoryEntry(String name, String path, boolean world) {
    }
}
