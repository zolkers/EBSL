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

import java.nio.file.Path;

public record MinecraftWorldImportOptions(
    Path worldDirectory,
    Vec3d start,
    int goalX,
    int goalY,
    int goalZ,
    int radiusChunks
) {
    public static final int DEFAULT_RADIUS_CHUNKS = 4;

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private Path worldDirectory;
        private Vec3d start = new Vec3d(0.5, 64.0, 0.5);
        private int goalX;
        private int goalY = 64;
        private int goalZ;
        private int radiusChunks = DEFAULT_RADIUS_CHUNKS;

        public Builder worldDirectory(Path value) {
            this.worldDirectory = value;
            return this;
        }

        public Builder start(double[] value) {
            if (value != null && value.length == 3) {
                this.start = new Vec3d(value[0], value[1], value[2]);
            }
            return this;
        }

        public Builder goal(int[] value) {
            if (value != null && value.length == 3) {
                this.goalX = value[0];
                this.goalY = value[1];
                this.goalZ = value[2];
            }
            return this;
        }

        public Builder radiusChunks(int value) {
            this.radiusChunks = Math.max(1, value);
            return this;
        }

        public MinecraftWorldImportOptions buildOrNull() {
            if (worldDirectory == null) {
                return null;
            }
            return new MinecraftWorldImportOptions(worldDirectory, start, goalX, goalY, goalZ, radiusChunks);
        }
    }
}
