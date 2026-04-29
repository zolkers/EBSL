package fr.riege.ebsl.registry;

import java.util.Collection;
import java.util.EnumMap;
import java.util.Map;
import java.util.Set;

public final class EnumRegistry<K extends Enum<K>, V> implements IRegistry<K, V> {
    private final Map<K, V> entries;
    private final V fallback;

    public EnumRegistry(Class<K> keyType, V fallback) {
        this.entries = new EnumMap<>(keyType);
        this.fallback = fallback;
    }

    @Override
    public V get(K key) {
        return entries.getOrDefault(key, fallback);
    }

    @Override
    public void register(K key, V value) {
        V previous = entries.putIfAbsent(key, value);
        if (previous != null) {
            throw new IllegalStateException("Duplicate registry key: " + key);
        }
    }

    @Override
    public boolean contains(K key) {
        return entries.containsKey(key);
    }

    @Override
    public Collection<V> values() {
        return entries.values();
    }

    @Override
    public Set<K> keys() {
        return Set.copyOf(entries.keySet());
    }

    @Override
    public boolean isEmpty() {
        return entries.isEmpty();
    }
}
