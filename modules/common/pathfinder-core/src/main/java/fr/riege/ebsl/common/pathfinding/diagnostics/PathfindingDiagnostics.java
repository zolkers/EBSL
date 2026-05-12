package fr.riege.ebsl.common.pathfinding.diagnostics;

import java.util.function.BooleanSupplier;
import java.util.function.LongConsumer;

public final class PathfindingDiagnostics {
    private static final BooleanSupplier DISABLED = () -> false;
    private static final LongConsumer NO_EXPLORED_NODE_SINK = ignored -> {};
    private static final TelemetrySink NO_TELEMETRY_SINK = (source, message) -> {};

    private static volatile BooleanSupplier exploredNodeCaptureEnabled = DISABLED;
    private static volatile LongConsumer exploredNodeSink = NO_EXPLORED_NODE_SINK;
    private static volatile TelemetrySink telemetrySink = NO_TELEMETRY_SINK;

    private PathfindingDiagnostics() {
    }

    public static void setExploredNodeSink(BooleanSupplier enabled, LongConsumer sink) {
        exploredNodeCaptureEnabled = enabled == null ? DISABLED : enabled;
        exploredNodeSink = sink == null ? NO_EXPLORED_NODE_SINK : sink;
    }

    public static boolean shouldCaptureExploredNodes() {
        return exploredNodeCaptureEnabled.getAsBoolean();
    }

    public static void recordExploredNode(long packedPosition) {
        exploredNodeSink.accept(packedPosition);
    }

    public static void setTelemetrySink(TelemetrySink sink) {
        telemetrySink = sink == null ? NO_TELEMETRY_SINK : sink;
    }

    public static void recordTelemetry(String source, String message) {
        telemetrySink.record(source, message);
    }

    @FunctionalInterface
    public interface TelemetrySink {
        void record(String source, String message);
    }
}
