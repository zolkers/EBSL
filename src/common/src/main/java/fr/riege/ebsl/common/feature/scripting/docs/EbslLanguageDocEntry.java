package fr.riege.ebsl.common.feature.scripting.docs;

import java.util.List;

public record EbslLanguageDocEntry(
    String id,
    String title,
    String description,
    String usage,
    String sample,
    List<String> aliases,
    List<EbslLanguageDocParameter> parameters
) {
    public EbslLanguageDocEntry {
        aliases = List.copyOf(aliases);
        parameters = List.copyOf(parameters);
    }
}
