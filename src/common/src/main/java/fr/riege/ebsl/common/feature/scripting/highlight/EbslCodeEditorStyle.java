package fr.riege.ebsl.common.feature.scripting.highlight;

public enum EbslCodeEditorStyle {
    DARK;

    private EbslCodeEditorSettings settings() {
        return EbslCodeEditorSettings.instance();
    }

    public int backgroundColor() {
        return settings().backgroundColor.value();
    }

    public int borderColor() {
        return settings().borderColor.value();
    }

    public int gutterColor() {
        return settings().gutterColor.value();
    }

    public int editableTextColor() {
        return settings().nativeTextColor.value();
    }

    public int frameColor() {
        return settings().frameColor.value();
    }

    public int caretColor() {
        return settings().caretColor.value();
    }

    public double caretBlinkSeconds() {
        return settings().caretBlink.value() ? settings().caretBlinkSeconds.value() : 0.0;
    }

    public float caretThickness() {
        return settings().caretThickness.value().floatValue();
    }

    public float textPadding() {
        return settings().textPadding.value().floatValue();
    }
}
