package fr.riege.ebsl.pathfinding.execution;

import fr.riege.ebsl.pathfinding.settings.PathfinderSettings;

final class ParkourRecoveryProfile implements MovementRecoveryProfile {
    @Override
    public long hardStaleMs() {
        return PathfinderSettings.instance().parkourPathReplanHardStaleMs.value();
    }

    @Override
    public long pathRepairStaleMs() {
        return PathfinderSettings.instance().parkourPathReplanStaleMs.value();
    }

    @Override
    public long groundedNoProgressMs() {
        return PathfinderSettings.instance().parkourGroundedNoProgressReplanMs.value();
    }

    @Override
    public long deadlockMs() {
        return Math.max(
            PathfinderSettings.instance().stuckTimeMs.value() * 4L,
            PathfinderSettings.instance().parkourPathReplanHardStaleMs.value());
    }

    @Override
    public boolean allowBackup() {
        return false;
    }

    @Override
    public boolean allowRecoveryJump() {
        return false;
    }
}
