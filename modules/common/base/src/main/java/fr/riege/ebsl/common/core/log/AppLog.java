package fr.riege.ebsl.common.core.log;

import java.util.List;

public final class AppLog {
    private static final int MAX_ENTRIES = 1000;
    private static final BoundedDirtyLog<LogEntry> LOG = new BoundedDirtyLog<>(MAX_ENTRIES);
    private static Appender appender;

    private AppLog() {}

    public record LogEntry(String time, AppLogLevel level, String logger, String text) {}

    /**
     * Defines the contract for {@code Appender} implementations.
     */
    @FunctionalInterface
    public interface Appender {
        void bootstrap(Receiver receiver);
    }

    /**
     * Defines the contract for {@code Receiver} implementations.
     */
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
