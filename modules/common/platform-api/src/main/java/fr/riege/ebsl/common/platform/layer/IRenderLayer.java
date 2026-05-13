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
package fr.riege.ebsl.common.platform.layer;

import fr.riege.ebsl.common.platform.render.RenderHandle;

/**
 * Begins and ends world-rendering frames for shared rendering APIs.
 *
 * <p>The layer binds camera and matrix state before primitives are emitted through the inherited render handle.</p>
 */
public interface IRenderLayer extends RenderHandle {
    /**
     * Begins a world-render frame using the supplied camera and matrix state.
 *
     * @param camX the camera x coordinate
     * @param camY the camera y coordinate
     * @param camZ the camera z coordinate
     * @param viewMatrix the active view matrix
     * @param projMatrix the active projection matrix
     */
    void beginFrame(double camX, double camY, double camZ, float[] viewMatrix, float[] projMatrix);
    /**
     * Ends the active world-render frame and flushes pending primitives.
     */
    void endFrame();
}
