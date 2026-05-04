package fr.riege.ebsl.pathfinding.execution;

import fr.riege.ebsl.pathfinding.settings.PathfinderSettings;

interface MovementRecoveryProfile {
    long hardStaleMs();

    long pathRepairStaleMs();

    long groundedNoProgressMs();

    default long deadlockMs() {
        return PathfinderSettings.instance().stuckTimeMs.value() * 2L;
    }

    boolean allowBackup();

    boolean allowRecoveryJump();
}
