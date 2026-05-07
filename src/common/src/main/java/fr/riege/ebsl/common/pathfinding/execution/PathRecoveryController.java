package fr.riege.ebsl.common.pathfinding.execution;

import fr.riege.ebsl.common.platform.layer.IPhysicsLayer;
import fr.riege.ebsl.common.platform.layer.IPlayerLayer;
import fr.riege.ebsl.common.math.Vec3d;
import fr.riege.ebsl.common.pathfinding.Node;
import fr.riege.ebsl.common.pathfinding.settings.PathfinderSettings;

final class PathRecoveryController {
    private int backupTicksLeft;
    private long cornerAlignStartMs;

    void reset() {
        backupTicksLeft = 0;
        cornerAlignStartMs = 0;
    }

    RecoveryDecision update(IPlayerLayer player, IPhysicsLayer physics,
                            PathProgressSnapshot progress, boolean allowReplan,
                            boolean cooldownPassed, int jumpCooldown, Node.MoveType recoveryMoveType) {
        MovementRecoveryProfile recoveryProfile = MovementRecoveryRegistry.get(recoveryMoveType);

        boolean inWater = player.isInWater();
        keepSurfaceSwimming(physics, inWater);

        if (allowReplan && progress.pathStale(recoveryProfile.hardStaleMs()) && cooldownPassed) {
            return RecoveryDecision.repairToSegment("hard stale path progress stale=" + progress.pathStaleMs());
        }

        if (backupTicksLeft > 0 && horizontalSpeed(player) > PathfinderSettings.instance().backupMaxHorizontalSpeed.value()) {
            backupTicksLeft = 0;
        }

        if (backupTicksLeft > 0) {
            applyBackupTick(physics);
            return RecoveryDecision.tickHandled();
        }

        boolean drifted = progress.drifted(PathfinderSettings.instance().driftDistance.value());
        if (recoveryProfile.allowBackup() && shouldBackup(player, progress, drifted, inWater)) {
            backupTicksLeft = PathfinderSettings.instance().backupTicks.value();
            applyBackupTick(physics);
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
            physics.setJump(true);
            return RecoveryDecision.recoveryJump();
        }

        if (allowReplan && player.onGround() && progress.pathStale(recoveryProfile.groundedNoProgressMs())) {
            return RecoveryDecision.repairToSegment("grounded no progress stale=" + progress.pathStaleMs());
        }

        if (allowReplan && progress.pathStale(recoveryProfile.pathRepairStaleMs()) && cooldownPassed
            && (progress.proximity().horizontalDistance() > PathfinderSettings.instance().pathReplanDriftDistance.value()
            || progress.distanceMoved() >= PathfinderSettings.instance().stuckDistThreshold.value())) {
            return RecoveryDecision.repairToSegment(String.format(
                "path progress stale drift=%.2f stale=%d", progress.proximity().horizontalDistance(), progress.pathStaleMs()));
        }

        if (allowReplan && progress.movementStale(PathfinderSettings.instance().stuckTimeMs.value())
            && drifted && cooldownPassed) {
            return RecoveryDecision.repairToSegment(String.format(
                "drift stale=%.2f", progress.proximity().horizontalDistance()));
        }

        if (allowReplan && progress.movementStale(recoveryProfile.deadlockMs()) && cooldownPassed) {
            return RecoveryDecision.repairToSegment("deadlock stale=" + progress.movementStaleMs());
        }

        return RecoveryDecision.continueMovement();
    }

    private void keepSurfaceSwimming(IPhysicsLayer physics, boolean inWater) {
        if (!inWater) return;
        backupTicksLeft = 0;
        physics.setJump(true);
        physics.setSneak(false);
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

    private void applyBackupTick(IPhysicsLayer physics) {
        backupTicksLeft--;
        physics.setForward(false);
        physics.setBackward(true);
        physics.setLeft(false);
        physics.setRight(false);
        physics.setJump(false);
        physics.setSprint(false);
        if (backupTicksLeft == 0) {
            physics.setBackward(false);
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
