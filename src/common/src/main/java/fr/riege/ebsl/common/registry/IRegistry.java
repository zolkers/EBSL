package fr.riege.ebsl.common.registry;

import java.util.Collection;
import java.util.Set;

public interface IRegistry<K, V> {
    V get(K key);

    void register(K key, V value);

    boolean contains(K key);

    Collection<V> values();

    Set<K> keys();

    boolean isEmpty();
}
