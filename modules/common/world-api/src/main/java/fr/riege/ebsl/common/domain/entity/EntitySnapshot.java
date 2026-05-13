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

package fr.riege.ebsl.common.domain.entity;

import fr.riege.ebsl.common.math.Vec3d;

public record EntitySnapshot(
    int id,
    String typeId,
    String displayName,
    String name,
    Vec3d position,
    Vec3d eyePosition,
    double minX,
    double minY,
    double minZ,
    double maxX,
    double maxY,
    double maxZ,
    boolean living,
    boolean mob,
    boolean alive,
    boolean removed,
    float health
) {
    public double distanceToSq(Vec3d other) {
        double dx = position.x() - other.x();
        double dy = position.y() - other.y();
        double dz = position.z() - other.z();
        return dx * dx + dy * dy + dz * dz;
    }

    public double bbHeight() {
        return Math.max(0.0, maxY - minY);
    }
}
