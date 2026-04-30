package fr.riege.ebsl.terminal;

public enum CommandScope {
    /** Works without an active player session. */
    TERMINAL,
    /** Requires mc.player != null. */
    MC,
    /** Works in both contexts. */
    BOTH
}
