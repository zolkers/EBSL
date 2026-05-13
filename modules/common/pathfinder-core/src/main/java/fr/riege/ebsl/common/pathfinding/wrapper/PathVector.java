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

package fr.riege.ebsl.common.pathfinding.wrapper;

public final class PathVector {
    public final double x;
    public final double y;
    public final double z;

    public PathVector(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public double dot(PathVector other) { return x*other.x + y*other.y + z*other.z; }

    public double length() {
        return Math.sqrt(x*x + y*y + z*z);
    }

    public double distance(PathVector other) {
        double dx = x-other.x;
        double dy = y-other.y;
        double dz = z-other.z;
        return Math.sqrt(dx*dx + dy*dy + dz*dz);
    }

    public PathVector setX(double x) { return new PathVector(x, y, z); }
    public PathVector setY(double y) { return new PathVector(x, y, z); }
    public PathVector setZ(double z) { return new PathVector(x, y, z); }

    public PathVector subtract(PathVector other) {
        return new PathVector(x-other.x, y-other.y, z-other.z);
    }

    public PathVector multiply(double value) {
        return new PathVector(x*value, y*value, z*value);
    }

    public PathVector normalize() {
        double mag = length();
        return new PathVector(x/mag, y/mag, z/mag);
    }

    public PathVector divide(double value) {
        return new PathVector(x/value, y/value, z/value);
    }

    public PathVector add(PathVector other) {
        return new PathVector(x+other.x, y+other.y, z+other.z);
    }

    @Override
    public String toString() {
        return "PathVector{" + x + ", " + y + ", " + z + "}";
    }
}
