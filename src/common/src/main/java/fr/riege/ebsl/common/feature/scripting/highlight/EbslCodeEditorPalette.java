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
