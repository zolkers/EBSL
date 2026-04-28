package fr.riege.ebsl.pathfinding.execution;

import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;

final class PathRecoveryController {
    private static final long UNSTUCK_JUMP_MS = 400;
    private static final long UNSTUCK_BACKUP_MS = 900;
    private static final int BACKUP_TICKS = 6;
    private static final long PATH_REPLAN_STALE_MS = 500;
    private static final double PATH_REPLAN_DRIFT_DISTANCE = 1.25;
    private static final long GROUNDED_NO_PROGRESS_REPLAN_MS = 400;
    private static final long PATH_REPLAN_HARD_STALE_MS = 1800;
    private static final double BACKUP_MAX_HORIZONTAL_SPEED = 0.22;

    private int backupTicksLeft;

    void reset() {
        backupTicksLeft = 0;
    }

    RecoveryDecision update(Minecraft mc, Vec3 playerPos,
                            PathProgressSnapshot progress, boolean allowReplan,
                            boolean cooldownPassed, int jumpCooldown) {
        if (mc.player == null) {
            return RecoveryDecision.continueMovement();
        }

        boolean inWater = mc.player.isInWater();
        keepSurfaceSwimming(mc, inWater);

        if (allowReplan && mc.player.onGround()
            && progress.pathStale(GROUNDED_NO_PROGRESS_REPLAN_MS)) {
            return RecoveryDecision.repairToSegment("grounded no progress stale=" + progress.pathStaleMs());
        }

        if (allowReplan && progress.pathStale(PATH_REPLAN_HARD_STALE_MS) && cooldownPassed) {
            return RecoveryDecision.replanFromPlayer("hard stale path progress");
        }

        if (backupTicksLeft > 0 && horizontalSpeed(mc) > BACKUP_MAX_HORIZONTAL_SPEED) {
            backupTicksLeft = 0;
        }

        if (backupTicksLeft > 0) {
            applyBackupTick(mc);
            return RecoveryDecision.tickHandled();
        }

        boolean drifted = progress.drifted(PathExecutor.DRIFT_DISTANCE);
        if (shouldBackup(mc, progress, drifted, inWater)) {
            backupTicksLeft = BACKUP_TICKS;
            applyBackupTick(mc);
            return RecoveryDecision.tickHandledWithProgress();
        }

        if (shouldJumpForRecovery(mc, progress, drifted, jumpCooldown)) {
            mc.options.keyJump.setDown(true);
            return RecoveryDecision.recoveryJump();
        }

        if (allowReplan && progress.pathStale(PATH_REPLAN_STALE_MS) && cooldownPassed
            && (progress.proximity().horizontalDistance() > PATH_REPLAN_DRIFT_DISTANCE
            || progress.distanceMoved() >= PathExecutor.STUCK_DIST_THRESHOLD)) {
            return RecoveryDecision.repairToSegment(String.format(
                "path progress stale drift=%.2f", progress.proximity().horizontalDistance()));
        }

        if (allowReplan && progress.movementStale(PathExecutor.STUCK_TIME_MS)
            && drifted && cooldownPassed) {
            return RecoveryDecision.repairToSegment(String.format(
                "drift stale=%.2f", progress.proximity().horizontalDistance()));
        }

        if (progress.movementStale(PathExecutor.STUCK_TIME_MS * 2) && cooldownPassed) {
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
            && progress.movementStale(UNSTUCK_BACKUP_MS)
            && !drifted
            && mc.player.onGround()
            && horizontalSpeed(mc) <= BACKUP_MAX_HORIZONTAL_SPEED;
    }

    private boolean shouldJumpForRecovery(Minecraft mc, PathProgressSnapshot progress,
                                          boolean drifted, int jumpCooldown) {
        return progress.movementStale(UNSTUCK_JUMP_MS)
            && !drifted
            && mc.player.onGround()
            && jumpCooldown == 0;
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
        REPAIR_TO_SEGMENT,
        REPLAN_FROM_PLAYER
    }
}
