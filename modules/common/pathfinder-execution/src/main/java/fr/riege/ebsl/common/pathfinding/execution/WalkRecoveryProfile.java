package fr.riege.ebsl.common.pathfinding.execution;

import fr.riege.ebsl.common.pathfinding.settings.PathfinderSettings;

final class WalkRecoveryProfile implements MovementRecoveryProfile {
    @Override
    public long hardStaleMs() {
        return PathfinderSettings.instance().pathReplanHardStaleMs.value();
    }

    @Override
    public long pathRepairStaleMs() {
        return PathfinderSettings.instance().pathReplanStaleMs.value();
    }

    @Override
    public long groundedNoProgressMs() {
        return PathfinderSettings.instance().groundedNoProgressReplanMs.value();
    }

    @Override
    public boolean allowBackup() {
        return true;
    }

    @Override
    public boolean allowRecoveryJump() {
        return true;
    }
}
