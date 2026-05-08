package fr.riege.ebsl.common.feature.ui.imgui.graph;

import fr.riege.ebsl.common.feature.scripting.manager.EbslGraphNodePosition;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EbslScriptGraphParserTest {
    @Test
    void assignsBlockDepthsForNestedScripts() {
        List<EbslScriptGraphNode> nodes = EbslScriptGraphParser.parse("""
            start
            forever {
              goal_nearest_block minecraft:oak_leaves 32 4
              repeat_until sensor_targeted_block minecraft:oak_leaves {
                aim_at_block minecraft:oak_leaves 32 6t
                wait 1t
              }
              break_block minecraft:oak_leaves
            }
            """, line -> "main.ebsl:" + line);

        assertEquals(7, nodes.size());
        assertEquals(0, nodes.get(0).depth());
        assertEquals(0, nodes.get(1).depth());
        assertEquals(1, nodes.get(2).depth());
        assertEquals(1, nodes.get(3).depth());
        assertEquals(2, nodes.get(4).depth());
        assertEquals(2, nodes.get(5).depth());
        assertEquals(1, nodes.get(6).depth());
        assertTrue(nodes.get(1).blockStart());
        assertTrue(nodes.get(3).blockStart());
    }

    @Test
    void indentsAutoLayoutByBlockDepth() {
        List<EbslScriptGraphNode> nodes = EbslScriptGraphParser.parse("""
            forever {
              repeat_until sensor_targeted_block minecraft:oak_leaves {
                aim_at_block minecraft:oak_leaves 32 6t
              }
            }
            """, line -> "main.ebsl:" + line);

        Map<String, EbslGraphNodePosition> positions = EbslGraphAutoLayout.layout(nodes);

        assertTrue(positions.get(nodes.get(1).key()).x() > positions.get(nodes.get(0).key()).x());
        assertTrue(positions.get(nodes.get(2).key()).x() > positions.get(nodes.get(1).key()).x());
        assertTrue(positions.get(nodes.get(2).key()).y() > positions.get(nodes.get(1).key()).y());
    }
}
