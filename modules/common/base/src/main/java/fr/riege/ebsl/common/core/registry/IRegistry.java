/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 *
 * Copyright (C) 2026 Victor Riegert and EBSL contributors
 *
 * This file is part of EBSL.
 *
 * EBSL is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * EBSL is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public
 * License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with EBSL. If not, see <https://www.gnu.org/licenses/>.
 */

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
