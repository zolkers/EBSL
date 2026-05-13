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

package fr.riege.ebsl.common.domain.world;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BlockSelectorTest {
    @ParameterizedTest
    @CsvSource({
        "leaf|wood,minecraft:oak_leaves,true",
        "leaf|wood,minecraft:birch_log,true",
        "leaf|wood,minecraft:stone,false",
        "wood&!crimson_stem,minecraft:birch_log,true",
        "wood&!crimson_stem,minecraft:crimson_stem,false",
        "minecraft:oak_leaves,minecraft:oak_leaves,true",
        "leaves,minecraft:oak_leaves,true",
        "minecraft:leaves,minecraft:oak_leaves,false",
        "stone|wood&!crimson_stem,minecraft:stone,true",
        "stone|wood&!crimson_stem,minecraft:birch_log,true",
        "stone|wood&!crimson_stem,minecraft:crimson_stem,false",
        "'  OAK-LEAVES  |  birch-log  ',minecraft:oak_leaves,true",
        "'  OAK-LEAVES  |  birch-log  ',minecraft:birch_log,true",
        "'  OAK-LEAVES  |  birch-log  ',minecraft:stone,false",
        "'',minecraft:stone,false",
        "!,minecraft:stone,false",
        "wood&,minecraft:oak_log,false"
    })
    void evaluatesSelectors(String selectorText, String blockId, boolean expected) {
        boolean matches = BlockSelector.parse(selectorText).matches(BlockId.of(blockId));
        if (expected) {
            assertTrue(matches);
        } else {
            assertFalse(matches);
        }
    }
}
