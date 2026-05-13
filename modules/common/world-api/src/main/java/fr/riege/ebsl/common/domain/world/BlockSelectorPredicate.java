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
package fr.riege.ebsl.common.domain.world;

/**
 * Tests whether a block identifier matches a selector rule.
 *
 * <p>Predicates are pure matching functions used by block selectors and registries.</p>
 */
@FunctionalInterface
public interface BlockSelectorPredicate {
    /**
     * Returns whether the supplied value satisfies this predicate.
 *
     * @param id the block or entity identifier
     * @return true when the condition is satisfied; false otherwise
     */
    boolean matches(BlockId id);
}
