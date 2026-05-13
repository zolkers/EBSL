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

import fr.riege.ebsl.common.core.registry.MapRegistry;

import java.util.Locale;

public final class BlockGroupRegistry {
    private static final MapRegistry<String, BlockMatcher> GROUPS = new MapRegistry<>(null);

    static {
        register(BlockGroupType.LEAF, BlockGroupRegistry::isLeaf);
        register(BlockGroupType.WOOD, BlockGroupRegistry::isWood);
        register(BlockGroupType.GRASS, BlockGroupRegistry::isGrass);
    }

    private BlockGroupRegistry() {
    }

    public static boolean matches(BlockId id, String token) {
        return BlockSelectorPredicateRegistry.matches(id, token);
    }

    private static void register(BlockGroupType type, BlockMatcher matcher) {
        for (String token : type.tokens()) {
            GROUPS.register(normalize(token), matcher);
        }
    }

    public static boolean isLeaf(BlockId id) {
        String path = path(id);
        return path.endsWith("_leaves") || path.equals("azalea_leaves") || path.equals("flowering_azalea_leaves");
    }

    public static boolean isWood(BlockId id) {
        String path = path(id);
        return path.endsWith("_log")
            || path.endsWith("_wood")
            || path.endsWith("_stem")
            || path.endsWith("_hyphae")
            || path.endsWith("_stripped_log")
            || path.endsWith("_stripped_wood")
            || path.endsWith("_stripped_stem")
            || path.endsWith("_stripped_hyphae")
            || path.startsWith("stripped_");
    }

    public static boolean isGrass(BlockId id) {
        String path = path(id);
        return path.equals("grass")
            || path.equals("short_grass")
            || path.equals("tall_grass")
            || path.equals("fern")
            || path.equals("large_fern")
            || path.equals("seagrass")
            || path.equals("tall_seagrass");
    }

    private static String path(BlockId id) {
        return id == null ? "" : id.path().toLowerCase(Locale.ROOT);
    }

    private static String normalize(String token) {
        return token == null ? "" : token.trim().toLowerCase(Locale.ROOT).replace('-', '_');
    }

    /**
     * Tests whether a block identifier belongs to a registered block group.

     *

     * <p>Matchers keep group membership logic isolated from selector parsing.</p>

     */
    @FunctionalInterface
    private interface BlockMatcher {
        /**
         * Returns whether the supplied value satisfies this predicate.
 *
         * @param id the block or entity identifier
         * @return true when the condition is satisfied; false otherwise
         */
        boolean matches(BlockId id);
    }
}
