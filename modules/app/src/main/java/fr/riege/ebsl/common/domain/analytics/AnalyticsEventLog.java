/*
 * This file is part of EBSL.
 *
 * EBSL is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * EBSL is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package fr.riege.ebsl.common.domain.analytics;

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
