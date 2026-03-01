package fr.aetheris.api.storage.file;

import fr.aetheris.api.storage.KeyValueStore;
import fr.aetheris.api.storage.StorageException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;

public final class FileKeyValueStore implements KeyValueStore {

    private final Path file;
    private final Properties properties = new Properties();

    public FileKeyValueStore(Path file) {
        this.file = file;
        load();
    }

    @Override
    public synchronized void put(String key, String value) {
        properties.setProperty(key, value);
        save();
    }

    @Override
    public synchronized Optional<String> get(String key) {
        return Optional.ofNullable(properties.getProperty(key));
    }

    @Override
    public synchronized void delete(String key) {
        properties.remove(key);
        save();
    }

    @Override
    public synchronized Set<String> keys() {
        return properties.stringPropertyNames();
    }

    @Override
    public void close() {
        save();
    }

    private void load() {
        if (!Files.exists(file)) {
            return;
        }
        try (InputStream inputStream = Files.newInputStream(file)) {
            properties.load(inputStream);
        } catch (IOException exception) {
            throw new StorageException("Unable to load file storage: " + file, exception);
        }
    }

    private synchronized void save() {
        try {
            if (file.getParent() != null) {
                Files.createDirectories(file.getParent());
            }
            try (OutputStream outputStream = Files.newOutputStream(file)) {
                properties.store(outputStream, "Aetheris file storage");
            }
        } catch (IOException exception) {
            throw new StorageException("Unable to save file storage: " + file, exception);
        }
    }
}
