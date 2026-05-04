package fr.riege.ebsl.mc;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Property;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public final class McChatLog {

    private static final int MAX_ENTRIES = 1000;
    private static final DateTimeFormatter TIME_FORMAT =
        DateTimeFormatter.ofPattern("HH:mm:ss").withZone(ZoneId.systemDefault());
    private static final Deque<McLogEntry> LOG = new ArrayDeque<>();
    private static volatile boolean dirty = false;

    private McChatLog() {}

    public record McLogEntry(String time, String level, String logger, String text) {}

    public static void bootstrap() {
        LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
        var config = ctx.getConfiguration();
        var appender = new AbstractAppender("EbslUiAppender", null, null, true, Property.EMPTY_ARRAY) {
            @Override
            public void append(LogEvent event) {
                String time = TIME_FORMAT.format(Instant.ofEpochMilli(event.getTimeMillis()));
                String level = event.getLevel().name();
                String loggerName = event.getLoggerName();
                int dot = loggerName.lastIndexOf('.');
                String shortLogger = dot >= 0 ? loggerName.substring(dot + 1) : loggerName;
                String msg = event.getMessage().getFormattedMessage();
                add(new McLogEntry(time, level, shortLogger, msg));
            }
        };
        appender.start();
        config.getRootLogger().addAppender(appender, Level.INFO, null);
        ctx.updateLoggers();
    }

    public static void clear() {
        synchronized (LOG) {
            LOG.clear();
        }
        dirty = true;
    }

    public static List<McLogEntry> snapshot() {
        synchronized (LOG) {
            return new ArrayList<>(LOG);
        }
    }

    public static boolean consumeDirty() {
        boolean was = dirty;
        dirty = false;
        return was;
    }

    private static void add(McLogEntry entry) {
        synchronized (LOG) {
            if (LOG.size() >= MAX_ENTRIES) {
                LOG.pollFirst();
            }
            LOG.addLast(entry);
        }
        dirty = true;
    }
}
