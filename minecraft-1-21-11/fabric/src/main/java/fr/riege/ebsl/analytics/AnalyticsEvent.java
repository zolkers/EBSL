package fr.riege.ebsl.analytics;

public record AnalyticsEvent(long timestampMs, String source, String message) {
    public static AnalyticsEvent now(String source, String message) {
        return new AnalyticsEvent(System.currentTimeMillis(), source, message);
    }
}
