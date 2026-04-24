package fr.riege.ebsl.pathfinding.execution;

import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;

final class PathRecoveryController {
    private static final long UNSTUCK_JUMP_MS = 1200;
    private static final long UNSTUCK_BACKUP_MS = 2200;
    private static final int BACKUP_TICKS = 8;
    private static final long PATH_REPLAN_STALE_MS = 1400;
    private static final double PATH_REPLAN_DRIFT_DISTANCE = 1.75;
    private static final long GROUNDED_NO_PROGRESS_REPLAN_MS = 1000;
    private static final long PATH_REPLAN_HARD_STALE_MS = 4200;

    private int backupTicksLeft;

    void reset() {
        backupTicksLeft = 0;
    }

    RecoveryDecision update(PathExecutor executor, Minecraft mc, Vec3 playerPos,
                            PathProgressSnapshot progress, boolean allowReplan,
                            boolean cooldownPassed, int jumpCooldown) {
        if (mc.player == null) {
            return RecoveryDecision.continueMovement();
        }

        if (allowReplan && mc.player.onGround()
            && progress.pathStale(GROUNDED_NO_PROGRESS_REPLAN_MS)) {
            return RecoveryDecision.replanFromPlayer("grounded no progress stale=" + progress.pathStaleMs());
        }

        if (allowReplan && progress.pathStale(PATH_REPLAN_HARD_STALE_MS) && cooldownPassed) {
            return RecoveryDecision.replanFromPlayer("hard stale path progress");
        }

        if (backupTicksLeft > 0) {
            applyBackupTick(mc);
            return RecoveryDecision.tickHandled();
        }

        boolean drifted = progress.drifted(PathExecutor.DRIFT_DISTANCE);
        if (progress.movementStale(UNSTUCK_BACKUP_MS) && !drifted && mc.player.onGround()) {
            backupTicksLeft = BACKUP_TICKS;
            executor.noteRecoveryMovement(playerPos);
            applyBackupTick(mc);
            return RecoveryDecision.tickHandled();
        }

        if (progress.movementStale(UNSTUCK_JUMP_MS) && !drifted
            && mc.player.onGround() && jumpCooldown == 0) {
            mc.options.keyJump.setDown(true);
            return RecoveryDecision.recoveryJump();
        }

        if (allowReplan && progress.pathStale(PATH_REPLAN_STALE_MS) && cooldownPassed
            && (progress.proximity().horizontalDistance() > PATH_REPLAN_DRIFT_DISTANCE
            || progress.distanceMoved() >= PathExecutor.STUCK_DIST_THRESHOLD)) {
            return RecoveryDecision.replanFromPlayer(String.format(
                "path progress stale drift=%.2f", progress.proximity().horizontalDistance()));
        }

        if (allowReplan && progress.movementStale(PathExecutor.STUCK_TIME_MS)
            && drifted && cooldownPassed) {
            return RecoveryDecision.replanFromPlayer(String.format(
                "drift stale=%.2f", progress.proximity().horizontalDistance()));
        }

        if (progress.movementStale(PathExecutor.STUCK_TIME_MS * 2) && cooldownPassed) {
            return RecoveryDecision.replanFromPlayer("deadlock stale=" + progress.movementStaleMs());
        }

        return RecoveryDecision.continueMovement();
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

    record RecoveryDecision(Action action, String reason) {
        static RecoveryDecision continueMovement() {
            return new RecoveryDecision(Action.CONTINUE_MOVEMENT, "");
        }

        static RecoveryDecision tickHandled() {
            return new RecoveryDecision(Action.TICK_HANDLED, "");
        }

        static RecoveryDecision recoveryJump() {
            return new RecoveryDecision(Action.RECOVERY_JUMP, "");
        }

        static RecoveryDecision replanFromPlayer(String reason) {
            return new RecoveryDecision(Action.REPLAN_FROM_PLAYER, reason);
        }
    }

    enum Action {
        CONTINUE_MOVEMENT,
        TICK_HANDLED,
        RECOVERY_JUMP,
        REPLAN_FROM_PLAYER
    }
}
