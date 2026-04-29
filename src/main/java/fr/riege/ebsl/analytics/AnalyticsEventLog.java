package fr.riege.ebsl.analytics;

import fr.riege.ebsl.EbslMod;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class AnalyticsEventLog {
    private static final int MAX_EVENTS = 240;
    private static final DateTimeFormatter FILE_TIMESTAMP = DateTimeFormatter
        .ofPattern("yyyyMMdd-HHmmss-SSS", Locale.ROOT)
        .withZone(ZoneId.systemDefault());
    private static final DateTimeFormatter EVENT_TIMESTAMP = DateTimeFormatter
        .ofPattern("yyyy-MM-dd HH:mm:ss.SSS", Locale.ROOT)
        .withZone(ZoneId.systemDefault());
    private static final List<AnalyticsEvent> EVENTS = new ArrayList<>();
    private static final Path SESSION_FILE = FabricLoader.getInstance()
        .getConfigDir()
        .resolve("ebsl")
        .resolve(".session")
        .resolve(FILE_TIMESTAMP.format(Instant.now()) + ".log");
    private static boolean fileUnavailable;

    private AnalyticsEventLog() {
    }

    public static synchronized void record(String source, String message) {
        AnalyticsEvent event = AnalyticsEvent.now(source, message);
        EVENTS.add(event);
        while (EVENTS.size() > MAX_EVENTS) {
            EVENTS.removeFirst();
        }
        writeSessionEvent(event);
    }

    public static synchronized List<AnalyticsEvent> latest(int count) {
        int from = Math.max(0, EVENTS.size() - count);
        return List.copyOf(EVENTS.subList(from, EVENTS.size()));
    }

    public static Path sessionFile() {
        return SESSION_FILE;
    }

    private static void writeSessionEvent(AnalyticsEvent event) {
        if (fileUnavailable) {
            return;
        }
        try {
            Files.createDirectories(SESSION_FILE.getParent());
            Files.writeString(
                SESSION_FILE,
                format(event) + System.lineSeparator(),
                StandardOpenOption.CREATE,
                StandardOpenOption.APPEND);
        } catch (IOException exception) {
            fileUnavailable = true;
            EbslMod.LOGGER.warn("Failed to write EBSL analytics session log: {}", SESSION_FILE, exception);
        }
    }

    private static String format(AnalyticsEvent event) {
        return String.format(Locale.ROOT,
            "%s [%s] %s",
            EVENT_TIMESTAMP.format(Instant.ofEpochMilli(event.timestampMs())),
            event.source(),
            event.message());
    }
}
