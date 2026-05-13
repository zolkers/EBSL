package fr.riege.ebsl.common.feature;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class AbstractEnabledFeatureTest {
    @Test
    void exposesStableMetadataAndEnabledSetting() {
        TestFeature feature = new TestFeature();

        assertEquals("test_feature", feature.id());
        assertEquals("Test Feature", feature.displayName());
        assertEquals("Feature used by tests.", feature.description());
        assertFalse(feature.isEnabled());

        feature.setEnabled(true);
        assertTrue(feature.isEnabled());
        assertEquals(1, feature.settings().size());

        feature.resetSettings();
        assertFalse(feature.isEnabled());
    }

    private static final class TestFeature extends AbstractEnabledFeature {
        private TestFeature() {
            super("test_feature", "Test Feature", "Feature used by tests.");
        }
    }
}
