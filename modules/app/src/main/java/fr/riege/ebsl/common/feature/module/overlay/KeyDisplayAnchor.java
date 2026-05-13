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

package fr.riege.ebsl.common.feature.module.overlay;

import fr.riege.ebsl.common.feature.ui.layout.UiRect;

public enum KeyDisplayAnchor {
    TOP_LEFT("Top left"),
    TOP_CENTER("Top center"),
    TOP_RIGHT("Top right"),
    BOTTOM_LEFT("Bottom left"),
    BOTTOM_CENTER("Bottom center"),
    BOTTOM_RIGHT("Bottom right");

    private final String label;

    KeyDisplayAnchor(String label) {
        this.label = label;
    }

    public float x(UiRect viewport, float width, float pad) {
        return switch (this) {
            case TOP_LEFT, BOTTOM_LEFT -> viewport.x() + pad;
            case TOP_CENTER, BOTTOM_CENTER -> viewport.x() + (viewport.width() - width) * 0.5f;
            case TOP_RIGHT, BOTTOM_RIGHT -> viewport.right() - width - pad;
        };
    }

    public float y(UiRect viewport, float height, float pad) {
        return switch (this) {
            case TOP_LEFT, TOP_CENTER, TOP_RIGHT -> viewport.y() + pad;
            case BOTTOM_LEFT, BOTTOM_CENTER, BOTTOM_RIGHT -> viewport.bottom() - height - pad;
        };
    }

    @Override
    public String toString() {
        return label;
    }
}
