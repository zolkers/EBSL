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

    @FunctionalInterface
    public interface TelemetrySink {
        void accept(String source, String message);
    }
}
