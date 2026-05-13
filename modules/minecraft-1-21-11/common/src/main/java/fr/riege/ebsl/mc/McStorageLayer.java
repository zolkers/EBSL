package fr.riege.ebsl.mc;

import fr.riege.ebsl.common.platform.layer.IStorageLayer;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class McStorageLayer implements IStorageLayer {
    private final Path dir;
    public McStorageLayer(Path dir) { this.dir = dir; }

    @Override
    public void saveJson(String key, String json) {
        try {
            Files.createDirectories(dir);
            Files.writeString(dir.resolve(key + ".json"), json);
        } catch (IOException e) { throw new StorageAccessException("Unable to save json: " + key, e); }
    }

    @Override
    public Optional<String> loadJson(String key) {
        try {
            var file = dir.resolve(key + ".json");
            return Files.exists(file) ? Optional.of(Files.readString(file)) : Optional.empty();
        } catch (IOException e) { return Optional.empty(); }
    }

    @Override
    public void saveText(String path, String text) {
        try {
            Path file = resolveTextPath(path);
            Files.createDirectories(file.getParent());
            Files.writeString(file, text);
        } catch (IOException e) { throw new StorageAccessException("Unable to save text: " + path, e); }
    }

    @Override
    public Optional<String> loadText(String path) {
        try {
            Path file = resolveTextPath(path);
            return Files.exists(file) ? Optional.of(Files.readString(file)) : Optional.empty();
        } catch (IOException e) { return Optional.empty(); }
    }

    @Override
    public void deleteText(String path) {
        try {
            Files.deleteIfExists(resolveTextPath(path));
        } catch (IOException e) { throw new StorageAccessException("Unable to delete text: " + path, e); }
    }

    @Override
    public List<String> listTextFiles(String directory, String extension) {
        try {
            Path folder = resolveTextPath(directory);
            if (!Files.isDirectory(folder)) {
                return List.of();
            }
            String suffix = extension.startsWith(".") ? extension : "." + extension;
            try (var stream = Files.list(folder)) {
                return stream
                    .filter(Files::isRegularFile)
                    .map(folder::relativize)
                    .map(Path::toString)
                    .filter(name -> name.endsWith(suffix))
                    .sorted(Comparator.naturalOrder())
                    .toList();
            }
        } catch (IOException e) {
            return List.of();
        }
    }

    private Path resolveTextPath(String path) {
        Path resolved = dir.resolve(path.replace('\\', '/')).normalize();
        if (!resolved.startsWith(dir.normalize())) {
            throw new IllegalArgumentException("Storage path escapes EBSL directory: " + path);
        }
        return resolved;
    }

    private static final class StorageAccessException extends IllegalStateException {
        private StorageAccessException(String message, IOException cause) {
            super(message, cause);
        }
    }
}
