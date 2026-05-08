package fr.riege.ebsl.common.platform.render;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class RenderingSystem {
    private static final Map<String, ActiveBatch> BATCHES = new LinkedHashMap<>();
    private static volatile boolean enabled = true;

    private RenderingSystem() {
    }

    public static synchronized void submit(RenderBatch batch) {
        if (!enabled) {
            return;
        }
        if (batch == null || batch.primitives().isEmpty()) {
            return;
        }
        BATCHES.put(batch.id(), new ActiveBatch(batch, batch.ttlTicks()));
    }

    public static boolean enabled() {
        return enabled;
    }

    public static synchronized void setEnabled(boolean enabled) {
        RenderingSystem.enabled = enabled;
        if (!enabled) {
            clear();
        }
    }

    public static synchronized boolean remove(String id) {
        return BATCHES.remove(id) != null;
    }

    public static synchronized void clear() {
        BATCHES.clear();
    }

    public static synchronized int batchCount() {
        return BATCHES.size();
    }

    public static synchronized List<RenderBatch> batches() {
        return BATCHES.values().stream()
            .map(ActiveBatch::batch)
            .toList();
    }

    public static synchronized void tick() {
        List<String> expired = new ArrayList<>();
        for (ActiveBatch active : BATCHES.values()) {
            if (active.remainingTicks == RenderBatch.PERSISTENT) {
                continue;
            }
            active.remainingTicks--;
            if (active.remainingTicks <= 0) {
                expired.add(active.batch.id());
            }
        }
        for (String id : expired) {
            BATCHES.remove(id);
        }
    }

    public static void renderWorld(RenderHandle handle) {
        if (!enabled) {
            return;
        }
        Map<RenderStage, List<RenderBatch>> snapshot = snapshotByStage();
        for (RenderStage stage : RenderStage.values()) {
            List<RenderBatch> batches = snapshot.get(stage);
            if (batches == null) {
                continue;
            }
            for (RenderBatch batch : batches) {
                renderBatch(handle, batch);
            }
        }
    }

    private static synchronized Map<RenderStage, List<RenderBatch>> snapshotByStage() {
        Map<RenderStage, List<RenderBatch>> byStage = new EnumMap<>(RenderStage.class);
        for (ActiveBatch active : BATCHES.values()) {
            byStage.computeIfAbsent(active.batch.stage(), ignored -> new ArrayList<>()).add(active.batch);
        }
        return byStage;
    }

    private static void renderBatch(RenderHandle handle, RenderBatch batch) {
        if (canRenderAsSingleSession(batch)) {
            try (WorldRenderSession session = WorldRender.session(handle).ignoreDepth(batch.style().ignoreDepth())) {
                for (RenderPrimitive primitive : batch.primitives()) {
                    primitive.render(session, batch.style());
                }
            }
            return;
        }
        for (RenderPrimitive primitive : batch.primitives()) {
            primitive.render(handle, batch.style());
        }
    }

    private static boolean canRenderAsSingleSession(RenderBatch batch) {
        boolean ignoreDepth = batch.style().ignoreDepth();
        for (RenderPrimitive primitive : batch.primitives()) {
            if (primitive.effectiveStyle(batch.style()).ignoreDepth() != ignoreDepth) {
                return false;
            }
        }
        return true;
    }

    private static final class ActiveBatch {
        private final RenderBatch batch;
        private int remainingTicks;

        private ActiveBatch(RenderBatch batch, int remainingTicks) {
            this.batch = batch;
            this.remainingTicks = remainingTicks;
        }

        private RenderBatch batch() {
            return batch;
        }
    }
}
