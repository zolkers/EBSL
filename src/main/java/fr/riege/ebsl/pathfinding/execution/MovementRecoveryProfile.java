package fr.riege.ebsl.pathfinding.execution;

interface MovementRecoveryProfile {
    long hardStaleMs();

    long pathRepairStaleMs();

    long groundedNoProgressMs();

    boolean allowBackup();

    boolean allowRecoveryJump();
}
