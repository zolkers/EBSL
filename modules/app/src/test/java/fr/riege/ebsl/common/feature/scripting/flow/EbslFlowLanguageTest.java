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

package fr.riege.ebsl.common.feature.scripting.flow;

import fr.riege.ebsl.common.feature.scripting.manager.EbslGraphDocument;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

final class EbslFlowLanguageTest {
    @Test
    void parsesGraphNativeWorkflowSource() {
        EbslGraphDocument document = EbslFlowLanguage.parse("""
            workflow mine {
              node start start
              node choose switch {
                output = "mine";
                output mine;
                output fight;
              }
              node mine break_block {
                block = "minecraft:stone";
              }
              connect start -> choose;
              connect choose.mine -> mine;
            }
            """);

        assertEquals(3, document.nodes().size());
        assertEquals("minecraft:stone", document.nodes().get("mine").fields().get("block"));
        assertEquals("mine", document.connections().get(1).fromPort());
    }

    @Test
    void writesRoundTrippableWorkflowSource() {
        EbslGraphDocument first = EbslFlowLanguage.parse("""
            node start start
            node message message { text = "hello world"; }
            connect start -> message;
            """);

        EbslGraphDocument second = EbslFlowLanguage.parse(EbslFlowLanguage.write(first));

        assertEquals(first.nodes(), second.nodes());
        assertEquals(first.connections(), second.connections());
    }

    @Test
    void reportsInvalidWorkflowSourceWithLineNumbers() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> EbslFlowLanguage.parse("""
            workflow bad {
              node only
            }
            """));

        assertEquals("Expected node type at line 3", exception.getMessage());
    }
}
