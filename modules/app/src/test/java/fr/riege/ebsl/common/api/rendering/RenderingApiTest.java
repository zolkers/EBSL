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

package fr.riege.ebsl.common.api.rendering;

import fr.riege.ebsl.common.api.EbslApi;
import fr.riege.ebsl.common.platform.render.RenderBatch;
import fr.riege.ebsl.common.platform.render.RenderColor;
import fr.riege.ebsl.common.platform.render.RenderPaint;
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

    @Test
    void apiCreatesGradientAndRainbowStyles() {
        RenderBatch gradient = EbslApi.rendering()
            .batch("test:gradient")
            .gradientArgb(0xFFFF0000, 0xFF0000FF)
            .line(0, 0, 0, 1, 1, 1)
            .build();

        RenderBatch rainbow = EbslApi.rendering()
            .batch("test:rainbow")
            .rainbow(0.7f)
            .filledBlock(0, 0, 0)
            .build();

        RenderColor middle = gradient.style().paint().colorAt(0.5f);
        assertTrue(gradient.style().paint() instanceof RenderPaint.Gradient);
        assertTrue(rainbow.style().paint() instanceof RenderPaint.Rainbow);
        assertEquals(0.5f, middle.r(), 0.01f);
        assertEquals(0.5f, middle.b(), 0.01f);
    }
}
