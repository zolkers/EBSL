package fr.riege.ebsl.common.analytics;

import java.util.ArrayList;
import java.util.List;

public final class AnalyticsEventLog {
    private static final List<String> EVENTS = new ArrayList<>();

    private AnalyticsEventLog() {
    }

    public static void record(String category, String message) {
        EVENTS.add(category + ": " + message);
    }

    public static List<String> snapshot() {
        return List.copyOf(EVENTS);
    }

    public static void clear() {
        EVENTS.clear();
    }
}
