package fr.riege.ebsl.common.feature.scripting.manager;

import fr.riege.ebsl.common.core.settings.DoubleSetting;
import fr.riege.ebsl.common.core.settings.EnumSetting;
import fr.riege.ebsl.common.core.settings.IntSetting;
import fr.riege.ebsl.common.feature.scripting.enums.EbslNodeType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class EbslNodeFieldHelpTest {
    @Test
    void usesScopedDescriptionsBeforeGenericFieldDescriptions() {
        assertEquals(
            "How far to scan for a matching block to aim at. Keep this small inside tight loops.",
            EbslNodeFieldHelp.description("aim_at_block", "search_radius")
        );
        assertEquals(
            "Maximum block scan radius around the player. Larger values are more expensive.",
            EbslNodeFieldHelp.description("unknown_node", "search_radius")
        );
    }

    @Test
    void fallsBackForUnknownFieldsWithoutThrowing() {
        assertEquals(
            "Parameter passed to the custom node.",
            EbslNodeFieldHelp.description("custom", "future_field")
        );
    }

    @Test
    void rendersSettingMetadataAndValuesConsistently() {
        assertEquals("integer | default 3 | 1..8", EbslNodeFieldHelp.meta(new IntSetting("count", "Count", 3, 1, 8)));
        assertEquals("decimal | default 1.5 | 0..4", EbslNodeFieldHelp.meta(new DoubleSetting("speed", "Speed", 1.5, 0.0, 4.0)));
        assertTrue(EbslNodeFieldHelp.meta(new EnumSetting<>(
            "node", "Node", EbslNodeType.GOTO, EbslNodeType.class)).contains("goto"));
        assertEquals("empty", EbslNodeFieldHelp.value(""));
        assertEquals("empty", EbslNodeFieldHelp.value(List.of()));
        assertEquals("a, b", EbslNodeFieldHelp.value(List.of("a", "b")));
    }

    @Test
    void signatureReturnsRegisteredNodeFields() {
        String signature = EbslNodeFieldHelp.signature("aim_at_block");

        assertTrue(signature.contains("Block"));
        assertTrue(signature.contains("Search Radius"));
    }
}
