package fr.riege.ebsl.common.core.registry;

import java.util.Collection;
import java.util.Set;

/**
 * Defines the contract for {@code IRegistry} implementations.
 */
public interface IRegistry<K, V> {
    V get(K key);

    void register(K key, V value);

    boolean contains(K key);

    Collection<V> values();

    Set<K> keys();

    boolean isEmpty();
}
