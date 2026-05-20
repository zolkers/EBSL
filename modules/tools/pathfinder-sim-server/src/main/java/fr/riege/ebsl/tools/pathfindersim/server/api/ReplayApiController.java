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

import fr.riege.ebsl.tools.pathfindersim.server.config.PathfinderSimServerProperties;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ReplayApiController {
    private static final String EMPTY_CATALOG = "{\"replays\":[]}";
    private static final String INDEX_FILE = "index.json";
    private static final String JSON_EXTENSION = ".json";

    private final PathfinderSimServerProperties properties;

    public ReplayApiController(PathfinderSimServerProperties properties) {
        this.properties = properties;
    }

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "ok");
    }

    @GetMapping(value = "/replays", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> replayCatalog() throws IOException {
        Path index = replayDirectory().resolve(INDEX_FILE);
        if (!Files.isRegularFile(index)) {
            return ResponseEntity.ok(EMPTY_CATALOG);
        }
        return ResponseEntity.ok(Files.readString(index));
    }

    @GetMapping(value = "/replays/{fileName:.+}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> replay(@PathVariable String fileName) throws IOException {
        if (!isReplayFileName(fileName)) {
            return ResponseEntity.badRequest().body("{\"error\":\"invalid replay file\"}");
        }
        Path replay = replayDirectory().resolve(fileName).normalize();
        if (!replay.startsWith(replayDirectory()) || !Files.isRegularFile(replay)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(Files.readString(replay));
    }

    private Path replayDirectory() {
        return properties.getReplayDir().toAbsolutePath().normalize();
    }

    private static boolean isReplayFileName(String fileName) {
        return fileName != null
            && fileName.endsWith(JSON_EXTENSION)
            && Path.of(fileName).getFileName().toString().equals(fileName)
            && !INDEX_FILE.equals(fileName);
    }
}
