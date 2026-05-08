package fr.riege.ebsl.common.feature.scripting.highlight;

public enum EbslCodeEditorStyle {
    DARK(
        0x00FFFFFF,
        0x00000000,
        16.0f,
        7.2f,
        8.0f
    );

    private final int editableTextColor;
    private final int frameColor;
    private final float lineHeight;
    private final float characterWidth;
    private final float textPadding;

    EbslCodeEditorStyle(int editableTextColor, int frameColor, float lineHeight, float characterWidth, float textPadding) {
        this.editableTextColor = editableTextColor;
        this.frameColor = frameColor;
        this.lineHeight = lineHeight;
        this.characterWidth = characterWidth;
        this.textPadding = textPadding;
    }

    public int editableTextColor() {
        return editableTextColor;
    }

    public int frameColor() {
        return frameColor;
    }

    public float lineHeight() {
        return lineHeight;
    }

    public float characterWidth() {
        return characterWidth;
    }

    public float textPadding() {
        return textPadding;
    }
}
