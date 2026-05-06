package fr.riege.ebsl.mc;

import fr.riege.ebsl.common.layer.IStorageLayer;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

public class McStorageLayer implements IStorageLayer {
    private final Path dir;
    public McStorageLayer(Path dir) { this.dir = dir; }

    @Override
    public void save(String key, String json) {
        try {
            Files.createDirectories(dir);
            Files.writeString(dir.resolve(key + ".json"), json);
        } catch (IOException e) { throw new RuntimeException(e); }
    }

    @Override
    public Optional<String> load(String key) {
        try {
            var file = dir.resolve(key + ".json");
            return Files.exists(file) ? Optional.of(Files.readString(file)) : Optional.empty();
        } catch (IOException e) { return Optional.empty(); }
    }
}
