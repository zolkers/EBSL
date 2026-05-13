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

package fr.riege.ebsl.common.core.log;

import java.util.List;

public final class AppLog {
    private static final int MAX_ENTRIES = 1000;
    private static final BoundedDirtyLog<LogEntry> LOG = new BoundedDirtyLog<>(MAX_ENTRIES);
    private static Appender appender;

    private AppLog() {}

    public record LogEntry(String time, AppLogLevel level, String logger, String text) {}

    /**
     * Connects an external log sink to the application log stream.

     *

     * <p>Appenders receive a receiver callback during bootstrap and are responsible for forwarding entries without owning log storage.</p>

     */
    @FunctionalInterface
    public interface Appender {
        /**
         * Bootstraps the appender with the receiver used to publish log entries.
 *
         * @param receiver the receiver value
         */
        void bootstrap(Receiver receiver);
    }

    /**
     * Receives immutable application log entries.

     *

     * <p>Receivers are callback targets used by appenders, tests, and UI log views.</p>

     */
    @FunctionalInterface
    public interface Receiver {
        /**
         * Receives one log entry from the application log stream.
 *
         * @param entry the entry value
         */
        void receive(LogEntry entry);
    }

    public static void setAppender(Appender a) {
        appender = a;
    }

    public static void bootstrap() {
        if (appender != null) {
            appender.bootstrap(AppLog::add);
        }
    }

    public static void clear() {
        LOG.clear();
    }

    public static List<LogEntry> snapshot() {
        return LOG.snapshot();
    }

    public static boolean consumeDirty() {
        return LOG.consumeDirty();
    }

    private static void add(LogEntry entry) {
        LOG.add(entry);
    }
}
