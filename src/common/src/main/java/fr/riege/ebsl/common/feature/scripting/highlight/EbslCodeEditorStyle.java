package fr.riege.ebsl.common.feature.scripting.highlight;

public enum EbslCodeEditorStyle {
    DARK(
        0x00FFFFFF,
        0x00000000,
        0xFFE6EDF3,
        0.55,
        8.0f
    );

    private final int editableTextColor;
    private final int frameColor;
    private final int caretColor;
    private final double caretBlinkSeconds;
    private final float textPadding;

    EbslCodeEditorStyle(int editableTextColor, int frameColor, int caretColor, double caretBlinkSeconds, float textPadding) {
        this.editableTextColor = editableTextColor;
        this.frameColor = frameColor;
        this.caretColor = caretColor;
        this.caretBlinkSeconds = caretBlinkSeconds;
        this.textPadding = textPadding;
    }

    public int editableTextColor() {
        return editableTextColor;
    }

    public int frameColor() {
        return frameColor;
    }

    public int caretColor() {
        return caretColor;
    }

    public double caretBlinkSeconds() {
        return caretBlinkSeconds;
    }

    public float textPadding() {
        return textPadding;
    }
}
