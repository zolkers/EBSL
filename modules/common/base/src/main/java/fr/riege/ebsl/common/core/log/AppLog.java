package fr.riege.ebsl.common.core.log;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public final class AppLog {
    private static final int MAX_ENTRIES = 1000;
    private static final Deque<LogEntry> LOG = new ArrayDeque<>();
    private static volatile boolean dirty = false;
    private static Appender appender;

    private AppLog() {}

    public record LogEntry(String time, String level, String logger, String text) {}

    @FunctionalInterface
    public interface Appender {
        void bootstrap(Receiver receiver);
    }

    @FunctionalInterface
    public interface Receiver {
        void receive(LogEntry entry);
    }

    public static void setAppender(Appender a) {
        appender = a;
    }

    public static void bootstrap() {
        if (appender != null) {
            appender.bootstrap(AppLog::add);
        }
    }

    public static void clear() {
        synchronized (LOG) { LOG.clear(); }
        dirty = true;
    }

    public static List<LogEntry> snapshot() {
        synchronized (LOG) { return new ArrayList<>(LOG); }
    }

    public static boolean consumeDirty() {
        boolean was = dirty;
        dirty = false;
        return was;
    }

    private static void add(LogEntry entry) {
        synchronized (LOG) {
            if (LOG.size() >= MAX_ENTRIES) LOG.pollFirst();
            LOG.addLast(entry);
        }
        dirty = true;
    }
}
