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
import fr.riege.ebsl.common.pathfinding.settings.PathfinderSettings;
import fr.riege.ebsl.common.platform.layer.IInputLayer;
import fr.riege.ebsl.common.world.layer.IPlayerLayer;

final class PathRecoveryController {
    private int backupTicksLeft;
    private long cornerAlignStartMs;

    void reset() {
        backupTicksLeft = 0;
        cornerAlignStartMs = 0;
    }

    RecoveryDecision update(IPlayerLayer player, IInputLayer input,
                            PathProgressSnapshot progress, boolean allowReplan,
                            boolean cooldownPassed, int jumpCooldown, Node.MoveType recoveryMoveType) {
        MovementRecoveryProfile recoveryProfile = MovementRecoveryRegistry.get(recoveryMoveType);

        boolean inWater = player.isInWater();
        keepSurfaceSwimming(input, inWater);

        if (allowReplan && progress.pathStale(recoveryProfile.hardStaleMs()) && cooldownPassed) {
            return RecoveryDecision.repairToSegment("hard stale path progress stale=" + progress.pathStaleMs());
        }

        if (backupTicksLeft > 0 && horizontalSpeed(player) > PathfinderSettings.instance().backupMaxHorizontalSpeed.value()) {
            backupTicksLeft = 0;
        }

        if (backupTicksLeft > 0) {
            applyBackupTick(input);
            return RecoveryDecision.tickHandled();
        }

        boolean drifted = progress.drifted(PathfinderSettings.instance().driftDistance.value());
        if (recoveryProfile.allowBackup() && shouldBackup(player, progress, drifted, inWater)) {
            backupTicksLeft = PathfinderSettings.instance().backupTicks.value();
            applyBackupTick(input);
            return RecoveryDecision.tickHandledWithProgress();
        }

        boolean alignOffCorner = shouldAlignOffCorner(player, progress, inWater, recoveryProfile.groundedNoProgressMs());
        if (alignOffCorner && withinCornerAlignWindow()) {
            return RecoveryDecision.alignToPath();
        }
        if (!alignOffCorner) {
            cornerAlignStartMs = 0;
        }

        if (recoveryProfile.allowRecoveryJump() && shouldJumpForRecovery(player, progress, drifted, jumpCooldown)) {
            input.setJumpDown(true);
            return RecoveryDecision.recoveryJump();
        }

        RecoveryDecision repair = repairDecision(player, progress, allowReplan, cooldownPassed, drifted, recoveryProfile);
        if (repair != null) {
            return repair;
        }

        return RecoveryDecision.continueMovement();
    }

    private RecoveryDecision repairDecision(IPlayerLayer player,
                                            PathProgressSnapshot progress,
                                            boolean allowReplan,
                                            boolean cooldownPassed,
                                            boolean drifted,
                                            MovementRecoveryProfile recoveryProfile) {
        if (!allowReplan) {
            return null;
        }
        if (player.onGround() && progress.pathStale(recoveryProfile.groundedNoProgressMs())) {
            return RecoveryDecision.repairToSegment("grounded no progress stale=" + progress.pathStaleMs());
        }
        if (shouldRepairStalePath(progress, cooldownPassed, recoveryProfile)) {
            return RecoveryDecision.repairToSegment(String.format(
                "path progress stale drift=%.2f stale=%d", progress.proximity().horizontalDistance(), progress.pathStaleMs()));
        }
        if (progress.movementStale(PathfinderSettings.instance().stuckTimeMs.value()) && drifted && cooldownPassed) {
            return RecoveryDecision.repairToSegment(String.format(
                "drift stale=%.2f", progress.proximity().horizontalDistance()));
        }
        if (progress.movementStale(recoveryProfile.deadlockMs()) && cooldownPassed) {
            return RecoveryDecision.repairToSegment("deadlock stale=" + progress.movementStaleMs());
        }
        return null;
    }

