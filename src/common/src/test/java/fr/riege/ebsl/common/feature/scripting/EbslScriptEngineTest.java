package fr.riege.ebsl.common.feature.scripting;

import fr.riege.ebsl.common.feature.scripting.enums.EbslNodeType;
import fr.riege.ebsl.common.feature.scripting.parser.EbslProgram;
import fr.riege.ebsl.common.feature.scripting.registry.EbslNodeRegistry;
import fr.riege.ebsl.common.feature.scripting.runtime.EbslScriptEngine;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EbslScriptEngineTest {
    @Test
    void compilesNestedFlowAndFunctions() {
        EbslProgram program = EbslScriptEngine.compile("""
            start
            set score 0
            repeat 3 {
              change score 1
            }
            if $score > 2 {
              event_call done
            } else {
              stop_chain
            }
            event_function done {
              message "finished"
            }
            """);

        assertFalse(program.statements().isEmpty());
        assertTrue(program.functions().containsKey("done"));
    }

    @Test
    void keepsPathmindNodeCatalogueAvailableAsTaskIds() {
        assertEquals(EbslNodeType.GOTO, EbslNodeType.byId("goto"));
        assertEquals(EbslNodeType.CONTROL_REPEAT_UNTIL, EbslNodeType.byId("control-repeat-until"));
        assertTrue(EbslNodeType.ids().contains("sensor_health_below"));
    }

    @Test
    void registersExistingModFeaturesAsScriptNodes() {
        assertEquals("space_mob", EbslNodeRegistry.get("space_mob").id());
        assertEquals("goal_walk", EbslNodeRegistry.get("goal_walk").id());
        assertEquals("walk", EbslNodeRegistry.get("walk").id());
    }
}
