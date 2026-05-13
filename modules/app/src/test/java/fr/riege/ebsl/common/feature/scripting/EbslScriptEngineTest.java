package fr.riege.ebsl.common.feature.scripting;

import fr.riege.ebsl.common.feature.scripting.enums.EbslNodeType;
import fr.riege.ebsl.common.feature.scripting.registry.EbslSensorRegistry;
import fr.riege.ebsl.common.feature.scripting.parser.EbslProgram;
import fr.riege.ebsl.common.feature.scripting.registry.EbslNodeRegistry;
import fr.riege.ebsl.common.feature.scripting.runtime.EbslScriptEngine;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EbslScriptEngineTest {
    @Test
    void compilesNestedFlowAndFunctions() {
        EbslProgram program = EbslScriptEngine.compile("""
            start
            set wood minecraft:oak_log
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
    void variablesRequireExplicitPrefixWhenRead() {
        EbslProgram program = EbslScriptEngine.compile("""
            set wood minecraft:oak_log
            goal_nearest_block $wood 32
            """);

        assertEquals(2, program.statements().size());
        assertEquals("set_variable", EbslNodeRegistry.get("set").id());
    }

    @Test
    void keepsExecutableNodeTypesFocused() {
        assertEquals(EbslNodeType.GOTO, EbslNodeType.byId("goto"));
        assertEquals(EbslNodeType.CONTROL_REPEAT_UNTIL, EbslNodeType.byId("control-repeat-until"));
        assertFalse(EbslNodeType.ids().contains("sensor_health_below"));
        assertNotNull(EbslSensorRegistry.definition("sensor_health_below"));
    }

    @Test
    void registersExistingModFeaturesAsScriptNodes() {
        assertEquals("space_mob", EbslNodeRegistry.get("space_mob").id());
        assertEquals("goal_walk", EbslNodeRegistry.get("goal_walk").id());
        assertEquals("walk", EbslNodeRegistry.get("walk").id());
        assertEquals("break_block", EbslNodeRegistry.get("break_block").id());
        assertEquals("no_render", EbslNodeRegistry.get("no_render").id());
    }

    @Test
    void createsFreshRuntimeNodeInstances() {
        assertNotSame(EbslNodeRegistry.create("aim_at_block"), EbslNodeRegistry.create("aim_at_block"));
        assertNotSame(EbslNodeRegistry.get("aim_at_block"), EbslNodeRegistry.create("aim_at_block"));
    }

    @Test
    void nodeArgumentsAreSettingsBackedFields() {
        for (EbslNode node : EbslNodeRegistry.canonicalNodes()) {
            assertEquals(node.settings().size(), node.fields().size(), node.id());
            for (int i = 0; i < node.fields().size(); i++) {
                EbslNodeField field = node.fields().get(i);
                assertEquals(i, field.argumentIndex(), node.id() + "." + field.id());
                assertSame(node.settings().get(i), field.setting(), node.id() + "." + field.id());
                assertFalse(field.label().isBlank(), node.id() + "." + field.id());
                assertFalse(field.type().isBlank(), node.id() + "." + field.id());
                assertFalse(field.description().isBlank(), node.id() + "." + field.id());
            }
        }
    }
}
