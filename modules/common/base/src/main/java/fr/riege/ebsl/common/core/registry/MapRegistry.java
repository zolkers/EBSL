package fr.riege.ebsl.common.core.registry;

import java.util.LinkedHashMap;

public final class MapRegistry<K, V> extends AbstractRegistry<K, V> {
    public MapRegistry(V fallback) {
        super(new LinkedHashMap<>(), fallback);
    }
}
