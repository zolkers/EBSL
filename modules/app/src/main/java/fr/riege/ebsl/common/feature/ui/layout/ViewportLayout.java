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

package fr.riege.ebsl.common.feature.ui.layout;

public record ViewportLayout(UiRect header, UiRect left, UiRect center, UiRect right, UiRect bottom) {
    public static ViewportLayout create(int width, int height) {
        int bottomY = Math.max(UiTheme.HEADER_H + 80, height - UiTheme.BOTTOM_H);
        int rightX = Math.max(UiTheme.LEFT_W + 120, width - UiTheme.RIGHT_W);
        return new ViewportLayout(
            new UiRect(0, 0, width, UiTheme.HEADER_H),
            new UiRect(0, UiTheme.HEADER_H, UiTheme.LEFT_W, bottomY - UiTheme.HEADER_H),
            new UiRect(UiTheme.LEFT_W, UiTheme.HEADER_H, rightX - UiTheme.LEFT_W, bottomY - UiTheme.HEADER_H),
            new UiRect(rightX, UiTheme.HEADER_H, width - rightX, bottomY - UiTheme.HEADER_H),
            new UiRect(0, bottomY, width, height - bottomY));
    }
}
