package fr.riege.ebsl.common.feature.terminal;

public enum CommandScope {
    /** Works without an active player session. */
    TERMINAL,
    /** Requires mc.player != null. */
    MC,
    /** Works in both contexts. */
    BOTH
}
