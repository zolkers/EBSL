package fr.riege.ebsl.common.feature.scripting.manager;

import fr.riege.ebsl.common.platform.layer.IStorageLayer;
import java.util.List;
import java.util.Locale;

public final class EbslScriptManager {
    public static final String DIRECTORY = "scripts";
    public static final String EXTENSION = ".ebsl";
    public static final String DEFAULT_FILE = "main.ebsl";
    public static final String DEFAULT_SOURCE = """
        # EBSL script
        start
        message "EBSL ready"
        """;

    private final IStorageLayer storage;

    public EbslScriptManager(IStorageLayer storage) {
        this.storage = storage;
    }

    public List<String> scripts() {
        List<String> files = storage.listTextFiles(DIRECTORY, EXTENSION);
        if (files.isEmpty()) {
            return List.of(DEFAULT_FILE);
        }
        return files;
    }

    public EbslScriptDocument load(String fileName) {
        String normalized = normalizeFileName(fileName);
        String source = storage.loadText(path(normalized)).orElse(DEFAULT_SOURCE);
        return new EbslScriptDocument(normalized, source);
    }

    public EbslScriptDocument create(String fileName) {
        String normalized = normalizeFileName(fileName);
        storage.saveText(path(normalized), DEFAULT_SOURCE);
        return new EbslScriptDocument(normalized, DEFAULT_SOURCE);
    }

    public void save(String fileName, String source) {
        storage.saveText(path(normalizeFileName(fileName)), source == null ? "" : source);
    }

    public static String normalizeFileName(String fileName) {
        String normalized = fileName == null ? "" : fileName.trim().replace('\\', '/');
        int slash = normalized.lastIndexOf('/');
        if (slash >= 0) {
            normalized = normalized.substring(slash + 1);
        }
        normalized = normalized.toLowerCase(Locale.ROOT);
        if (normalized.isBlank()) {
            normalized = DEFAULT_FILE;
        }
        return normalized.endsWith(EXTENSION) ? normalized : normalized + EXTENSION;
    }

    public static String path(String fileName) {
        return DIRECTORY + "/" + normalizeFileName(fileName);
    }
}
