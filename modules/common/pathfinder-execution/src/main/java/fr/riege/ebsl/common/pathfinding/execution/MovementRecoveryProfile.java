package fr.riege.ebsl.common.pathfinding.execution;

import fr.riege.ebsl.common.pathfinding.settings.PathfinderSettings;

/**
 * Defines the contract for {@code MovementRecoveryProfile} implementations.
 */
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
