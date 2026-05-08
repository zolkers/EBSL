package fr.riege.ebsl.common.feature.scripting.highlight;

import fr.riege.ebsl.common.core.registry.EnumRegistry;

public enum EbslSyntaxPalette {
    DARK;

    private final EnumRegistry<EbslTokenKind, EbslTokenStyle> styles =
        new EnumRegistry<>(EbslTokenKind.class, new EbslTokenStyle(0xFFE6EDF3));

    EbslSyntaxPalette() {
        register(EbslTokenKind.COMMAND, 0xFF7DD3FC);
        register(EbslTokenKind.CONTROL, 0xFFC084FC);
        register(EbslTokenKind.SENSOR, 0xFF86EFAC);
        register(EbslTokenKind.OPERATOR, 0xFFFBBF24);
        register(EbslTokenKind.VARIABLE, 0xFFF472B6);
        register(EbslTokenKind.STRING, 0xFFA7F3D0);
        register(EbslTokenKind.NUMBER, 0xFFFCA5A5);
        register(EbslTokenKind.DURATION, 0xFFFDBA74);
        register(EbslTokenKind.BLOCK, 0xFF93C5FD);
        register(EbslTokenKind.COMMENT, 0xFF64748B);
        register(EbslTokenKind.IDENTIFIER, 0xFFE5E7EB);
        register(EbslTokenKind.WHITESPACE, 0x00000000);
    }

    public EbslTokenStyle style(EbslTokenKind kind) {
        return styles.get(kind);
    }

    private void register(EbslTokenKind kind, int color) {
        styles.register(kind, new EbslTokenStyle(color));
    }
}
