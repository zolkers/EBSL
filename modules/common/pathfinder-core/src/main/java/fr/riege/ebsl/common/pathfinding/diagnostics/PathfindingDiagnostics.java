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
package fr.riege.ebsl.common.pathfinding.diagnostics;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BooleanSupplier;
import java.util.function.LongConsumer;

public final class PathfindingDiagnostics {
    private static final BooleanSupplier DISABLED = () -> false;
    private static final LongConsumer NO_EXPLORED_NODE_SINK = ignored -> {};
    private static final TelemetrySink NO_TELEMETRY_SINK = (source, message) -> {};

    private static final AtomicReference<BooleanSupplier> exploredNodeCaptureEnabled = new AtomicReference<>(DISABLED);
    private static final AtomicReference<LongConsumer> exploredNodeSink = new AtomicReference<>(NO_EXPLORED_NODE_SINK);
    private static final AtomicReference<TelemetrySink> telemetrySink = new AtomicReference<>(NO_TELEMETRY_SINK);

    private PathfindingDiagnostics() {
    }

    public static void setExploredNodeSink(BooleanSupplier enabled, LongConsumer sink) {
        exploredNodeCaptureEnabled.set(enabled == null ? DISABLED : enabled);
        exploredNodeSink.set(sink == null ? NO_EXPLORED_NODE_SINK : sink);
    }

    public static boolean shouldCaptureExploredNodes() {
        return exploredNodeCaptureEnabled.get().getAsBoolean();
    }

    public static void recordExploredNode(long packedPosition) {
        exploredNodeSink.get().accept(packedPosition);
    }

    public static void setTelemetrySink(TelemetrySink sink) {
        telemetrySink.set(sink == null ? NO_TELEMETRY_SINK : sink);
    }

    public static void recordTelemetry(String source, String message) {
        telemetrySink.get().accept(source, message);
    }

    /**
     * Accepts pathfinding telemetry messages.

     *

     * <p>Telemetry sinks are lightweight observers used for debug output and profiling traces.</p>

     */
    @FunctionalInterface
    public interface TelemetrySink {
        /**
         * Accepts the supplied diagnostic or callback payload.
 *
         * @param source the diagnostic source name
         * @param message the message to display or record
         */
        void accept(String source, String message);
    }
}
