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

package fr.riege.ebsl.common.feature.scripting.highlight;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EbslSyntaxHighlighterTest {
    @Test
    void classifiesScriptTokens() {
        List<List<EbslSyntaxToken>> lines = EbslSyntaxHighlighter.highlight("""
            forever {
              repeat_until sensor_targeted_block leaf {
                break_block leaf
                wait 2s # cool
              }
            }
            """);

        assertToken(lines.get(0), "forever", EbslTokenKind.CONTROL);
        assertToken(lines.get(0), "{", EbslTokenKind.BLOCK);
        assertToken(lines.get(1), "repeat_until", EbslTokenKind.CONTROL);
        assertToken(lines.get(1), "sensor_targeted_block", EbslTokenKind.SENSOR);
        assertToken(lines.get(2), "break_block", EbslTokenKind.COMMAND);
        assertToken(lines.get(3), "2s", EbslTokenKind.DURATION);
        assertTrue(lines.get(3).stream().anyMatch(token -> token.kind() == EbslTokenKind.COMMENT));
    }

    private static void assertToken(List<EbslSyntaxToken> tokens, String text, EbslTokenKind kind) {
        EbslSyntaxToken token = tokens.stream()
            .filter(candidate -> candidate.text().equals(text))
            .findFirst()
            .orElseThrow();
        assertEquals(kind, token.kind());
    }
}
