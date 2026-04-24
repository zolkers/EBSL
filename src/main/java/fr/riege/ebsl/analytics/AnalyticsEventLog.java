package fr.riege.ebsl.analytics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class AnalyticsEventLog {
    private static final int MAX_EVENTS = 80;
    private static final List<AnalyticsEvent> EVENTS = new ArrayList<>();

    private AnalyticsEventLog() {
    }

    public static synchronized void record(String source, String message) {
        EVENTS.add(AnalyticsEvent.now(source, message));
        while (EVENTS.size() > MAX_EVENTS) {
            EVENTS.removeFirst();
        }
    }

    public static synchronized List<AnalyticsEvent> latest(int count) {
        int from = Math.max(0, EVENTS.size() - count);
        return Collections.unmodifiableList(new ArrayList<>(EVENTS.subList(from, EVENTS.size())));
    }
}
