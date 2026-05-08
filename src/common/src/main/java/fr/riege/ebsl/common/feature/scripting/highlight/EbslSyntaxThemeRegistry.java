package fr.riege.ebsl.common.feature.scripting.highlight;

import fr.riege.ebsl.common.core.registry.EnumRegistry;

public final class EbslSyntaxThemeRegistry {
    private static final EnumRegistry<EbslTokenKind, EbslTokenStyle> ACTIVE =
        new EnumRegistry<>(EbslTokenKind.class, new EbslTokenStyle(0xFFE6EDF3));

    static {
        for (EbslTokenKind kind : EbslTokenKind.values()) {
            ACTIVE.register(kind, EbslSyntaxPalette.DARK.style(kind));
        }
    }

    private EbslSyntaxThemeRegistry() {
    }

    public static EbslTokenStyle style(EbslTokenKind kind) {
        return ACTIVE.get(kind);
    }
}
