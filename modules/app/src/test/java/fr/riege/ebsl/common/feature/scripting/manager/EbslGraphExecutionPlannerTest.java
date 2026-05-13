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
package fr.riege.ebsl.common.feature.scripting.manager;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class EbslGraphExecutionPlannerTest {
    @Test
    void ordersFlatScriptFromGraphConnections() {
        EbslGraphDocument graph = new EbslGraphDocument(
            Map.of(),
            List.of(
                new EbslGraphConnection("main.ebsl:3", "main.ebsl:1"),
                new EbslGraphConnection("main.ebsl:1", "main.ebsl:2")
            )
        );

        String planned = EbslGraphExecutionPlanner.plan("main.ebsl", """
            message third
            message second
            message first
            """, graph);

        assertEquals("""
            message first
            message third
            message second
            """, planned);
    }

    @Test
    void appendsDisconnectedNodesAfterGraphFlow() {
        EbslGraphDocument graph = new EbslGraphDocument(
            Map.of(),
            List.of(new EbslGraphConnection("main.ebsl:2", "main.ebsl:1"))
        );

        String planned = EbslGraphExecutionPlanner.plan("main.ebsl", """
            message second
            message first
            message loose
            """, graph);

        assertEquals("""
            message first
            message second
            message loose
            """, planned);
    }

    @Test
    void supportsMultipleFlowInputsToSameNodeWithoutDuplicatingIt() {
        EbslGraphDocument graph = new EbslGraphDocument(
            Map.of(),
            List.of(
                new EbslGraphConnection("main.ebsl:1", "main.ebsl:3"),
                new EbslGraphConnection("main.ebsl:2", "main.ebsl:3")
            )
        );

        String planned = EbslGraphExecutionPlanner.plan("main.ebsl", """
            message first
            message second
            message merged
            """, graph);

        assertEquals("""
            message first
            message second
            message merged
            """, planned);
    }

    @Test
    void eachInputLinksMaterializeExtraExecutionWithoutGeneratedComments() {
        EbslGraphDocument graph = new EbslGraphDocument(
            Map.of(),
            List.of(new EbslGraphConnection(
                "edge-a",
                "main.ebsl:1",
                "main.ebsl:3",
                EbslGraphConnectionMode.EACH_INPUT,
                "retry"
            ))
        );

        String planned = EbslGraphExecutionPlanner.plan("main.ebsl", """
            message first
            message second
            message target
            """, graph);

        assertEquals("""
            message first
            message target
            message second
            message target
            """, planned);
    }

    @Test
    void keepsBlockScriptsTextOrdered() {
        EbslGraphDocument graph = new EbslGraphDocument(
            Map.of(),
            List.of(new EbslGraphConnection("main.ebsl:3", "main.ebsl:1"))
        );
        String source = """
            repeat 2 {
              message loop
            }
            message done
            """;

        assertEquals(source, EbslGraphExecutionPlanner.plan("main.ebsl", source, graph));
    }
}
