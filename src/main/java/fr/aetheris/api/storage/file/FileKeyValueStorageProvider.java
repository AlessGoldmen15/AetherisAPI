package fr.aetheris.api.storage.file;

import fr.aetheris.api.storage.KeyValueStore;
import fr.aetheris.api.storage.StorageException;
import fr.aetheris.api.storage.StorageProvider;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class FileKeyValueStorageProvider implements StorageProvider {

    private final Path baseDirectory;

    public FileKeyValueStorageProvider(Path baseDirectory) {
        this.baseDirectory = baseDirectory;
        try {
            Files.createDirectories(baseDirectory);
        } catch (IOException exception) {
            throw new StorageException("Unable to create file storage directory: " + baseDirectory, exception);
        }
    }

    @Override
    public KeyValueStore open(String namespace) {
        return new FileKeyValueStore(baseDirectory.resolve(namespace + ".properties"));
    }

    @Override
    public void close() {
    }
}
