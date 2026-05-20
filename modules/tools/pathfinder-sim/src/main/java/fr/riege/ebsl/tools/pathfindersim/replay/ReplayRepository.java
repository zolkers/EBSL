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

package fr.riege.ebsl.tools.pathfindersim.replay;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public final class ReplayRepository {
    private static final DateTimeFormatter FILE_TIMESTAMP = DateTimeFormatter
        .ofPattern("yyyyMMdd-HHmmss-SSS", Locale.ROOT)
        .withZone(ZoneOffset.UTC);
    private static final String INDEX_FILE = "index.json";

    private final Path directory;

    public ReplayRepository(Path directory) {
        this.directory = directory;
    }

    public SavedReplay save(List<SimulationResult> results) throws IOException {
        if (results == null || results.isEmpty()) {
            throw new IOException("Cannot save an empty replay report.");
        }
        Files.createDirectories(directory);
        Instant savedAt = Instant.now();
        SimulationResult primary = results.getFirst();
        String fileName = FILE_TIMESTAMP.format(savedAt) + "-" + slug(primary.scenarioId()) + ".json";
        Path replayFile = directory.resolve(fileName);
        Files.writeString(replayFile, SimulationReport.toJson(results));
        SavedReplay replay = new SavedReplay(
            savedAt,
            fileName,
            primary.scenarioId(),
            primary.status().name(),
            primary.reached(),
            primary.ticks(),
            results.size());
        writeIndex(loadIndexWith(replay));
        return replay;
    }

    private List<SavedReplay> loadIndexWith(SavedReplay replay) throws IOException {
        List<SavedReplay> replays = new ArrayList<>();
        replays.add(replay);
        if (Files.isDirectory(directory)) {
            try (var stream = Files.list(directory)) {
                stream
                    .filter(path -> path.getFileName().toString().endsWith(".json"))
                    .filter(path -> !INDEX_FILE.equals(path.getFileName().toString()))
                    .filter(path -> !replay.fileName().equals(path.getFileName().toString()))
                    .map(this::summarizeExistingReplay)
                    .flatMap(List::stream)
                    .forEach(replays::add);
            }
        }
        replays.sort(Comparator.comparing(SavedReplay::savedAt).reversed());
        return replays;
    }

    private List<SavedReplay> summarizeExistingReplay(Path path) {
        try {
            JsonArray results = JsonParser.parseString(Files.readString(path)).getAsJsonObject().getAsJsonArray("results");
            JsonObject first = results.get(0).getAsJsonObject();
            return List.of(new SavedReplay(
                Files.getLastModifiedTime(path).toInstant(),
                path.getFileName().toString(),
                first.get("scenarioId").getAsString(),
                first.get("status").getAsString(),
                first.get("reached").getAsBoolean(),
                first.get("ticks").getAsInt(),
                results.size()));
        } catch (IllegalStateException | IOException ignored) {
            return List.of();
        }
    }

    private void writeIndex(List<SavedReplay> replays) throws IOException {
        JsonObject root = new JsonObject();
        JsonArray replayArray = new JsonArray();
        for (SavedReplay replay : replays) {
            JsonObject replayObject = new JsonObject();
            replayObject.addProperty("savedAt", replay.savedAt().toString());
            replayObject.addProperty("file", replay.fileName());
            replayObject.addProperty("scenarioId", replay.scenarioId());
            replayObject.addProperty("status", replay.status());
            replayObject.addProperty("reached", replay.reached());
            replayObject.addProperty("ticks", replay.ticks());
            replayObject.addProperty("resultCount", replay.resultCount());
            replayArray.add(replayObject);
        }
        root.add("replays", replayArray);
        Files.writeString(directory.resolve(INDEX_FILE), root.toString());
    }

    private static String slug(String value) {
        String slug = value == null ? "replay" : value.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "-");
        String trimmed = trimDashes(slug);
        return trimmed.isBlank() ? "replay" : trimmed;
    }

    private static String trimDashes(String value) {
        int start = 0;
        int end = value.length();
        while (start < end && value.charAt(start) == '-') {
            start++;
        }
        while (end > start && value.charAt(end - 1) == '-') {
            end--;
        }
        return value.substring(start, end);
    }

    public record SavedReplay(
        Instant savedAt,
        String fileName,
        String scenarioId,
        String status,
        boolean reached,
        int ticks,
        int resultCount
    ) {
    }
}
