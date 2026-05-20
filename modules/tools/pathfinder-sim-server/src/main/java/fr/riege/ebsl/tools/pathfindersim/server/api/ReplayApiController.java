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

import fr.riege.ebsl.common.math.Vec3d;
import fr.riege.ebsl.tools.pathfindersim.api.PathfinderSimApi;
import fr.riege.ebsl.tools.pathfindersim.api.SimGoalDescriptor;
import fr.riege.ebsl.tools.pathfindersim.api.SimRouteRequest;
import fr.riege.ebsl.tools.pathfindersim.server.config.PathfinderSimServerProperties;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ReplayApiController {
    private final PathfinderSimApi simulator = new PathfinderSimApi();
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
        return ResponseEntity.ok(simulator.replayCatalog(replayDirectory()));
    }

    @GetMapping(value = "/replays/{fileName:.+}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> replay(@PathVariable String fileName) throws IOException {
        return ResponseEntity.ok(simulator.replay(replayDirectory(), fileName));
    }

    @GetMapping("/goals")
    public List<SimGoalDescriptor> goals() {
        return simulator.goals();
    }

    @PostMapping(value = "/simulations/minecraft", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> runMinecraftRoute(@RequestBody MinecraftRouteHttpRequest request) throws IOException {
        return ResponseEntity.ok(simulator.runMinecraftRouteJson(request.toSimRequest(replayDirectory())));
    }

    private Path replayDirectory() {
        return properties.getReplayDir().toAbsolutePath().normalize();
    }

    public record MinecraftRouteHttpRequest(
        String worldDirectory,
        double startX,
        double startY,
        double startZ,
        String goalId,
        Map<String, Integer> goalValues,
        int maxTicks,
        int radiusChunks,
        int goalSearchBlocks,
        boolean saveReplay
    ) {
        SimRouteRequest toSimRequest(Path replayDirectory) {
            return new SimRouteRequest(
                Path.of(worldDirectory),
                new Vec3d(startX, startY, startZ),
                goalId,
                goalValues,
                maxTicks,
                radiusChunks,
                goalSearchBlocks,
                saveReplay,
                replayDirectory);
        }
    }
}
