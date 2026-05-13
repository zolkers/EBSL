package fr.riege.ebsl.common.core.registry;

import java.util.Collection;
import java.util.Set;

/**
 * Defines a keyed registry abstraction used by small domain catalogs.
 *
 * <p>Registries provide deterministic lookup-related operations while hiding the storage structure selected by each implementation.</p>
 */
public interface IRegistry<K, V> {
    V get(K key);

    /**
     * Registers the supplied value with this component.
 *
     * @param key the storage or registry key
     * @param value the value to apply
     */
    void register(K key, V value);

    /**
     * Returns whether the registry contains a value for the supplied key.
 *
     * @param key the storage or registry key
     * @return true when the condition is satisfied; false otherwise
     */
    boolean contains(K key);

    /**
     * Returns all values currently registered.
 *
     * @return the requested values
     */
    Collection<V> values();

    /**
     * Returns all keys currently registered.
 *
     * @return the requested values
     */
    Set<K> keys();

    /**
     * Returns whether empty is true for the current state.
 *
     * @return true when the condition is satisfied; false otherwise
     */
    boolean isEmpty();
}
