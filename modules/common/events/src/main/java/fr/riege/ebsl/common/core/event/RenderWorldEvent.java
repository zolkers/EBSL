/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 *
 * Copyright (C) 2026 Victor Riegert and EBSL contributors
 *
 * This file is part of EBSL.
 *
 * EBSL is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * EBSL is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public
 * License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with EBSL. If not, see <https://www.gnu.org/licenses/>.
 */

package fr.riege.ebsl.common.core.event;

import java.util.Arrays;

public record RenderWorldEvent(
        float[] viewMatrix,
        float[] projMatrix,
        float tickDelta,
        double camX,
        double camY,
        double camZ
) {

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof RenderWorldEvent(
                float[] matrix, float[] projMatrix1, float delta, double x, double y, double z
        ))) {
            return false;
        }

        return Arrays.equals(viewMatrix, matrix)
                && Arrays.equals(projMatrix, projMatrix1)
                && Float.compare(tickDelta, delta) == 0
                && Double.compare(camX, x) == 0
                && Double.compare(camY, y) == 0
                && Double.compare(camZ, z) == 0;
    }

    @Override
    public int hashCode() {
        int result = Arrays.hashCode(viewMatrix);
        result = 31 * result + Arrays.hashCode(projMatrix);
        result = 31 * result + Float.hashCode(tickDelta);
        result = 31 * result + Double.hashCode(camX);
        result = 31 * result + Double.hashCode(camY);
        result = 31 * result + Double.hashCode(camZ);
        return result;
    }

    @Override
    public String toString() {
        return "RenderWorldEvent[" +
                "viewMatrix=" + Arrays.toString(viewMatrix) +
                ", projMatrix=" + Arrays.toString(projMatrix) +
                ", tickDelta=" + tickDelta +
                ", camX=" + camX +
                ", camY=" + camY +
                ", camZ=" + camZ +
                ']';
    }
}