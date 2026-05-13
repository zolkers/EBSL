package fr.riege.ebsl.common.platform.layer;

import java.util.List;
import java.util.Optional;

/**
 * Persists small JSON and text documents for shared features.
 *
 * <p>Implementations decide where data lives while exposing stable save, load, delete, and listing operations.</p>
 */
public interface IStorageLayer {
    /**
     * Persists a JSON document under the supplied storage key.
 *
     * @param key the storage or registry key
     * @param json the serialized JSON payload
     */
    void saveJson(String key, String json);
    /**
     * Loads a JSON document stored under the supplied key.
 *
     * @param key the storage or registry key
     * @return an optional value containing the result when it exists
     */
    Optional<String> loadJson(String key);

    /**
     * Persists a text document at the supplied logical path.
 *
     * @param path the path or file path to use
     * @param text the text value
     */
    void saveText(String path, String text);

    /**
     * Loads a text document from the supplied logical path.
 *
     * @param path the path or file path to use
     * @return an optional value containing the result when it exists
     */
    Optional<String> loadText(String path);

    /**
     * Deletes a text document at the supplied logical path when it exists.
 *
     * @param path the path or file path to use
     */
    default void deleteText(String path) {
    }

    /**
     * Lists text files in a logical directory matching the supplied extension.
 *
     * @param directory the directory to inspect
     * @param extension the file extension to match
     * @return the requested values
     */
    default List<String> listTextFiles(String directory, String extension) {
        return List.of();
    }
}
