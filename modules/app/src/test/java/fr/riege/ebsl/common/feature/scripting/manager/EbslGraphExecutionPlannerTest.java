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
