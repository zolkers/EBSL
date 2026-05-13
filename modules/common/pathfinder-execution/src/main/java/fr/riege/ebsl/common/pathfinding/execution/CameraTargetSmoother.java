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

package fr.riege.ebsl.common.pathfinding.execution;

import fr.riege.ebsl.common.math.Vec3d;
import fr.riege.ebsl.common.pathfinding.settings.PathfinderSettings;

final class CameraTargetSmoother {
    private boolean initialized;
    private double y;
    private double velocityY;

    void reset(double initialY) {
        initialized = true;
        y = initialY;
        velocityY = 0.0;
    }

    void clear() {
        initialized = false;
        y = 0.0;
        velocityY = 0.0;
    }

    Vec3d smooth(Vec3d target, Vec3d eye) {
        if (target == null) {
            return null;
        }
        if (!initialized) {
            reset(target.y());
            return target;
        }

        PathfinderSettings settings = PathfinderSettings.instance();
        double deadband = settings.cameraHeightDeadband.value();
        double stiffness = settings.cameraHeightStiffness.value();
        double damping = settings.cameraHeightDamping.value();
        double maxStep = settings.cameraHeightMaxStep.value();
        double minY = eye.y() - settings.cameraHeightDownLimit.value();
        double maxY = eye.y() + settings.cameraHeightUpLimit.value();

        double desiredY = Math.clamp(target.y(), minY, maxY);
        double error = desiredY - y;
        if (Math.abs(error) <= deadband) {
            velocityY *= damping * 0.5;
        } else {
            velocityY = velocityY * damping + (Math.abs(error) - deadband) * Math.signum(error) * stiffness;
        }

        velocityY = Math.clamp(velocityY, -maxStep, maxStep);
        y = Math.clamp(y + velocityY, minY, maxY);
        return new Vec3d(target.x(), y, target.z());
    }

    double y() {
        return y;
    }
}
