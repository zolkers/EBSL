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

package fr.riege.ebsl.common.feature.scripting.builder;

import fr.riege.ebsl.common.feature.scripting.manager.EbslGraphDocument;
import fr.riege.ebsl.common.feature.scripting.manager.EbslGraphPort;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EbslGraphScriptBuilderTest {
    @Test
    void buildsGraphScriptFromJavaApi() {
        EbslGraphDocument document = EbslGraphScriptBuilder.graph()
            .node("start", "start")
            .outputs(EbslGraphPort.output("success", "Success"))
            .add()
            .node("walk", "walk")
            .field("duration", 20)
            .add()
            .connect("start", "success", "walk", "main")
            .build();

        assertEquals(2, document.nodes().size());
        assertEquals("20", document.nodes().get("walk").fields().get("duration"));
        assertEquals("success", document.connections().getFirst().fromPort());
    }

    @Test
    void rejectsDuplicateNodeIds() {
        EbslGraphScriptBuilder builder = EbslGraphScriptBuilder.graph()
            .node("start", "start")
            .add();

        assertThrows(IllegalArgumentException.class, () -> builder.node("start", "wait").add());
    }
}
