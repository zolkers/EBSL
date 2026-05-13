package fr.riege.ebsl.common.feature.terminal;

import fr.riege.ebsl.common.core.log.BoundedDirtyLog;

import java.util.List;

public final class TerminalLog {

    public enum EntryType { INPUT, OUTPUT, ERROR }

    public record LogEntry(String text, EntryType type) {}

    private static final int MAX_ENTRIES = 500;
    private static final BoundedDirtyLog<LogEntry> LOG = new BoundedDirtyLog<>(MAX_ENTRIES);

    private TerminalLog() {}

    public static void addInput(String text) {
        add(new LogEntry(text, EntryType.INPUT));
    }

    public static void addOutput(String text) {
        add(new LogEntry(text, EntryType.OUTPUT));
    }

    public static void addError(String text) {
        add(new LogEntry(text, EntryType.ERROR));
    }

    public static void clear() {
        LOG.clear();
    }

    public static List<LogEntry> snapshot() {
        return LOG.snapshot();
    }

    public static boolean consumeDirty() {
        return LOG.consumeDirty();
    }

    private static void add(LogEntry entry) {
        LOG.add(entry);
    }
}
