package fr.riege.ebsl.common.feature.scripting.docs;

import java.util.List;

public record EbslLanguageDocSection(String title, List<EbslLanguageDocEntry> entries) {
    public EbslLanguageDocSection {
        entries = List.copyOf(entries);
    }
}
