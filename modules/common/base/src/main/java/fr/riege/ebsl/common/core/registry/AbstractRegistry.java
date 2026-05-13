/*
 * This file is part of EBSL.
 *
 * EBSL is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * EBSL is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package fr.riege.ebsl.common.core.registry;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

abstract class AbstractRegistry<K, V> implements IRegistry<K, V> {
    private final Map<K, V> entries;
    private final V fallback;

    AbstractRegistry(Map<K, V> entries, V fallback) {
        this.entries = entries;
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
