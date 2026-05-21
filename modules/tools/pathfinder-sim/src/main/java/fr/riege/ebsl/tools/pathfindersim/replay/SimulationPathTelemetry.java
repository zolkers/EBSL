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

import fr.riege.ebsl.common.math.Vec3d;
import fr.riege.ebsl.common.pathfinding.Node;

import java.util.List;

public record SimulationPathTelemetry(
    int nearestSegment,
    double segmentProgress,
    double lateralError,
    double verticalError,
    double speedAlongPath,
    double speedAcrossPath
) {
    public static SimulationPathTelemetry capture(Vec3d position, Vec3d velocity, List<Node> path) {
        if (path == null || path.size() < 2) {
            return new SimulationPathTelemetry(0, 0.0, 0.0, 0.0, 0.0, 0.0);
        }

        SegmentProjection projection = nearestSegment(position, path);
        Node from = path.get(projection.segment());
        Node to = path.get(projection.segment() + 1);
        double dx = to.position.centeredX() - from.position.centeredX();
        double dz = to.position.centeredZ() - from.position.centeredZ();
        double horizontalLength = Math.sqrt(dx * dx + dz * dz);
        if (horizontalLength <= 1.0e-6) {
            return new SimulationPathTelemetry(
                projection.segment(),
                projection.progress(),
                projection.horizontalDistance(),
                projection.verticalDistance(),
                0.0,
                0.0);
        }

        double dirX = dx / horizontalLength;
        double dirZ = dz / horizontalLength;
        double speedAlong = velocity.x() * dirX + velocity.z() * dirZ;
        double speedAcross = velocity.x() * -dirZ + velocity.z() * dirX;
        return new SimulationPathTelemetry(
            projection.segment(),
            projection.progress(),
            projection.horizontalDistance(),
            projection.verticalDistance(),
            speedAlong,
            speedAcross);
    }

    private static SegmentProjection nearestSegment(Vec3d position, List<Node> path) {
        int bestSegment = 0;
        double bestT = 0.0;
        double bestHorizontalSq = Double.POSITIVE_INFINITY;
        double bestVertical = 0.0;
        for (int i = 0; i < path.size() - 1; i++) {
            Node from = path.get(i);
            Node to = path.get(i + 1);
            double ax = from.position.centeredX();
            double ay = from.position.flooredY();
            double az = from.position.centeredZ();
            double dx = to.position.centeredX() - ax;
            double dy = (double) to.position.flooredY() - from.position.flooredY();
            double dz = to.position.centeredZ() - az;
            double lenSq = dx * dx + dy * dy + dz * dz;
            double t = lenSq <= 1.0e-6
                ? 0.0
                : Math.clamp(((position.x() - ax) * dx + (position.y() - ay) * dy + (position.z() - az) * dz) / lenSq,
                    0.0,
                    1.0);
            double projectedX = ax + dx * t;
            double projectedY = ay + dy * t;
            double projectedZ = az + dz * t;
            double offX = position.x() - projectedX;
            double offY = position.y() - projectedY;
            double offZ = position.z() - projectedZ;
            double horizontalSq = offX * offX + offZ * offZ;
            if (horizontalSq < bestHorizontalSq) {
                bestHorizontalSq = horizontalSq;
                bestVertical = Math.abs(offY);
                bestSegment = i;
                bestT = t;
            }
        }
        return new SegmentProjection(bestSegment, bestT, Math.sqrt(bestHorizontalSq), bestVertical);
    }

    private record SegmentProjection(int segment, double progress, double horizontalDistance, double verticalDistance) {
    }
}
