package fr.riege.ebsl.pathfinding.execution;

import fr.riege.ebsl.pathfinding.Node;
import fr.riege.ebsl.pathfinding.settings.PathfinderSettings;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;

final class PathRecoveryController {
    private int backupTicksLeft;
    private long cornerAlignStartMs;

    void reset() {
        backupTicksLeft = 0;
        cornerAlignStartMs = 0;
    }

    RecoveryDecision update(Minecraft mc, Vec3 playerPos,
                            PathProgressSnapshot progress, boolean allowReplan,
                            boolean cooldownPassed, int jumpCooldown, Node.MoveType recoveryMoveType) {
        if (mc.player == null) {
            return RecoveryDecision.continueMovement();
        }
        MovementRecoveryProfile recoveryProfile = MovementRecoveryRegistry.get(recoveryMoveType);

        boolean inWater = mc.player.isInWater();
        keepSurfaceSwimming(mc, inWater);

        if (allowReplan && progress.pathStale(recoveryProfile.hardStaleMs()) && cooldownPassed) {
            return RecoveryDecision.replanFromPlayer("hard stale path progress stale=" + progress.pathStaleMs());
        }

        if (backupTicksLeft > 0 && horizontalSpeed(mc) > PathfinderSettings.instance().backupMaxHorizontalSpeed.value()) {
            backupTicksLeft = 0;
        }

        if (backupTicksLeft > 0) {
            applyBackupTick(mc);
            return RecoveryDecision.tickHandled();
        }

        boolean drifted = progress.drifted(PathfinderSettings.instance().driftDistance.value());
        if (recoveryProfile.allowBackup() && shouldBackup(mc, progress, drifted, inWater)) {
            backupTicksLeft = PathfinderSettings.instance().backupTicks.value();
            applyBackupTick(mc);
            return RecoveryDecision.tickHandledWithProgress();
        }

        boolean alignOffCorner = shouldAlignOffCorner(mc, progress, inWater, recoveryProfile.groundedNoProgressMs());
        if (alignOffCorner && withinCornerAlignWindow()) {
            return RecoveryDecision.alignToPath();
        }
        if (!alignOffCorner) {
            cornerAlignStartMs = 0;
        }

        if (recoveryProfile.allowRecoveryJump() && shouldJumpForRecovery(mc, progress, drifted, jumpCooldown)) {
            mc.options.keyJump.setDown(true);
            return RecoveryDecision.recoveryJump();
        }

        if (allowReplan && mc.player.onGround()
            && progress.pathStale(recoveryProfile.groundedNoProgressMs())) {
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

        if (progress.movementStale(PathfinderSettings.instance().stuckTimeMs.value() * 2L) && cooldownPassed) {
            return RecoveryDecision.replanFromPlayer("deadlock stale=" + progress.movementStaleMs());
        }

        return RecoveryDecision.continueMovement();
    }

    private void keepSurfaceSwimming(Minecraft mc, boolean inWater) {
        if (!inWater) {
            return;
        }
        backupTicksLeft = 0;
        mc.options.keyJump.setDown(true);
        mc.options.keyShift.setDown(false);
    }

    private boolean shouldBackup(Minecraft mc, PathProgressSnapshot progress, boolean drifted, boolean inWater) {
        return !inWater
            && progress.movementStale(PathfinderSettings.instance().unstuckBackupMs.value())
            && !drifted
            && mc.player.onGround()
            && horizontalSpeed(mc) <= PathfinderSettings.instance().backupMaxHorizontalSpeed.value();
    }

    private boolean shouldJumpForRecovery(Minecraft mc, PathProgressSnapshot progress,
                                          boolean drifted, int jumpCooldown) {
        return progress.movementStale(PathfinderSettings.instance().unstuckJumpMs.value())
            && !drifted
            && mc.player.onGround()
            && jumpCooldown == 0;
    }

    private boolean shouldAlignOffCorner(Minecraft mc, PathProgressSnapshot progress,
                                         boolean inWater, long groundedNoProgressMs) {
        return !inWater
            && mc.player.onGround()
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

    private void applyBackupTick(Minecraft mc) {
        backupTicksLeft--;
        mc.options.keyUp.setDown(false);
        mc.options.keyDown.setDown(true);
        mc.options.keyLeft.setDown(false);
        mc.options.keyRight.setDown(false);
        mc.options.keyJump.setDown(false);
        mc.options.keySprint.setDown(false);
        if (backupTicksLeft == 0) {
            mc.options.keyDown.setDown(false);
        }
    }

    private static double horizontalSpeed(Minecraft mc) {
        Vec3 velocity = mc.player.getDeltaMovement();
        return Math.sqrt(velocity.x * velocity.x + velocity.z * velocity.z);
    }

    record RecoveryDecision(Action action, String reason, boolean noteProgress) {
        static RecoveryDecision continueMovement() {
            return new RecoveryDecision(Action.CONTINUE_MOVEMENT, "", false);
        }

        static RecoveryDecision tickHandled() {
            return new RecoveryDecision(Action.TICK_HANDLED, "", false);
        }

        static RecoveryDecision tickHandledWithProgress() {
            return new RecoveryDecision(Action.TICK_HANDLED, "", true);
        }

        static RecoveryDecision recoveryJump() {
            return new RecoveryDecision(Action.RECOVERY_JUMP, "", false);
        }

        static RecoveryDecision alignToPath() {
            return new RecoveryDecision(Action.ALIGN_TO_PATH, "", false);
        }

        static RecoveryDecision replanFromPlayer(String reason) {
            return new RecoveryDecision(Action.REPLAN_FROM_PLAYER, reason, false);
        }

        static RecoveryDecision repairToSegment(String reason) {
            return new RecoveryDecision(Action.REPAIR_TO_SEGMENT, reason, false);
        }
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
