package fr.riege.ebsl.common.api.rendering;

import fr.riege.ebsl.common.api.EbslApi;
import fr.riege.ebsl.common.platform.render.RenderBatch;
import fr.riege.ebsl.common.platform.render.RenderingSystem;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class RenderingApiTest {
    @AfterEach
    void clearRendering() {
        RenderingSystem.clear();
    }

    @Test
    void apiSubmitsAndRemovesNamedBatches() {
        RenderBatch batch = EbslApi.rendering()
            .batch("test:cube")
            .argb(0x80FF0000)
            .filledBlock(1, 2, 3)
            .build();

        EbslApi.rendering().submit(batch);

        assertEquals(1, EbslApi.rendering().batchCount());
        assertEquals("test:cube", EbslApi.rendering().batches().getFirst().id());
        assertTrue(EbslApi.rendering().remove("test:cube"));
        assertEquals(0, EbslApi.rendering().batchCount());
    }

    @Test
    void oneFrameBatchesExpireOnTick() {
        EbslApi.rendering()
            .batch("test:once")
            .filledBlock(0, 0, 0)
            .drawOnce();

        assertEquals(1, EbslApi.rendering().batchCount());
        RenderingSystem.tick();
        assertEquals(0, EbslApi.rendering().batchCount());
    }
}
