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
package fr.riege.ebsl.common.feature.scripting.highlight;

import fr.riege.ebsl.common.feature.scripting.highlight.EbslCodeEditorSettings.EbslCodeEditorColor;

@SuppressWarnings("java:S6548")
public enum EbslCodeEditorStyle {
    DARK;

    private EbslCodeEditorSettings settings() {
        return EbslCodeEditorSettings.instance();
    }

    public int backgroundColor() {
        return settings().editorColor(EbslCodeEditorColor.BACKGROUND);
    }

    public int borderColor() {
        return settings().editorColor(EbslCodeEditorColor.BORDER);
    }

    public int gutterColor() {
        return settings().editorColor(EbslCodeEditorColor.GUTTER);
    }

    public int editableTextColor() {
        return settings().editorColor(EbslCodeEditorColor.NATIVE_TEXT);
    }

    public int frameColor() {
        return settings().editorColor(EbslCodeEditorColor.FRAME);
    }

    public int caretColor() {
        return settings().editorColor(EbslCodeEditorColor.CARET);
    }

    public double caretBlinkSeconds() {
        return Boolean.TRUE.equals(settings().caretBlink.value()) ? settings().caretBlinkSeconds.value() : 0.0;
    }

    public float caretThickness() {
        return settings().caretThickness.value().floatValue();
    }

    public float textPadding() {
        return settings().textPadding.value().floatValue();
    }
}
