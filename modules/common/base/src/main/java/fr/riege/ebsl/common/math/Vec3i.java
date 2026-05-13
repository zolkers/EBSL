/*
 * This file is part of EBSL.
 *
 * EBSL is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * EBSL is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package fr.riege.ebsl.common.math;

public record Vec3i(int x, int y, int z) {
    public Vec3d center() { return new Vec3d(x + 0.5, y + 0.5, z + 0.5); }
    public Vec3i offset(int dx, int dy, int dz) { return new Vec3i(x + dx, y + dy, z + dz); }
    public double distanceTo(Vec3i other) {
        double dx = x - (double) other.x;
        double dy = y - (double) other.y;
        double dz = z - (double) other.z;
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }
}
