package fr.riege.ebsl.common.feature.scripting.highlight;

public final class EbslSyntaxThemeRegistry {
    private EbslSyntaxThemeRegistry() {
    }

    public static EbslTokenStyle style(EbslTokenKind kind) {
        return EbslCodeEditorSettings.instance().tokenStyle(kind);
    }
}
