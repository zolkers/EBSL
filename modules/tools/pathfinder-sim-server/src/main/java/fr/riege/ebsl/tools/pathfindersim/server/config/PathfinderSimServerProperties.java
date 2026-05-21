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

package fr.riege.ebsl.tools.pathfindersim.server.config;

import fr.riege.ebsl.tools.pathfindersim.replay.ReplayPaths;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.nio.file.Path;

@ConfigurationProperties(prefix = "ebsl.sim")
public class PathfinderSimServerProperties {
    private Path replayDir = ReplayPaths.defaultReplayDirectory();
    private Path worldDir = Path.of("run", "saves");

    public Path getReplayDir() {
        return replayDir;
    }

    public void setReplayDir(Path replayDir) {
        this.replayDir = replayDir;
    }

    public Path getWorldDir() {
        return worldDir;
    }

    public void setWorldDir(Path worldDir) {
        this.worldDir = worldDir;
    }
}
