package fr.riege.ebsl.common.feature.scripting.highlight;

public enum EbslCodeEditorStyle {
    DARK(
        0x00FFFFFF,
        0x00000000,
        8.0f
    );

    private final int editableTextColor;
    private final int frameColor;
    private final float textPadding;

    EbslCodeEditorStyle(int editableTextColor, int frameColor, float textPadding) {
        this.editableTextColor = editableTextColor;
        this.frameColor = frameColor;
        this.textPadding = textPadding;
    }

    public int editableTextColor() {
        return editableTextColor;
    }

    public int frameColor() {
        return frameColor;
    }

    public float textPadding() {
        return textPadding;
    }
}
