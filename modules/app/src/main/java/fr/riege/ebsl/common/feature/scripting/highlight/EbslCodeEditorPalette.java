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

package fr.riege.ebsl.common.feature.scripting.highlight;

public enum EbslCodeEditorPalette {
    DARK(
        0xFF0D1117,
        0xFF26313D,
        0xFF111923,
        0x00FFFFFF,
        0x00000000,
        0xFFE6EDF3
    ),
    LIGHT(
        0xFFF8FAFC,
        0xFFCBD5E1,
        0xFFE2E8F0,
        0x00000000,
        0x00000000,
        0xFF0F172A
    ),
    HIGH_CONTRAST(
        0xFF000000,
        0xFFFFFFFF,
        0xFF111111,
        0x00000000,
        0x00000000,
        0xFFFFFFFF
    );

    private final int backgroundColor;
    private final int borderColor;
    private final int gutterColor;
    private final int nativeTextColor;
    private final int frameColor;
    private final int caretColor;

    EbslCodeEditorPalette(
        int backgroundColor,
        int borderColor,
        int gutterColor,
        int nativeTextColor,
        int frameColor,
        int caretColor
    ) {
        this.backgroundColor = backgroundColor;
        this.borderColor = borderColor;
        this.gutterColor = gutterColor;
        this.nativeTextColor = nativeTextColor;
        this.frameColor = frameColor;
        this.caretColor = caretColor;
    }

    public int backgroundColor() {
        return backgroundColor;
    }

    public int borderColor() {
        return borderColor;
    }

    public int gutterColor() {
        return gutterColor;
    }

    public int nativeTextColor() {
        return nativeTextColor;
    }

    public int frameColor() {
        return frameColor;
    }

    public int caretColor() {
        return caretColor;
    }
}
