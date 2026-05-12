package fr.riege.ebsl.common.feature.scripting.docs;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class EbslLanguageDocGeneratorTest {
    @Test
    void generatesDocsFromLanguageRegistries() {
        EbslLanguageDoc doc = EbslLanguageDocGenerator.generate();

        assertEntry(doc, "break_block");
        assertEntry(doc, "repeat_until");
        assertEntry(doc, "sensor_targeted_block");
        assertEntry(doc, "greater_than");
        assertEntry(doc, "wood");
    }

    private static void assertEntry(EbslLanguageDoc doc, String id) {
        boolean found = doc.sections().stream()
            .flatMap(section -> section.entries().stream())
            .anyMatch(entry -> entry.id().equals(id));
        assertTrue(found, "Missing generated doc entry: " + id);
    }
}
