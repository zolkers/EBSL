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

package fr.riege.ebsl.tools.pathfindersim.ui.render;

import fr.riege.ebsl.tools.pathfindersim.ui.Bounds;

public final class ReplayProjection {
    private ReplayProjection() {
    }

    public static double screenX(Bounds bounds, ReplayCamera camera, double x) {
        double span = Math.max(1.0, bounds.maxX() - bounds.minX());
        return camera.transformX(48.0 + (x - bounds.minX()) / span * (camera.width() - 96.0));
    }

    public static double screenY(Bounds bounds, ReplayCamera camera, double z) {
        double span = Math.max(1.0, bounds.maxZ() - bounds.minZ());
        return camera.transformY(camera.height() - 48.0 - (z - bounds.minZ()) / span * (camera.height() - 96.0));
    }

    public static double[] isoPoint(Bounds bounds, ReplayCamera camera, int terrainMinY,
                                    double x, double y, double z) {
        double centerX = (bounds.minX() + bounds.maxX()) * 0.5;
        double centerZ = (bounds.minZ() + bounds.maxZ()) * 0.5;
        double scale = isoScale(bounds, camera);
        double[] rotated = rotate(x - centerX, z - centerZ, camera.yawRadians());
        double isoX = (rotated[0] - rotated[1]) * scale + camera.width() * 0.5;
        double groundY = (rotated[0] + rotated[1]) * scale * 0.5 + camera.height() * 0.58;
        double isoY = groundY - (y - terrainMinY) * scale * 0.8;
        return new double[] { camera.transformX(isoX), camera.transformY(isoY) };
    }

    public static double isoDepth(Bounds bounds, double yawRadians, int x, int y, int z) {
        double centerX = (bounds.minX() + bounds.maxX()) * 0.5;
        double centerZ = (bounds.minZ() + bounds.maxZ()) * 0.5;
        double[] rotated = rotate(x - centerX, z - centerZ, yawRadians);
        return rotated[0] + rotated[1] + y * 0.35;
    }

    public static double[] rotate(double x, double z, double yawRadians) {
        double cos = Math.cos(yawRadians);
        double sin = Math.sin(yawRadians);
        return new double[] { x * cos - z * sin, x * sin + z * cos };
    }

    private static double isoScale(Bounds bounds, ReplayCamera camera) {
        double span = Math.max(1.0, Math.max(bounds.maxX() - bounds.minX(), bounds.maxZ() - bounds.minZ()));
        return Math.clamp(Math.min(camera.width(), camera.height()) / (span * 1.55), 5.0, 22.0);
    }
}
