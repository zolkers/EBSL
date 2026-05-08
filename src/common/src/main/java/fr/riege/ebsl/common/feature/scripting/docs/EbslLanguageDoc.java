package fr.riege.ebsl.common.feature.scripting.docs;

import java.util.List;

public record EbslLanguageDoc(List<EbslLanguageDocSection> sections) {
    public EbslLanguageDoc {
        sections = List.copyOf(sections);
    }
}
