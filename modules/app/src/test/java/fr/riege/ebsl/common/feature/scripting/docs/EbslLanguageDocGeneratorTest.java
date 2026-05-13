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
