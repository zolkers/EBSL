package fr.riege.ebsl.common.pathfinding.parkour;

import fr.riege.ebsl.common.analytics.AnalyticsEventLog;
import fr.riege.ebsl.common.pathfinding.wrapper.PathPosition;

import java.util.Locale;

public final class ParkourEvaluationTelemetry {
    private static final long MIN_INTERVAL_MS = 75;
    private static long lastRecordTime;

    private ParkourEvaluationTelemetry() {
    }

    public static void record(PathPosition from, PathPosition to, ParkourJumpPlan plan) {
        if (plan == null || plan.feasible()) {
            return;
        }
        long now = System.currentTimeMillis();
        if (now - lastRecordTime < MIN_INTERVAL_MS) {
            return;
        }
        lastRecordTime = now;

        int dx = to.flooredX() - from.flooredX();
        int dz = to.flooredZ() - from.flooredZ();
        int offset = ParkourGeometry.distanceBlocks(dx, dz);
        int gap = ParkourGeometry.isDiagonal(dx, dz)
            ? ParkourGeometry.diagonalGapBlocks(dx, dz)
            : ParkourGeometry.cardinalGapBlocks(dx, dz);

        String detail = plan.detail() == null || plan.detail().isBlank() ? "" : " detail=" + plan.detail();
        String message = String.format(Locale.ROOT,
            "%d,%d,%d -> %d,%d,%d gap=%d off=%d dy=%.2f approach=%d req=%.2f est=%.2f reason=%s%s",
            from.flooredX(), from.flooredY(), from.flooredZ(),
            to.flooredX(), to.flooredY(), to.flooredZ(),
            gap,
            offset,
            plan.verticalDelta(),
            plan.approachBlocks(),
            plan.requiredReach(),
            plan.estimatedReach(),
            plan.reason(),
            detail);
        AnalyticsEventLog.record("parkour-eval", message);
    }
}
