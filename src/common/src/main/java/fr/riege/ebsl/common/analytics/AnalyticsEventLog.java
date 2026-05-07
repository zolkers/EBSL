package fr.riege.ebsl.common.analytics;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public final class AnalyticsEventLog {
    private static final int MAX = 256;
    private static final Deque<AnalyticsEvent> EVENTS = new ArrayDeque<>(MAX);

    private AnalyticsEventLog() {}

    public static void recordAnalytics(String source, String message) {
        synchronized (EVENTS) {
            if (EVENTS.size() >= MAX) EVENTS.removeFirst();
            EVENTS.addLast(AnalyticsEvent.now(source, message));
        }
    }

    public static List<AnalyticsEvent> snapshot() {
        synchronized (EVENTS) { return new ArrayList<>(EVENTS); }
    }

    public static List<AnalyticsEvent> latest(int count) {
        List<AnalyticsEvent> all = snapshot();
        int start = Math.max(0, all.size() - count);
        return all.subList(start, all.size());
    }

    public static void clear() {
        synchronized (EVENTS) { EVENTS.clear(); }
    }
}
