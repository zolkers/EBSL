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
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import fr.riege.ebsl.common.math.Vec3d;
import fr.riege.ebsl.common.navigation.NavigationStatus;
import fr.riege.ebsl.common.pathfinding.Node;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class ReplayReportReader {
    public List<SimulationResult> read(Path path) throws IOException {
        try (Reader reader = Files.newBufferedReader(path)) {
            JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
            JsonArray results = array(root, "results");
            List<SimulationResult> parsed = new ArrayList<>(results.size());
            for (JsonElement element : results) {
                parsed.add(result(element.getAsJsonObject()));
            }
            return parsed;
        } catch (JsonParseException | IllegalStateException | NumberFormatException exception) {
            throw new IOException("Invalid replay JSON: " + path.getFileName(), exception);
        }
    }

    private static SimulationResult result(JsonObject object) {
        int ticks = integer(object, "ticks", 0);
        return new SimulationResult(
            string(object, "scenarioId", "loaded-replay"),
            string(object, "description", "Loaded replay"),
            status(string(object, "status", NavigationStatus.FAILED.name())),
            bool(object, "reached", false),
            ticks,
            longValue(object, "elapsedNanos", 0L),
            integer(object, "navigationNodes", 0),
            integer(object, "rawNodes", 0),
            bool(object, "completePlan", false),
            metrics(object(object, "metrics"), ticks),
            terrain(array(object, "terrain")),
            trace(array(object, "trace")));
    }

    private static SimMetrics metrics(JsonObject object, int ticks) {
        return new SimMetrics(
            ticks,
            integer(object, "stuckTicks", 0),
            integer(object, "stuckEvents", 0),
            integer(object, "longestStuckStreak", 0),
            integer(object, "recoveryAttempts", 0),
            integer(object, "backwardTicks", 0),
            decimal(object, "averageLateralError", 0.0),
            decimal(object, "maxLateralError", 0.0),
            decimal(object, "averageSpeedAlongPath", 0.0),
            decimal(object, "maxSpeedAcrossPath", 0.0),
            decimal(object, "bestDistance", 0.0),
            decimal(object, "finalDistance", 0.0));
    }

    private static List<ReplayBlock> terrain(JsonArray terrain) {
        List<ReplayBlock> blocks = new ArrayList<>(terrain.size());
        for (JsonElement element : terrain) {
            JsonObject block = element.getAsJsonObject();
            blocks.add(new ReplayBlock(
                integer(block, "x", 0),
                integer(block, "y", 0),
                integer(block, "z", 0),
                ReplayBlockKind.fromKey(string(block, "kind", ReplayBlockKind.SOLID.key()))));
        }
        return blocks;
    }

    private static List<SimulationTick> trace(JsonArray trace) {
        List<SimulationTick> ticks = new ArrayList<>(trace.size());
        for (JsonElement element : trace) {
            JsonObject tick = element.getAsJsonObject();
            ticks.add(new SimulationTick(
                integer(tick, "tick", ticks.size()),
                vector(array(tick, "position")),
                vector(array(tick, "velocity")),
                status(string(tick, "status", NavigationStatus.FAILED.name())),
                moveType(string(tick, "moveType", Node.MoveType.WALK.name())),
                decimal(tick, "distanceToGoal", 0.0),
                bool(tick, "stuck", false),
                bool(tick, "jump", false),
                bool(tick, "sprint", false),
                bool(tick, "sneak", false),
                pathTelemetry(object(tick, "pathTelemetry"))));
        }
        return ticks;
    }

    private static SimulationPathTelemetry pathTelemetry(JsonObject object) {
        return new SimulationPathTelemetry(
            integer(object, "nearestSegment", 0),
            decimal(object, "segmentProgress", 0.0),
            decimal(object, "lateralError", 0.0),
            decimal(object, "verticalError", 0.0),
            decimal(object, "speedAlongPath", 0.0),
            decimal(object, "speedAcrossPath", 0.0));
    }

    private static Vec3d vector(JsonArray values) {
        return new Vec3d(
            decimal(values, 0, 0.0),
            decimal(values, 1, 0.0),
            decimal(values, 2, 0.0));
    }

    private static JsonObject object(JsonObject object, String key) {
        JsonElement element = object.get(key);
        return element == null || !element.isJsonObject() ? new JsonObject() : element.getAsJsonObject();
    }

    private static JsonArray array(JsonObject object, String key) {
        JsonElement element = object.get(key);
        return element == null || !element.isJsonArray() ? new JsonArray() : element.getAsJsonArray();
    }

    private static String string(JsonObject object, String key, String fallback) {
        JsonElement element = object.get(key);
        return element == null || element.isJsonNull() ? fallback : element.getAsString();
    }

    private static boolean bool(JsonObject object, String key, boolean fallback) {
        JsonElement element = object.get(key);
        return element == null || element.isJsonNull() ? fallback : element.getAsBoolean();
    }

    private static int integer(JsonObject object, String key, int fallback) {
        JsonElement element = object.get(key);
        return element == null || element.isJsonNull() ? fallback : element.getAsInt();
    }

    private static long longValue(JsonObject object, String key, long fallback) {
        JsonElement element = object.get(key);
        return element == null || element.isJsonNull() ? fallback : element.getAsLong();
    }

    private static double decimal(JsonObject object, String key, double fallback) {
        JsonElement element = object.get(key);
        return element == null || element.isJsonNull() ? fallback : element.getAsDouble();
    }

    private static double decimal(JsonArray values, int index, double fallback) {
        return index >= values.size() || values.get(index).isJsonNull() ? fallback : values.get(index).getAsDouble();
    }

    private static NavigationStatus status(String value) {
        try {
            return NavigationStatus.valueOf(value);
        } catch (IllegalArgumentException exception) {
            return NavigationStatus.FAILED;
        }
    }

    private static Node.MoveType moveType(String value) {
        try {
            return Node.MoveType.valueOf(value);
        } catch (IllegalArgumentException exception) {
            return Node.MoveType.WALK;
        }
    }
}
