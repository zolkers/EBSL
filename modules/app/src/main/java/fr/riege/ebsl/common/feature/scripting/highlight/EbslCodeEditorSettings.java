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

import fr.riege.ebsl.common.core.settings.*;

import java.util.List;
import java.util.Locale;

@SuppressWarnings("java:S6548")
public final class EbslCodeEditorSettings extends Settingable {
    private static final EbslCodeEditorSettings INSTANCE = new EbslCodeEditorSettings();

    public final EnumSetting<EbslCodeEditorPalette> editorTheme = registerSetting(new EnumSetting<>(
        "editor_theme", "Editor theme", EbslCodeEditorPalette.DARK, EbslCodeEditorPalette.class));
    public final BooleanSetting customEditorColors = registerSetting(new BooleanSetting(
        "custom_editor_colors", "Custom editor colors", false));
    public final ColorSetting backgroundColor = registerSetting(new ColorSetting(
        "background_color", "Background", EbslCodeEditorPalette.DARK.backgroundColor()));
    public final ColorSetting borderColor = registerSetting(new ColorSetting(
        "border_color", "Border", EbslCodeEditorPalette.DARK.borderColor()));
    public final ColorSetting gutterColor = registerSetting(new ColorSetting(
        "gutter_color", "Gutter", EbslCodeEditorPalette.DARK.gutterColor()));
    public final ColorSetting nativeTextColor = registerSetting(new ColorSetting(
        "native_text_color", "Native text", EbslCodeEditorPalette.DARK.nativeTextColor()));
    public final ColorSetting frameColor = registerSetting(new ColorSetting(
        "frame_color", "Input frame", EbslCodeEditorPalette.DARK.frameColor()));
    public final ColorSetting caretColor = registerSetting(new ColorSetting(
        "caret_color", "Caret", EbslCodeEditorPalette.DARK.caretColor()));
    public final DoubleSetting caretBlinkSeconds = registerSetting(new DoubleSetting(
        "caret_blink_seconds", "Caret blink seconds", 0.55, 0.0, 3.0));
    public final DoubleSetting caretThickness = registerSetting(new DoubleSetting(
        "caret_thickness", "Caret thickness", 1.4, 0.5, 6.0));
    public final BooleanSetting caretBlink = registerSetting(new BooleanSetting(
        "caret_blink", "Caret blink", true));
    public final DoubleSetting textPadding = registerSetting(new DoubleSetting(
        "text_padding", "Text padding", 8.0, 0.0, 24.0));
    public final EnumSetting<EbslSyntaxPalette> languageTheme = registerSetting(new EnumSetting<>(
        "language_theme", "Language theme", EbslSyntaxPalette.DARK, EbslSyntaxPalette.class));
    public final BooleanSetting customTokenColors = registerSetting(new BooleanSetting(
        "custom_token_colors", "Custom token colors", false));
    public final ColorSetting commandColor = registerSetting(tokenColor(EbslTokenKind.COMMAND, "Command"));
    public final ColorSetting controlColor = registerSetting(tokenColor(EbslTokenKind.CONTROL, "Control"));
    public final ColorSetting sensorColor = registerSetting(tokenColor(EbslTokenKind.SENSOR, "Sensor"));
    public final ColorSetting operatorColor = registerSetting(tokenColor(EbslTokenKind.OPERATOR, "Operator"));
    public final ColorSetting variableColor = registerSetting(tokenColor(EbslTokenKind.VARIABLE, "Variable"));
    public final ColorSetting stringColor = registerSetting(tokenColor(EbslTokenKind.STRING, "String"));
    public final ColorSetting numberColor = registerSetting(tokenColor(EbslTokenKind.NUMBER, "Number"));
    public final ColorSetting durationColor = registerSetting(tokenColor(EbslTokenKind.DURATION, "Duration"));
    public final ColorSetting blockColor = registerSetting(tokenColor(EbslTokenKind.BLOCK, "Block"));
    public final ColorSetting commentColor = registerSetting(tokenColor(EbslTokenKind.COMMENT, "Comment"));
    public final ColorSetting identifierColor = registerSetting(tokenColor(EbslTokenKind.IDENTIFIER, "Identifier"));
    public final ColorSetting whitespaceColor = registerSetting(tokenColor(EbslTokenKind.WHITESPACE, "Whitespace"));

