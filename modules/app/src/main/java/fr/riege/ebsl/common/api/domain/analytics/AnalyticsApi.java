package fr.riege.ebsl.common.api.domain.analytics;

import fr.riege.ebsl.common.api.core.annotation.EbslApiOperation;
import fr.riege.ebsl.common.api.core.annotation.EbslApiSurface;
import fr.riege.ebsl.common.domain.analytics.AnalyticsEvent;
import fr.riege.ebsl.common.domain.analytics.AnalyticsEventLog;
import fr.riege.ebsl.common.domain.analytics.AnalyticsSnapshot;
import fr.riege.ebsl.common.feature.module.PathfinderModule;
import fr.riege.ebsl.common.platform.service.EbslServices;

import java.util.List;

@EbslApiSurface(EbslApiSurface.Domain.ANALYTICS)
public final class AnalyticsApi {
    @EbslApiOperation("Record a UI or module analytics event.")
    public void track(String source, String message) {
        AnalyticsEventLog.recordAnalytics(source, message);
    }

    @EbslApiOperation("Read the latest analytics events.")
    public List<AnalyticsEvent> latestEvents(int count) {
        return AnalyticsEventLog.latest(count);
    }

    @EbslApiOperation("Read every retained analytics event.")
    public List<AnalyticsEvent> events() {
        return AnalyticsEventLog.snapshot();
    }

    @EbslApiOperation("Clear retained analytics events.")
    public void clear() {
        AnalyticsEventLog.clear();
    }

    @EbslApiOperation("Capture a compact analytics snapshot.")
    public AnalyticsSnapshot snapshot(PathfinderModule selectedModule) {
        return AnalyticsSnapshot.capture(EbslServices.navigation(), selectedModule);
    }
}
