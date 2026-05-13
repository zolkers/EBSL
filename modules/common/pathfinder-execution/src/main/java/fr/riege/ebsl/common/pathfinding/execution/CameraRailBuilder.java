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

package fr.riege.ebsl.common.pathfinding.execution;

import fr.riege.ebsl.common.math.Vec3d;
import fr.riege.ebsl.common.pathfinding.Node;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

final class CameraRailBuilder {
    private static final double CAMERA_RAIL_EYE_Y = 1.52;
    private static final double CAMERA_RAIL_MAX_STEP_DIST = 3.0;
    private static final double CAMERA_RAIL_DENSITY_SCALE = 0.1;
    private static final int CAMERA_RAIL_MAX_STEPS_PER_SEGMENT = 18;
    private static final double CAMERA_RAIL_MIN_POINT_SPACING = 0.44;
    private static final double CAMERA_RAIL_MIN_HORIZONTAL_SPACING = 0.16;
    private static final float CAMERA_RAIL_MAX_TURN_DEG = 14.0f;

    private CameraRailBuilder() {
    }

    static List<Vec3d> build(List<Node> navPath) {
        if (navPath == null || navPath.isEmpty()) {
            return Collections.emptyList();
        }

        List<Vec3d> anchors = new ArrayList<>(navPath.size());
        for (Node n : navPath) {
            anchors.add(new Vec3d(
                n.position.flooredX() + 0.5,
                n.position.flooredY() + CAMERA_RAIL_EYE_Y,
                n.position.flooredZ() + 0.5));
        }
        if (anchors.size() <= 1) {
            return anchors;
        }

        List<Vec3d> out = new ArrayList<>(anchors.size() * 2);
        appendPoint(out, anchors.getFirst());

        for (int i = 0; i < anchors.size() - 1; i++) {
            Vec3d a = anchors.get(i);
            Vec3d b = anchors.get(i + 1);

            double segDist = a.distanceTo(b);
            int byDistance = (int) Math.ceil(segDist / CAMERA_RAIL_MAX_STEP_DIST);

            int byTurn = 1;
            if (i > 0 && i + 2 < anchors.size()) {
                Vec3d prev = anchors.get(i - 1);
                Vec3d next = anchors.get(i + 2);
                double cornerAngle = horizontalAngleDeg(prev, a, b, next);
                byTurn = Math.max(1, (int) Math.ceil(cornerAngle / CAMERA_RAIL_MAX_TURN_DEG));
            }

            int rawSteps = Math.max(byDistance, byTurn);
            int steps = (int) Math.ceil(rawSteps * CAMERA_RAIL_DENSITY_SCALE);
            steps = Math.clamp(steps, 1, CAMERA_RAIL_MAX_STEPS_PER_SEGMENT);
            for (int s = 1; s <= steps; s++) {
                double t = (double) s / steps;
                appendPoint(out, new Vec3d(
                    a.x() + (b.x() - a.x()) * t,
                    a.y() + (b.y() - a.y()) * t,
                    a.z() + (b.z() - a.z()) * t));
            }
        }

        return compact(out);
    }

    private static void appendPoint(List<Vec3d> out, Vec3d p) {
        if (out.isEmpty()) {
            out.add(p);
            return;
        }
        Vec3d last = out.getLast();
        double dx = p.x() - last.x();
        double dy = p.y() - last.y();
        double dz = p.z() - last.z();
        double distSq = dx * dx + dy * dy + dz * dz;
        if (distSq < CAMERA_RAIL_MIN_POINT_SPACING * CAMERA_RAIL_MIN_POINT_SPACING) {
            return;
        }

        double horizSq = dx * dx + dz * dz;
        if (horizSq < CAMERA_RAIL_MIN_HORIZONTAL_SPACING * CAMERA_RAIL_MIN_HORIZONTAL_SPACING) {
            return;
        }

        out.add(p);
    }

    private static List<Vec3d> compact(List<Vec3d> points) {
        if (points.size() <= 1) {
            return points;
        }
        List<Vec3d> compact = new ArrayList<>(points.size());
        for (Vec3d p : points) {
            appendPoint(compact, p);
        }
        if (compact.isEmpty()) {
            compact.add(points.getFirst());
        }
        return compact;
    }

    private static double horizontalAngleDeg(Vec3d prev, Vec3d a, Vec3d b, Vec3d next) {
        double inX = a.x() - prev.x();
        double inZ = a.z() - prev.z();
        double outX = next.x() - b.x();
        double outZ = next.z() - b.z();

        double inLen = Math.sqrt(inX * inX + inZ * inZ);
        double outLen = Math.sqrt(outX * outX + outZ * outZ);
        if (inLen < 1.0e-3 || outLen < 1.0e-3) {
            return 0.0;
        }

        double dot = (inX / inLen) * (outX / outLen) + (inZ / inLen) * (outZ / outLen);
        return Math.toDegrees(Math.acos(Math.clamp(dot, -1.0, 1.0)));
    }
}
