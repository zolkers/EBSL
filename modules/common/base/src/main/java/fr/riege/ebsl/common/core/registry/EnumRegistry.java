package fr.riege.ebsl.common.core.registry;

import java.util.EnumMap;

public final class EnumRegistry<K extends Enum<K>, V> extends AbstractRegistry<K, V> {
    public EnumRegistry(Class<K> keyType, V fallback) {
        super(new EnumMap<>(keyType), fallback);
    }
}
