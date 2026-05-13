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

import fr.riege.ebsl.common.api.core.annotation.EbslApiOperation;
import fr.riege.ebsl.common.api.core.annotation.EbslApiSurface;
import fr.riege.ebsl.common.platform.render.*;

import java.util.List;

@EbslApiSurface(EbslApiSurface.Domain.RENDERING)
public final class RenderingApi {
    @EbslApiOperation("Create a render batch builder for world-space debug rendering.")
    public RenderBatch.Builder batch(String id) {
        return RenderBatch.builder(id);
    }

    @EbslApiOperation("Create a render style builder.")
    public RenderStyle.Builder style() {
        return RenderStyle.builder();
    }

    @EbslApiOperation("Create a normalized render color from packed ARGB.")
    public RenderColor color(int argb) {
        return RenderColor.argb(argb);
    }

    @EbslApiOperation("Create a solid render paint from packed ARGB.")
    public RenderPaint solid(int argb) {
        return RenderPaint.solid(RenderColor.argb(argb));
    }

    @EbslApiOperation("Create a gradient render paint between two packed ARGB colors.")
    public RenderPaint gradient(int fromArgb, int toArgb) {
        return RenderPaint.gradient(RenderColor.argb(fromArgb), RenderColor.argb(toArgb));
    }

    @EbslApiOperation("Create an animated rainbow render paint.")
    public RenderPaint rainbow() {
        return RenderPaint.rainbow();
    }

    @EbslApiOperation("Create an animated rainbow render paint with alpha.")
    public RenderPaint rainbow(float alpha) {
        return RenderPaint.rainbow(alpha);
    }

    @EbslApiOperation("Submit a render batch for persistent or time-limited drawing.")
    public void submit(RenderBatch batch) {
        RenderingSystem.submit(batch);
    }

    @EbslApiOperation("Draw a render batch for one frame.")
    public void drawOnce(RenderBatch batch) {
        if (batch == null) {
            return;
        }
        RenderingSystem.submit(batch.toBuilder().oneFrame().build());
    }

    @EbslApiOperation("Remove a render batch by id.")
    public boolean remove(String id) {
        return RenderingSystem.remove(id);
    }

    @EbslApiOperation("Clear every API-submitted render batch.")
    public void clear() {
        RenderingSystem.clear();
    }

    @EbslApiOperation("Count currently registered render batches.")
    public int batchCount() {
        return RenderingSystem.batchCount();
    }

    @EbslApiOperation("Read a snapshot of currently registered render batches.")
    public List<RenderBatch> batches() {
        return RenderingSystem.batches();
    }

    @EbslApiOperation("Read the supported render stages in render order.")
    public RenderStage[] stages() {
        return RenderStage.values();
    }
}