    private static boolean shouldRepairStalePath(PathProgressSnapshot progress,
                                                 boolean cooldownPassed,
                                                 MovementRecoveryProfile recoveryProfile) {
        return progress.pathStale(recoveryProfile.pathRepairStaleMs())
            && cooldownPassed
            && (progress.proximity().horizontalDistance() > PathfinderSettings.instance().pathReplanDriftDistance.value()
            || progress.distanceMoved() >= PathfinderSettings.instance().stuckDistThreshold.value());
    }

    private void keepSurfaceSwimming(IInputLayer input, boolean inWater) {
        if (!inWater) return;
        backupTicksLeft = 0;
        input.setJumpDown(true);
        input.setSneakDown(false);
    }

    private boolean shouldBackup(IPlayerLayer player, PathProgressSnapshot progress, boolean drifted, boolean inWater) {
        return !inWater
            && progress.movementStale(PathfinderSettings.instance().unstuckBackupMs.value())
            && !drifted
            && player.onGround()
            && horizontalSpeed(player) <= PathfinderSettings.instance().backupMaxHorizontalSpeed.value();
    }

    private boolean shouldJumpForRecovery(IPlayerLayer player, PathProgressSnapshot progress, boolean drifted, int jumpCooldown) {
        return progress.movementStale(PathfinderSettings.instance().unstuckJumpMs.value())
            && !drifted
            && player.onGround()
            && jumpCooldown == 0;
    }

    private boolean shouldAlignOffCorner(IPlayerLayer player, PathProgressSnapshot progress,
                                         boolean inWater, long groundedNoProgressMs) {
        return !inWater
            && player.onGround()
            && progress.pathStale(groundedNoProgressMs)
            && progress.proximity().horizontalDistance() >= PathfinderSettings.instance().cornerAlignMinDistance.value()
            && progress.proximity().horizontalDistance() <= PathfinderSettings.instance().cornerAlignMaxDistance.value()
            && progress.proximity().verticalDistance() <= PathfinderSettings.instance().cornerAlignMaxVertical.value();
    }

    private boolean withinCornerAlignWindow() {
        long now = System.currentTimeMillis();
        if (cornerAlignStartMs == 0) {
            cornerAlignStartMs = now;
        }
        return now - cornerAlignStartMs <= PathfinderSettings.instance().cornerAlignMaxMs.value();
    }

    private void applyBackupTick(IInputLayer input) {
        backupTicksLeft--;
        input.setForwardDown(false);
        input.setBackwardDown(true);
        input.setLeftDown(false);
        input.setRightDown(false);
        input.setJumpDown(false);
        input.setSprintDown(false);
        if (backupTicksLeft == 0) {
            input.setBackwardDown(false);
        }
    }

    private static double horizontalSpeed(IPlayerLayer player) {
        Vec3d velocity = player.velocity();
        return Math.sqrt(velocity.x() * velocity.x() + velocity.z() * velocity.z());
    }

    record RecoveryDecision(Action action, String reason, boolean noteProgress) {
        static RecoveryDecision continueMovement() { return new RecoveryDecision(Action.CONTINUE_MOVEMENT, "", false); }
        static RecoveryDecision tickHandled() { return new RecoveryDecision(Action.TICK_HANDLED, "", false); }
        static RecoveryDecision tickHandledWithProgress() { return new RecoveryDecision(Action.TICK_HANDLED, "", true); }
        static RecoveryDecision recoveryJump() { return new RecoveryDecision(Action.RECOVERY_JUMP, "", false); }
        static RecoveryDecision alignToPath() { return new RecoveryDecision(Action.ALIGN_TO_PATH, "", false); }
        static RecoveryDecision replanFromPlayer(String reason) { return new RecoveryDecision(Action.REPLAN_FROM_PLAYER, reason, false); }
        static RecoveryDecision repairToSegment(String reason) { return new RecoveryDecision(Action.REPAIR_TO_SEGMENT, reason, false); }
    }

    enum Action {
        CONTINUE_MOVEMENT,
        TICK_HANDLED,
        RECOVERY_JUMP,
        ALIGN_TO_PATH,
        REPAIR_TO_SEGMENT,
        REPLAN_FROM_PLAYER
    }
}