    private EbslCodeEditorSettings() {
    }

    public static EbslCodeEditorSettings instance() {
        return INSTANCE;
    }

    public static List<Setting<?>> all() {
        return INSTANCE.settings();
    }

    public static List<Setting<?>> editorAppearanceSettings() {
        return List.of(
            INSTANCE.editorTheme,
            INSTANCE.customEditorColors,
            INSTANCE.backgroundColor,
            INSTANCE.borderColor,
            INSTANCE.gutterColor,
            INSTANCE.nativeTextColor,
            INSTANCE.frameColor,
            INSTANCE.caretColor,
            INSTANCE.caretBlinkSeconds,
            INSTANCE.caretThickness,
            INSTANCE.caretBlink,
            INSTANCE.textPadding
        );
    }

    public static List<Setting<?>> languageThemeSettings() {
        return List.of(
            INSTANCE.languageTheme,
            INSTANCE.customTokenColors,
            INSTANCE.commandColor,
            INSTANCE.controlColor,
            INSTANCE.sensorColor,
            INSTANCE.operatorColor,
            INSTANCE.variableColor,
            INSTANCE.stringColor,
            INSTANCE.numberColor,
            INSTANCE.durationColor,
            INSTANCE.blockColor,
            INSTANCE.commentColor,
            INSTANCE.identifierColor,
            INSTANCE.whitespaceColor
        );
    }

    public int editorColor(EbslCodeEditorColor color) {
        EbslCodeEditorPalette palette = editorTheme.value();
        if (!Boolean.TRUE.equals(customEditorColors.value())) {
            return switch (color) {
                case BACKGROUND -> palette.backgroundColor();
                case BORDER -> palette.borderColor();
                case GUTTER -> palette.gutterColor();
                case NATIVE_TEXT -> palette.nativeTextColor();
                case FRAME -> palette.frameColor();
                case CARET -> palette.caretColor();
            };
        }
        return switch (color) {
            case BACKGROUND -> backgroundColor.value();
            case BORDER -> borderColor.value();
            case GUTTER -> gutterColor.value();
            case NATIVE_TEXT -> nativeTextColor.value();
            case FRAME -> frameColor.value();
            case CARET -> caretColor.value();
        };
    }

    public EbslTokenStyle tokenStyle(EbslTokenKind kind) {
        if (!Boolean.TRUE.equals(customTokenColors.value())) {
            return languageTheme.value().style(kind);
        }
        return new EbslTokenStyle(tokenColorSetting(kind).value());
    }

    public static void resetToDefaults() {
        INSTANCE.resetSettings();
    }

    private static ColorSetting tokenColor(EbslTokenKind kind, String displayName) {
        return new ColorSetting(
            "token_" + kind.name().toLowerCase(Locale.ROOT) + "_color",
            displayName,
            EbslSyntaxPalette.DARK.style(kind).color()
        );
    }

    private ColorSetting tokenColorSetting(EbslTokenKind kind) {
        return switch (kind) {
            case COMMAND -> commandColor;
            case CONTROL -> controlColor;
            case SENSOR -> sensorColor;
            case OPERATOR -> operatorColor;
            case VARIABLE -> variableColor;
            case STRING -> stringColor;
            case NUMBER -> numberColor;
            case DURATION -> durationColor;
            case BLOCK -> blockColor;
            case COMMENT -> commentColor;
            case IDENTIFIER -> identifierColor;
            case WHITESPACE -> whitespaceColor;
        };
    }

    public enum EbslCodeEditorColor {
        BACKGROUND,
        BORDER,
        GUTTER,
        NATIVE_TEXT,
        FRAME,
        CARET
    }
}
