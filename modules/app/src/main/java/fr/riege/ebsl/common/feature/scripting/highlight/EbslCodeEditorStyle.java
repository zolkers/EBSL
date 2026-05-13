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
