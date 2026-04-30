package fr.riege.ebsl.api.analytics;

import fr.riege.ebsl.analytics.AnalyticsEvent;
import fr.riege.ebsl.analytics.AnalyticsEventLog;
import fr.riege.ebsl.analytics.AnalyticsSnapshot;
import fr.riege.ebsl.api.annotation.EbslApiOperation;
import fr.riege.ebsl.api.annotation.EbslApiSurface;
import fr.riege.ebsl.general.module.PathfinderModule;

import java.util.List;

@EbslApiSurface(EbslApiSurface.Domain.ANALYTICS)
public final class AnalyticsApi {
    public AnalyticsApi() {
    }

    @EbslApiOperation("Record a UI or module analytics event.")
    public void record(String source, String message) {
        AnalyticsEventLog.record(source, message);
    }

    @EbslApiOperation("Read the latest analytics events.")
    public List<AnalyticsEvent> latestEvents(int count) {
        return AnalyticsEventLog.latest(count);
    }

    @EbslApiOperation("Capture a compact analytics snapshot.")
    public AnalyticsSnapshot snapshot(PathfinderModule selectedModule) {
        return AnalyticsSnapshot.capture(selectedModule);
    }
}
