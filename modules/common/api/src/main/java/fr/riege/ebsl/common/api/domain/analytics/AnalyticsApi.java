/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 *
 * Copyright (C) 2026 Victor Riegert and EBSL contributors
 *
 * This file is part of EBSL.
 *
 * EBSL is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * EBSL is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public
 * License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with EBSL. If not, see <https://www.gnu.org/licenses/>.
 */

package fr.riege.ebsl.common.api.domain.analytics;

import fr.riege.ebsl.common.api.core.annotation.EbslApiOperation;
import fr.riege.ebsl.common.api.core.annotation.EbslApiSurface;
import fr.riege.ebsl.common.domain.analytics.AnalyticsEvent;
import fr.riege.ebsl.common.domain.analytics.AnalyticsEventLog;
import fr.riege.ebsl.common.domain.analytics.AnalyticsSnapshot;
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
    public AnalyticsSnapshot snapshot(String selectedModule) {
        return AnalyticsSnapshot.capture(EbslServices.navigation(), selectedModule);
    }
}
