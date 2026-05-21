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

import fr.riege.ebsl.common.platform.layer.IInputLayer;
import fr.riege.ebsl.common.world.layer.IPlayerLayer;

final class InputApplier {
    private static final double STRAFE_SUPPRESSION_FORWARD_DOT = 0.45;
    private static final double STRAFE_SUPPRESSION_DOT = 0.55;
    private static final double DOT_SMOOTHING = 0.42;
    private static final double RELEASE_MARGIN = 0.10;

    private InputApplier() {
    }

    static void releaseAll(IInputLayer input, boolean keepSneaking) {
        input.releaseMovementKeys();
        input.setSneakDown(keepSneaking);
    }

    static void applyRelativeMovement(IPlayerLayer player, IInputLayer input, double dx, double dz,
                                      double forwardThreshold,
                                      double backwardThreshold,
                                      double strafeThreshold) {
        applyRelativeMovement(player, input, null, dx, dz,
            new MovementThresholds(forwardThreshold, backwardThreshold, strafeThreshold));
    }

    static MovementAxes applyStableRelativeMovement(IPlayerLayer player, IInputLayer input, MovementMemory memory,
                                                    MovementIntent intent) {
        return applyRelativeMovement(player, input, memory, intent.dx(), intent.dz(), intent.thresholds());
    }

    private static MovementAxes applyRelativeMovement(IPlayerLayer player, IInputLayer input, MovementMemory memory,
                                                      double dx, double dz,
                                                      MovementThresholds thresholds) {
        float yawRad = (float) Math.toRadians(player.yaw());
        double forwardX = -Math.sin(yawRad);
        double forwardZ = Math.cos(yawRad);
        double rightX = -Math.sin(yawRad + Math.PI / 2.0);
        double rightZ = Math.cos(yawRad + Math.PI / 2.0);

        double forwardDot = dx * forwardX + dz * forwardZ;
        double strafeDot = dx * rightX + dz * rightZ;
        boolean lateralCorrection = thresholds.lateralCorrection();
        if (memory != null) {
            forwardDot = memory.smoothForward(forwardDot);
            strafeDot = memory.smoothStrafe(strafeDot);
        }

        boolean forward = shouldHoldPositive(memory == null ? input.forwardDown() : memory.forward,
            forwardDot, thresholds.forward());
        boolean backward = shouldHoldNegative(memory == null ? input.backwardDown() : memory.backward,
            forwardDot, thresholds.backward());
        if (forward && backward) {
            backward = false;
        }
        input.setForwardDown(forward);
        input.setBackwardDown(backward);

        boolean mostlyForward = forwardDot > STRAFE_SUPPRESSION_FORWARD_DOT && !lateralCorrection;
        double effectiveStrafeThreshold = mostlyForward
            ? Math.max(thresholds.strafe(), STRAFE_SUPPRESSION_DOT)
            : thresholds.strafe();
        boolean right = shouldHoldPositive(memory == null ? input.rightDown() : memory.right,
            strafeDot, effectiveStrafeThreshold);
        boolean left = shouldHoldNegative(memory == null ? input.leftDown() : memory.left,
            strafeDot, -effectiveStrafeThreshold);
        if (right && left) {
            if (Math.abs(strafeDot) < effectiveStrafeThreshold + RELEASE_MARGIN) {
                right = false;
                left = false;
            } else {
                left = strafeDot < 0.0;
                right = !left;
            }
        }
        input.setRightDown(right);
        input.setLeftDown(left);
        if (memory != null) {
            memory.remember(forward, backward, left, right);
        }
        return new MovementAxes(forwardDot, strafeDot, forward, backward, left, right);
    }

    static void applyGoalCentering(IPlayerLayer player, IInputLayer input, double dx, double dz) {
        float yawToGoal = (float) Math.toDegrees(Math.atan2(-dx, dz));
        float relativeYaw = wrapDegrees(yawToGoal - player.yaw());

        input.setForwardDown(relativeYaw > -67.5 && relativeYaw <= 67.5);
        input.setBackwardDown(relativeYaw > 112.5 || relativeYaw <= -112.5);
        input.setLeftDown(relativeYaw > -157.5 && relativeYaw <= -22.5);
        input.setRightDown(relativeYaw > 22.5 && relativeYaw <= 157.5);
        input.setSprintDown(false);
        boolean inWater = player.isInWater();
        input.setJumpDown(inWater);
        input.setSneakDown(!inWater);
    }

    static void applyCornerAlignment(IPlayerLayer player, IInputLayer input, double dx, double dz) {
        double hDist = Math.sqrt(dx * dx + dz * dz);
        if (hDist < 1.0e-6) {
            return;
        }

        applyRelativeMovement(
            player,
            input,
            dx / hDist,
            dz / hDist,
            0.05,
            -0.80,
            0.08);
        input.setBackwardDown(false);
        input.setSprintDown(false);
        input.setJumpDown(false);
    }

    private static float wrapDegrees(float value) {
        float wrapped = value % 360.0f;
        if (wrapped >= 180.0f) wrapped -= 360.0f;
        if (wrapped < -180.0f) wrapped += 360.0f;
        return wrapped;
    }

    private static boolean shouldHoldPositive(boolean currentlyHeld, double value, double pressThreshold) {
        double threshold = currentlyHeld ? pressThreshold - RELEASE_MARGIN : pressThreshold;
        return value > threshold;
    }

    private static boolean shouldHoldNegative(boolean currentlyHeld, double value, double pressThreshold) {
        double threshold = currentlyHeld ? pressThreshold + RELEASE_MARGIN : pressThreshold;
        return value < threshold;
    }

    static final class MovementMemory {
        private boolean initialized;
        private double forwardDot;
        private double strafeDot;
        private boolean forward;
        private boolean backward;
        private boolean left;
        private boolean right;

        void reset() {
            initialized = false;
            forwardDot = 0.0;
            strafeDot = 0.0;
            forward = false;
            backward = false;
            left = false;
            right = false;
        }

        private double smoothForward(double value) {
            forwardDot = smooth(forwardDot, value);
            return forwardDot;
        }

        private double smoothStrafe(double value) {
            strafeDot = smooth(strafeDot, value);
            initialized = true;
            return strafeDot;
        }

        private double smooth(double previous, double value) {
            return initialized ? previous + (value - previous) * DOT_SMOOTHING : value;
        }

        private void remember(boolean forward, boolean backward, boolean left, boolean right) {
            this.forward = forward;
            this.backward = backward;
            this.left = left;
            this.right = right;
        }
    }

    record MovementAxes(double forwardDot, double strafeDot, boolean forward, boolean backward,
                        boolean left, boolean right) {
    }

    record MovementThresholds(double forward, double backward, double strafe, boolean lateralCorrection) {
        MovementThresholds(double forward, double backward, double strafe) {
            this(forward, backward, strafe, false);
        }
    }

    record MovementIntent(double dx, double dz, MovementThresholds thresholds) {
        MovementIntent(double dx, double dz, double forwardThreshold, double backwardThreshold, double strafeThreshold) {
            this(dx, dz, new MovementThresholds(forwardThreshold, backwardThreshold, strafeThreshold));
        }

        MovementIntent(double dx, double dz, double forwardThreshold, double backwardThreshold,
                       double strafeThreshold, boolean lateralCorrection) {
            this(dx, dz, new MovementThresholds(forwardThreshold, backwardThreshold, strafeThreshold, lateralCorrection));
        }
    }
}
