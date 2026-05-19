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

package fr.riege.ebsl.common.feature.scripting.manager;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class EbslGraphScriptLinksTest {
    @Test
    void parsesEscapedLabelsAndIgnoresInvalidDirectives() {
        List<EbslGraphConnection> connections = EbslGraphScriptLinks.parse("main.ebsl", """
            # @link nope
            # @link 1 -> 1 mode=flow
            # @link 1 -> 2 mode=each_input label="retry \\"quoted\\" branch"
            # @link 2 -> x mode=flow
            """);

        assertEquals(List.of(new EbslGraphConnection(
            "main.ebsl:1",
            "main.ebsl:2",
            EbslGraphConnectionMode.EACH_INPUT,
            "retry \"quoted\" branch"
        )), connections);
    }

    @Test
    void syncRemovesStaleDirectiveBlockWhenNoConnectionsRemain() {
        String synced = EbslGraphScriptLinks.sync("main.ebsl", """
            message first

            # Graph links
            # @link 1 -> 2 mode=flow
            """, List.of());

        assertEquals("message first", synced);
    }
}
