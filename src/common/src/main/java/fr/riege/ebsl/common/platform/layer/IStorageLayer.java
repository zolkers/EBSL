package fr.riege.ebsl.common.platform.layer;

import java.util.Optional;
import java.util.List;

public interface IStorageLayer {
    void save(String key, String json);
    Optional<String> load(String key);

    default void saveText(String path, String text) {
        save(path, text);
    }

    default Optional<String> loadText(String path) {
        return load(path);
    }

    default List<String> listTextFiles(String directory, String extension) {
        return List.of();
    }
}
