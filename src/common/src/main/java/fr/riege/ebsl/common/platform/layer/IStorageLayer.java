package fr.riege.ebsl.common.platform.layer;

import java.util.Optional;
import java.util.List;

public interface IStorageLayer {
    void saveJson(String key, String json);
    Optional<String> loadJson(String key);

    void saveText(String path, String text);

    Optional<String> loadText(String path);

    default void deleteText(String path) {
    }

    default List<String> listTextFiles(String directory, String extension) {
        return List.of();
    }
}
