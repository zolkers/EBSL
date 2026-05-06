package fr.riege.ebsl.common.terminal;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public final class TerminalLog {

    public enum EntryType { INPUT, OUTPUT, ERROR }

    public record LogEntry(String text, EntryType type) {}

    private static final int MAX_ENTRIES = 500;
    private static final Deque<LogEntry> LOG = new ArrayDeque<>();
    private static volatile boolean dirty = false;

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
        synchronized (LOG) {
            LOG.clear();
        }
        dirty = true;
    }

    public static List<LogEntry> snapshot() {
        synchronized (LOG) {
            return new ArrayList<>(LOG);
        }
    }

    public static boolean consumeDirty() {
        boolean was = dirty;
        dirty = false;
        return was;
    }

    private static void add(LogEntry entry) {
        synchronized (LOG) {
            if (LOG.size() >= MAX_ENTRIES) {
                LOG.pollFirst();
            }
            LOG.addLast(entry);
        }
        dirty = true;
    }
}
